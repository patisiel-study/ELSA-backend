package com.example.elsa.domain.diagnosis.controller;

import com.example.elsa.domain.diagnosis.dto.DiagnosisSubmitRequest;
import com.example.elsa.domain.diagnosis.dto.DiagnosisSubmitResponse;
import com.example.elsa.domain.diagnosis.dto.StandardQuestionsDto;
import com.example.elsa.domain.diagnosis.service.DiagnosisService;
import com.example.elsa.global.util.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "자가진단 API")
@RequestMapping("/api/diagnosis")
public class DiagnosisController {
    private final DiagnosisService diagnosisService;

    @Operation(summary = "자가진단 문제 등록")
    @PostMapping(value = "/upload/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<?>> createDiagnosisQuestions(@RequestPart("file") MultipartFile file) {
        diagnosisService.createDiagnosisQuestions(file);
        return ResponseEntity.ok(new ResponseDto<>("자가진단 문제 등록이 완료되었습니다."));
    }

    @Operation(summary = "자가진단 문제 리스트 가져오기")
    @GetMapping("/list/questions")
    public ResponseEntity<ResponseDto<?>> getDiagnosisQuestions() {
        List<StandardQuestionsDto> questionsGroupedByStandard = diagnosisService.getDiagnosisQuestionRepository();
        return ResponseEntity.ok(new ResponseDto<>("자가진단 문제 리스트 조회가 완료되었습니다.", questionsGroupedByStandard));
    }

    @Operation(summary = "자가진단 결과 제출", description = """
            answer = YES, NO, NOT_APPLICABLE(미해당)
            """)
    @PostMapping("/submit/result")
    public ResponseEntity<ResponseDto<?>> submitDiagnosisResult(@RequestBody DiagnosisSubmitRequest request) {
        diagnosisService.submitDiagnosisResult(request);
        return ResponseEntity.ok(new ResponseDto<>("자가진단 결과를 성공적으로 제출 완료했습니다."));
    }



//    @Operation(summary = "회원의 자가진단 결과 목록 조회")
//    @GetMapping("/history")
//    public ResponseEntity<ResponseDto<?>> getMemberDiagnosisHistory() {
//        List<MemberDiagnosisListDto> history = diagnosisService.getMemberDiagnosisHistory();
//        return ResponseEntity.ok(new ResponseDto<>("회원의 자가진단 결과 목록들을 가져옵니다.", history));
//    }

    @Operation(summary = "회원의 단일 자가진단 결과 상세 조회")
    @GetMapping("/result/{diagnosisId}")
    public ResponseEntity<ResponseDto<?>> getSingleDiagnosisResult(@PathVariable Long diagnosisId) {
        DiagnosisSubmitResponse response = diagnosisService.getDiagnosisDetails(diagnosisId);
        return ResponseEntity.ok(new ResponseDto<>("회원의 단일 자가진단 결과를 조회합니다.", response));
    }
}