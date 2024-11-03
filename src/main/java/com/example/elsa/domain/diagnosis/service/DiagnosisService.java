package com.example.elsa.domain.diagnosis.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.elsa.domain.diagnosis.dto.DiagnosisHistoryResponse;
import com.example.elsa.domain.diagnosis.dto.DiagnosisSubmitRequest;
import com.example.elsa.domain.diagnosis.dto.DiagnosisSubmitResponse;
import com.example.elsa.domain.diagnosis.dto.NoOrNotApplicableDto;
import com.example.elsa.domain.diagnosis.dto.NonMemberDiagnosisForUserSubmitRequest;
import com.example.elsa.domain.diagnosis.dto.NonMemberSubmitDiagnosisResponse;
import com.example.elsa.domain.diagnosis.dto.QnaPairDto;
import com.example.elsa.domain.diagnosis.dto.StandardQuestionsDto;
import com.example.elsa.domain.diagnosis.dto.StandardScoreDto;
import com.example.elsa.domain.diagnosis.dto.SubmitDiagnosisResponse;
import com.example.elsa.domain.diagnosis.dto.TotalScoreDto;
import com.example.elsa.domain.diagnosis.entity.Answer;
import com.example.elsa.domain.diagnosis.entity.Diagnosis;
import com.example.elsa.domain.diagnosis.entity.DiagnosisQnaSet;
import com.example.elsa.domain.diagnosis.entity.DiagnosisQuestion;
import com.example.elsa.domain.diagnosis.entity.DiagnosisType;
import com.example.elsa.domain.diagnosis.entity.NoOrNotApplicable;
import com.example.elsa.domain.diagnosis.entity.StandardScore;
import com.example.elsa.domain.diagnosis.repository.DiagnosisQnaSetRepository;
import com.example.elsa.domain.diagnosis.repository.DiagnosisQuestionRepository;
import com.example.elsa.domain.diagnosis.repository.DiagnosisRepository;
import com.example.elsa.domain.diagnosis.repository.NoOrNotApplicableRepository;
import com.example.elsa.domain.diagnosis.repository.StandardScoreRepository;
import com.example.elsa.domain.member.entity.Member;
import com.example.elsa.domain.member.repository.MemberRepository;
import com.example.elsa.domain.qna.repository.StandardRepository;
import com.example.elsa.global.error.CustomException;
import com.example.elsa.global.error.ErrorCode;
import com.example.elsa.global.util.ExcelHelper;
import com.example.elsa.global.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DiagnosisService {
	private final DiagnosisRepository diagnosisRepository;
	private final DiagnosisQuestionRepository diagnosisQuestionRepository;
	private final MemberRepository memberRepository;
	private final DiagnosisQnaSetRepository diagnosisQnaSetRepository;
	private final StandardScoreRepository standardScoreRepository;
	private final NoOrNotApplicableRepository noOrNotApplicableRepository;
	private final StandardRepository standardRepository;

	public void createDiagnosisQuestions(MultipartFile file) {
		try {
			Map<String, List<String>> data = ExcelHelper.parseDiagnosisQnaFile(file);

			for (Map.Entry<String, List<String>> entry : data.entrySet()) {
				String standardName = entry.getKey();
				List<String> questions = entry.getValue();

				questions.forEach(question -> {
					DiagnosisQuestion diagnosisQuestion = DiagnosisQuestion.builder()
						.question(question)
						.standardName(standardName)
						.diagnosisType(DiagnosisType.DEVELOPER)
						.build();
					diagnosisQuestionRepository.save(diagnosisQuestion);
				});
			}
		} catch (IOException e) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional(readOnly = true)
	public List<StandardQuestionsDto> getDiagnosisQuestions() {
		List<DiagnosisQuestion> diagnosisQuestionList = diagnosisQuestionRepository.findAll();

		// diagnosisQuestionList를 standardName으로 그룹화하여 StandardQuestionsDto로 변환

		return diagnosisQuestionList.stream()
			.filter(diagnosisQuestion -> diagnosisQuestion.getDiagnosisType() == DiagnosisType.DEVELOPER)
			.collect(Collectors.groupingBy(DiagnosisQuestion::getStandardName))
			.entrySet().stream()
			.map(entry -> new StandardQuestionsDto(entry.getKey(),
				getStandardDescription(entry.getKey()),
				entry.getValue().stream()
					.map(DiagnosisQuestion::toDto)  // toDto 메서드 사용
					.toList()))
			.toList();

	}

	private String getStandardDescription(String standardName) {
		log.debug("standardName: {}", standardName);
		return standardRepository.findByName(standardName)
			.orElseThrow(() -> new CustomException(ErrorCode.STANDARD_NOT_FOUND))
			.getDescription();
	}

	public SubmitDiagnosisResponse submitDiagnosisResult(DiagnosisSubmitRequest request) {
		// 진단 결과 생성 및 저장
		Long memberId = memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)).getMemberId();

		Diagnosis diagnosis = Diagnosis.createDiagnosis(memberId, 0.0, request.getLlmName());
		diagnosisRepository.save(diagnosis);  // 여기서 id가 생성됨

		Long diagnosisId = diagnosis.getDiagnosisId();

		// 각 standard별 YES, NO, NOT_APPLICABLE 답변 수 및 진단 점수 계산
		Map<String, Integer> yesCounts = new HashMap<>();
		Map<String, List<QnaPairDto>> noOrNotApplicableMap = new HashMap<>();

		request.getAnswers().forEach(answerDto -> {
				log.debug("answer: {}", answerDto.getAnswer());
				DiagnosisQuestion diagnosisQuestion = getDiagnosisQnaSet(answerDto.getQuestionId());
				String question = diagnosisQuestion.getQuestion();
				String standardName = diagnosisQuestion.getStandardName();

				DiagnosisQnaSet diagnosisQnaSet = DiagnosisQnaSet.builder()
					.question(question)
					.answer(answerDto.getAnswer())
					.standardName(standardName)
					.diagnosisId(diagnosisId)
					.build();

				diagnosisQnaSetRepository.save(diagnosisQnaSet);

				// Answer가 YES인 경우
				if (answerDto.getAnswer() == Answer.YES) {
					yesCounts.put(standardName, yesCounts.getOrDefault(standardName, 0) + 1);
				}
				// Answer가 NO 또는 미해당인 경우
				else if (answerDto.getAnswer() == Answer.NO || answerDto.getAnswer() == Answer.NOT_APPLICABLE) {
					noOrNotApplicableMap.putIfAbsent(standardName, new ArrayList<>());
					noOrNotApplicableMap.get(standardName).add(new QnaPairDto(question, answerDto.getAnswer()));
				}
			}
		);

		// 각 standard별로 점수 계산 및 저장
		yesCounts.forEach((standardName, yesCount) -> {
			StandardScore standardScore = StandardScore.builder()
				.standardName(standardName)
				.score(yesCount)
				.diagnosisId(diagnosisId)
				.build();
			standardScoreRepository.save(standardScore);
		});

		// NO 또는 미해당 답변 리스트 저장
		noOrNotApplicableMap.forEach((standardName, qnaPairDtoList) -> {
			NoOrNotApplicable noOrNotApplicable = NoOrNotApplicable.builder()
				.standardName(standardName)
				.qnaPairDtoList(qnaPairDtoList)
				.diagnosisId(diagnosisId)
				.build();
			noOrNotApplicableRepository.save(noOrNotApplicable);
		});

		// 총점 계산 (YES 답변 개수 기준으로 비율 계산)
		int totalYesAnswers = yesCounts.values().stream().mapToInt(Integer::intValue).sum();
		int totalQuestions = request.getAnswers().size();
		double ratioDouble = Math.round(((double)totalYesAnswers / totalQuestions) * 100.0) / 100.0;
		String ratioString = totalYesAnswers + "/" + totalQuestions;

		// 계산된 점수를 Diagnosis 엔티티에 반영하고 저장
		double finalScore = ratioDouble * 100;
		diagnosis.updateTotalScore(finalScore);
		diagnosis.updateTotalScoreToString(ratioString);
		diagnosisRepository.save(diagnosis);  // 업데이트된 totalScore 저장

		return new SubmitDiagnosisResponse(diagnosisId);
	}

	public NonMemberSubmitDiagnosisResponse nonMemberSubmitDiagnosisResult(
		NonMemberDiagnosisForUserSubmitRequest request) {
		// 비회원 생성
		Member member = Member.createNonMember(request);
		Long newNonMemberId = memberRepository.save(member).getMemberId();

		// 진단 결과 생성 및 저장
		Diagnosis diagnosis = Diagnosis.createDiagnosisForUser(newNonMemberId, 0.0);
		diagnosisRepository.save(diagnosis);  // 여기서 id가 생성됨

		Long diagnosisId = diagnosis.getDiagnosisId();

		// 각 standard별 YES, NO, NOT_APPLICABLE 답변 수 및 진단 점수 계산
		Map<String, Integer> yesCounts = new HashMap<>();
		Map<String, List<QnaPairDto>> noOrNotApplicableMap = new HashMap<>();

		request.getAnswers().forEach(answerDto -> {
				log.debug("answer: {}", answerDto.getAnswer());
				DiagnosisQuestion diagnosisQuestion = getDiagnosisQnaSet(answerDto.getQuestionId());
				String question = diagnosisQuestion.getQuestion();
				String standardName = diagnosisQuestion.getStandardName();

				DiagnosisQnaSet diagnosisQnaSet = DiagnosisQnaSet.builder()
					.question(question)
					.answer(answerDto.getAnswer())
					.standardName(standardName)
					.diagnosisId(diagnosisId)
					.build();

				diagnosisQnaSetRepository.save(diagnosisQnaSet);

				// Answer가 YES인 경우
				if (answerDto.getAnswer() == Answer.YES) {
					yesCounts.put(standardName, yesCounts.getOrDefault(standardName, 0) + 1);
				}
				// Answer가 NO 또는 미해당인 경우
				else if (answerDto.getAnswer() == Answer.NO || answerDto.getAnswer() == Answer.NOT_APPLICABLE) {
					noOrNotApplicableMap.putIfAbsent(standardName, new ArrayList<>());
					noOrNotApplicableMap.get(standardName).add(new QnaPairDto(question, answerDto.getAnswer()));
				}
			}
		);

		// 각 standard별로 점수 계산 및 저장
		yesCounts.forEach((standardName, yesCount) -> {
			StandardScore standardScore = StandardScore.builder()
				.standardName(standardName)
				.score(yesCount)
				.diagnosisId(diagnosisId)
				.build();
			standardScoreRepository.save(standardScore);
		});

		// NO 또는 미해당 답변 리스트 저장
		noOrNotApplicableMap.forEach((standardName, qnaPairDtoList) -> {
			NoOrNotApplicable noOrNotApplicable = NoOrNotApplicable.builder()
				.standardName(standardName)
				.qnaPairDtoList(qnaPairDtoList)
				.diagnosisId(diagnosisId)
				.build();
			noOrNotApplicableRepository.save(noOrNotApplicable);
		});

		// 총점 계산 (YES 답변 개수 기준으로 비율 계산)
		int totalYesAnswers = yesCounts.values().stream().mapToInt(Integer::intValue).sum();
		int totalQuestions = request.getAnswers().size();
		double ratioDouble = Math.round(((double)totalYesAnswers / totalQuestions) * 100.0) / 100.0;
		String ratioString = totalYesAnswers + "/" + totalQuestions;

		// 계산된 점수를 Diagnosis 엔티티에 반영하고 저장
		double finalScore = ratioDouble * 100;
		diagnosis.updateTotalScore(finalScore);
		diagnosis.updateTotalScoreToString(ratioString);
		diagnosisRepository.save(diagnosis);  // 업데이트된 totalScore 저장

		return new NonMemberSubmitDiagnosisResponse(diagnosisId);
	}

	@Transactional(readOnly = true)
	public DiagnosisSubmitResponse getDiagnosisDetails(Long diagnosisId) {
		Member member = memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
		Long memberId = member.getMemberId();

		Diagnosis diagnosis = diagnosisRepository.findByMemberIdAndDiagnosisId(memberId, diagnosisId)
			.orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSIS_NOT_FOUND));

		List<StandardScoreDto> standardScoreDtoList = standardScoreRepository.findByDiagnosisId(diagnosisId)
			.stream()
			.map(req -> new StandardScoreDto(req.getStandardName(), req.getScore()))
			.toList();

		List<NoOrNotApplicableDto> noOrNotApplicableDtoList = noOrNotApplicableRepository.findByDiagnosisId(diagnosisId)
			.stream()
			.map(req -> new NoOrNotApplicableDto(
				req.getStandardName(),
				getStandardDescription(req.getStandardName()),
				req.getQnaPairDtoList()))
			.toList();

		String totalScoreString = diagnosis.getTotalScoreToString();
		double totalScore = diagnosis.getTotalScore();

		return new DiagnosisSubmitResponse(
			diagnosis.getLlmName(),
			standardScoreDtoList,
			noOrNotApplicableDtoList,
			new TotalScoreDto(totalScoreString, totalScore),
			member.getCareer().getDescription(),
			member.getCountry().getDescription()
		);
	}

	@Transactional(readOnly = true)
	public DiagnosisSubmitResponse getDiagnosisDetailsByNonMember(Long diagnosisId) {

		Diagnosis diagnosis = diagnosisRepository.findByDiagnosisId(diagnosisId)
			.orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSIS_NOT_FOUND));

		Member member = memberRepository.findByMemberId(diagnosis.getMemberId())
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		List<StandardScoreDto> standardScoreDtoList = standardScoreRepository.findByDiagnosisId(diagnosisId)
			.stream()
			.map(req -> new StandardScoreDto(req.getStandardName(), req.getScore()))
			.toList();

		List<NoOrNotApplicableDto> noOrNotApplicableDtoList = noOrNotApplicableRepository.findByDiagnosisId(diagnosisId)
			.stream()
			.map(req -> new NoOrNotApplicableDto(
				req.getStandardName(),
				getStandardDescription(req.getStandardName()),
				req.getQnaPairDtoList()))
			.toList();

		String totalScoreString = diagnosis.getTotalScoreToString();
		double totalScore = diagnosis.getTotalScore();

		return new DiagnosisSubmitResponse(
			diagnosis.getLlmName(),
			standardScoreDtoList,
			noOrNotApplicableDtoList,
			new TotalScoreDto(totalScoreString, totalScore),
			member.getCareer().getDescription(),
			member.getCountry().getDescription()
		);
	}

	private DiagnosisQuestion getDiagnosisQnaSet(Long questionId) {
		return diagnosisQuestionRepository.findById(questionId)
			.orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND));
	}

	public List<DiagnosisHistoryResponse> getDiagnosisHistory() {
		Long memberId = memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)).getMemberId();

		List<Diagnosis> diagnosisList = diagnosisRepository.findByMemberIdOrderByTotalScoreDesc(memberId)
			.stream()
			.filter(diagnosis -> diagnosis.getDiagnosisType() == DiagnosisType.DEVELOPER)
			.toList();

		return diagnosisList.stream()
			.map(diagnosis -> new DiagnosisHistoryResponse(
				diagnosis.getDiagnosisId(),
				diagnosis.getCreatedAt(),
				diagnosis.getTotalScore(),
				diagnosis.getTotalScoreToString(),
				diagnosis.getLlmName()))
			.toList();
	}

}