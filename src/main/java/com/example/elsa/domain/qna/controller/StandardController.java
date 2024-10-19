package com.example.elsa.domain.qna.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.elsa.domain.qna.dto.QnaToDeleteRequest;
import com.example.elsa.domain.qna.dto.QnaToStandardDto;
import com.example.elsa.domain.qna.entity.QnaSet;
import com.example.elsa.domain.qna.enums.LLMModel;
import com.example.elsa.domain.qna.service.AnswerService;
import com.example.elsa.domain.qna.service.StandardService;
import com.example.elsa.global.util.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "스탠다드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/standard")
public class StandardController {
	private final StandardService standardService;
	private final AnswerService answerService;

	@Operation(summary = "모든 스탠다드의 답변들에 대한 점수 반환")
	@GetMapping("/analyze/sentiments")
	public ResponseEntity<ResponseDto<Map<String, Double>>> analyzeAllStandardQnaSentiments() {
		Map<String, Double> sentimentScores = standardService.analyzeAllStandardQnaSentiments();
		return ResponseEntity.ok(new ResponseDto<>("해당 모델에 대한 윤리 평가가 완료되었습니다.", sentimentScores));
	}

	@Operation(summary = "엑셀 파일 업로드를 통해 Q&A 생성")
	@PostMapping(value = "/admin/upload/qna", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseDto<?>> uploadQna(@RequestPart("file") MultipartFile file) {
		standardService.uploadAndProcessQna(file);
		return ResponseEntity.ok(new ResponseDto<>("Q&A 업로드가 완료되었습니다.", null));
	}

	@Operation(summary = "선택한 모델에 대해 질문 답변 생성")
	@PostMapping("/admin/generate-answers")
	public ResponseEntity<ResponseDto<?>> generateAnswers(@RequestParam LLMModel model) {
		standardService.generateAnswersForModel(model);
		return ResponseEntity.ok(new ResponseDto<>(model.name() + "에 대한 답변 생성이 완료되었습니다.", null));
	}

	// @Operation(summary = "스탠다드 생성", description = "비활성 기능")
	// @PostMapping("/admin/create/standard")
	// public ResponseEntity<ResponseDto<?>> addStandard(@RequestBody StandardDto standardDto) {
	//     standardService.addStandard(standardDto);
	//     return ResponseEntity.ok(new ResponseDto<>("스탠다드 추가가 완료되었습니다.", null));
	// }

	@Operation(summary = "질문/답변 추가", description = "비활성 기능")
	@PostMapping("/admin/create/qna")
	public ResponseEntity<ResponseDto<?>> addQna(@RequestBody QnaToStandardDto qnaToStandardDto,
		@RequestParam LLMModel model) {
		CompletableFuture<Void> future = standardService.addQnaToStandard(qnaToStandardDto, model);
		future.join(); // CompletableFuture의 결과를 기다림
		return ResponseEntity.ok(new ResponseDto<>("질문/답변 추가가 완료되었습니다.", null));
	}

	@Operation(summary = "모든 스탠다드 리스트 조회")
	@GetMapping("/list/standard")
	public ResponseEntity<ResponseDto<?>> getAllStandard() {
		return ResponseEntity.ok(new ResponseDto<>("모든 스탠다드 리스트 조회가 완료되었습니다.", standardService.getAllStandardNames()));
	}

	@Operation(summary = "특정 스탠다드 항목의 Qna 리스트 조회")
	@GetMapping("/{standardName}/list/qna")
	public ResponseEntity<ResponseDto<?>> getAllQnaByStandard(@PathVariable String standardName) {
		return ResponseEntity.ok(new ResponseDto<>(standardName + "의 Qna 리스트 조회가 완료되었습니다.",
			standardService.getAllQnaByStandardName(standardName)));
	}

	@Operation(summary = "질문/답변 삭제", description = "비활성 기능")
	@DeleteMapping("/admin/delete/qna")
	public ResponseEntity<ResponseDto<?>> deleteQnaFromStandard(@RequestBody QnaToDeleteRequest request) {
		standardService.removeQnaFromStandard(request.getStandardName(), request.getQnaSetId());
		return ResponseEntity.ok(new ResponseDto<>("질문/답변 삭제가 완료되었습니다.", null));
	}

	@Operation(summary = "선택한 LLM 모델로 질문에 대한 답변 얻기")
	@PostMapping("/admin/answer")
	public ResponseEntity<ResponseDto<String>> getAnswerFromLLM(@RequestBody String question,
		@RequestParam LLMModel model) {
		String answer = answerService.getAnswer(question, model).join();
		return ResponseEntity.ok(new ResponseDto<>(model.name() + "의 답변이 생성되었습니다.", answer));
	}

	@Operation(summary = "특정 스탠다드 항목의 선택한 LLM 모델 답변 조회", description = "비활성 기능")
	@GetMapping("/admin/{standardName}/list/qna/answers")
	public ResponseEntity<ResponseDto<?>> getQnaAnswersByModel(@PathVariable String standardName,
		@RequestParam LLMModel model) {
		List<QnaSet> qnaSets = standardService.getAllQnaByStandardName(standardName);
		List<String> answerList = qnaSets.stream()
			.map(qnaSet -> {
				CompletableFuture<String> answerFuture = answerService.getAnswer(qnaSet.getQuestion(), model);
				try {
					return answerFuture.get();
				} catch (Exception e) {
					return "Error occurred while getting the answer.";
				}
			})
			.collect(Collectors.toList());

		return ResponseEntity.ok(
			new ResponseDto<>(standardName + "의 " + model.name() + " 모델 답변 조회가 완료되었습니다.", answerList));
	}

	@Operation(summary = "특정 모델에 대한 모든 스탠다드의 점수 계산 및 저장")
	@PostMapping("/admin/calculate-scores/{model}")
	public ResponseEntity<ResponseDto<?>> calculateScoresForModel(@PathVariable LLMModel model) {
		Map<String, Object> scores = standardService.calculateAndSaveScoresForModel(model);
		return ResponseEntity.ok(new ResponseDto<>(model.name() + "에 대한 모든 스탠다드의 점수 계산 및 저장이 완료되었습니다.", scores));
	}

	@Operation(summary = "모든 모델의 모든 스탠다드 점수 조회")
	@GetMapping("/all-scores")
	public ResponseEntity<ResponseDto<Map<LLMModel, Map<String, Object>>>> getAllScores() {
		Map<LLMModel, Map<String, Object>> allScores = standardService.getAllScores();
		return ResponseEntity.ok(new ResponseDto<>("모든 모델의 모든 스탠다드 점수 조회가 완료되었습니다.", allScores));
	}

}