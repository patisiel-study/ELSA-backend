package com.example.elsa.domain.diagnosis.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.elsa.domain.diagnosis.dto.DiagnosisForUserSubmitRequest;
import com.example.elsa.domain.diagnosis.dto.DiagnosisHistoryForUserResponse;
import com.example.elsa.domain.diagnosis.dto.DiagnosisHistoryResponse;
import com.example.elsa.domain.diagnosis.dto.DiagnosisSubmitRequest;
import com.example.elsa.domain.diagnosis.dto.DiagnosisSubmitResponse;
import com.example.elsa.domain.diagnosis.dto.NonMemberDiagnosisForUserSubmitRequest;
import com.example.elsa.domain.diagnosis.dto.NonMemberDiagnosisSubmitRequest;
import com.example.elsa.domain.diagnosis.dto.StandardQuestionsDto;
import com.example.elsa.domain.diagnosis.service.DiagnosisService;
import com.example.elsa.domain.diagnosis.service.DiagnosisServiceForUser;
import com.example.elsa.global.util.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "자가진단 API")
@RequestMapping("/api/diagnosis")
public class DiagnosisController {
	private final DiagnosisService diagnosisService;
	private final DiagnosisServiceForUser diagnosisServiceForUser;

	@Operation(summary = "자가진단 문제 등록(개발자용)")
	@PostMapping(value = "/admin/upload/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseDto<?>> createDiagnosisQuestions(@RequestPart("file") MultipartFile file) {
		diagnosisService.createDiagnosisQuestions(file);
		return ResponseEntity.ok(new ResponseDto<>("자가진단 문제 등록이 완료되었습니다."));
	}

	@Operation(summary = "자가진단 문제 등록(사용자용)")
	@PostMapping(value = "/admin/upload/questions/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseDto<?>> createDiagnosisQuestionsForUser(@RequestPart("file") MultipartFile file) {
		diagnosisServiceForUser.createDiagnosisQuestionsForUser(file);
		return ResponseEntity.ok(new ResponseDto<>("자가진단 문제 등록이 완료되었습니다."));
	}

	@Operation(summary = "자가진단 문제 리스트 가져오기(개발자용)")
	@GetMapping("/list/questions")
	public ResponseEntity<ResponseDto<?>> getDiagnosisQuestions() {
		List<StandardQuestionsDto> questionsGroupedByStandard = diagnosisService.getDiagnosisQuestions();
		return ResponseEntity.ok(new ResponseDto<>("자가진단 문제 리스트 조회가 완료되었습니다.", questionsGroupedByStandard));
	}

	@Operation(summary = "자가진단 문제 리스트 가져오기(사용자용)")
	@GetMapping("/list/questions/user")
	public ResponseEntity<ResponseDto<?>> getDiagnosisQuestionsForUser() {
		List<StandardQuestionsDto> questionsGroupedByStandard = diagnosisServiceForUser.getDiagnosisQuestionsForUser();
		return ResponseEntity.ok(new ResponseDto<>("자가진단 문제 리스트 조회가 완료되었습니다.", questionsGroupedByStandard));
	}

	@Operation(summary = "회원의 자가진단 결과 제출(개발자용)", description = """
		answer = YES, NO, NOT_APPLICABLE(미해당)
		""")
	@PostMapping("/developer/submit")
	public ResponseEntity<ResponseDto<?>> submitDiagnosisResult(@RequestBody DiagnosisSubmitRequest request) {
		return ResponseEntity.ok(
			new ResponseDto<>("자가진단 결과를 성공적으로 제출 완료했습니다.", diagnosisService.submitDiagnosisResult(request)));
	}

	@Operation(summary = "회원의 자가진단 결과 제출(사용자용)", description = """
		answer = YES, NO, NOT_APPLICABLE(미해당)
		""")
	@PostMapping("/developer/submit/user")
	public ResponseEntity<ResponseDto<?>> submitDiagnosisResultForUser(
		@RequestBody DiagnosisForUserSubmitRequest request) {
		return ResponseEntity.ok(
			new ResponseDto<>("자가진단 결과를 성공적으로 제출 완료했습니다.",
				diagnosisServiceForUser.submitDiagnosisResultForUser(request)));
	}

	@Operation(summary = "비회원의 자가진단 결과 제출(개발자용)", description = """
		answer = YES, NO, NOT_APPLICABLE(미해당)
		프론트는 비회원이 사전에 기록해둔 국가와 직업 정보를 로컬에 두었다가 해당 api와 함께 보내야 함.
		""")
	@PostMapping("/non-member/submit")
	public ResponseEntity<ResponseDto<?>> submitNonMemberDiagnosisResult(
		@RequestBody NonMemberDiagnosisSubmitRequest request) {
		return ResponseEntity.ok(new ResponseDto<>("비회원의 자가진단 결과를 성공적으로 제출 완료했습니다.",
			diagnosisService.nonMemberSubmitDiagnosisResult(request)));
	}

	@Operation(summary = "비회원의 자가진단 결과 제출(사용자용)", description = """
		answer = YES, NO, NOT_APPLICABLE(미해당)
		프론트는 비회원이 사전에 기록해둔 국가와 직업 정보를 로컬에 두었다가 해당 api와 함께 보내야 함.
		""")
	@PostMapping("/non-member/submit/user")
	public ResponseEntity<ResponseDto<?>> submitNonMemberDiagnosisResultForUser(
		@RequestBody NonMemberDiagnosisForUserSubmitRequest request) {
		return ResponseEntity.ok(new ResponseDto<>("비회원의 자가진단 결과를 성공적으로 제출 완료했습니다.",
			diagnosisServiceForUser.nonMemberSubmitDiagnosisResultForUser(request)));
	}

	@Operation(summary = "회원의 자가진단 기록들 조회(개발자용)", description = """
		높은 점수 순으로 정렬됨
		answer = YES, NO, NOT_APPLICABLE(미해당)
		""")
	@GetMapping("/developer/result/history")
	public ResponseEntity<List<DiagnosisHistoryResponse>> getDiagnosisHistory() {
		return ResponseEntity.ok(diagnosisService.getDiagnosisHistory());
	}

	@Operation(summary = "회원의 자가진단 기록들 조회(사용자용)", description = """
		높은 점수 순으로 정렬됨
		answer = YES, NO, NOT_APPLICABLE(미해당)
		""")
	@GetMapping("/developer/result/history/user")
	public ResponseEntity<List<DiagnosisHistoryForUserResponse>> getDiagnosisHistoryForUser() {
		return ResponseEntity.ok(diagnosisServiceForUser.getDiagnosisHistoryForUser());
	}

	@Operation(summary = "회원의 단일 자가진단 결과 상세 조회(개발자, 사용자)", description = """
		각 스탠다드별 점수(Yes로 대답한 개수), 미해당 및 미응답으로 대답한 문항의 QNA 리스트, 총점 반환.
		사용자 자가 진단 결과의 경우 llmName은 렌더링 할 필요없음.
		""")
	@GetMapping("/developer/result/detail/{diagnosisId}")
	public ResponseEntity<ResponseDto<?>> getSingleDiagnosisResult(@PathVariable Long diagnosisId) {
		DiagnosisSubmitResponse response = diagnosisService.getDiagnosisDetails(diagnosisId);
		return ResponseEntity.ok(new ResponseDto<>("회원의 단일 자가진단 결과를 조회합니다.", response));
	}

	@Operation(summary = "비회원의 단일 자가진단 결과 상세 조회(개발자, 사용자)", description = """
		각 스탠다드별 점수(Yes로 대답한 개수), 미해당 및 미응답으로 대답한 문항의 QNA 리스트, 총점 반환.
		사용자 자가 진단 결과의 경우 llmName은 렌더링 할 필요없음.
		""")
	@GetMapping("/non-member/result/detail/{diagnosisId}")
	public ResponseEntity<ResponseDto<?>> getSingleNonMemberDiagnosisResult(@PathVariable Long diagnosisId) {
		DiagnosisSubmitResponse response = diagnosisService.getDiagnosisDetailsByNonMember(diagnosisId);
		return ResponseEntity.ok(new ResponseDto<>("비회원의 단일 자가진단 결과를 조회합니다.", response));
	}
}