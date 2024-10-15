package com.example.elsa.domain.diagnosis.controller;

import com.example.elsa.domain.diagnosis.dto.DiagnosisHistoryResponse;
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

    @Operation(summary = "자가진단 문제 등록", description = "비활성 기능")
    @PostMapping(value = "/admin/upload/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
    @PostMapping("/developer/submit")
    public ResponseEntity<ResponseDto<?>> submitDiagnosisResult(@RequestBody DiagnosisSubmitRequest request) {
        return ResponseEntity.ok(new ResponseDto<>("자가진단 결과를 성공적으로 제출 완료했습니다.", diagnosisService.submitDiagnosisResult(request)));
    }

    @Operation(summary = "회원의 자가진단 기록들 조회.", description = """
            높은 점수 순으로 정렬됨
            answer = YES, NO, NOT_APPLICABLE(미해당)
            """)
    @GetMapping("/developer/result/history")
    public ResponseEntity<List<DiagnosisHistoryResponse>> getDiagnosisHistory() {
        return ResponseEntity.ok(diagnosisService.getDiagnosisHistory());
    }

    @Operation(summary = "회원의 단일 자가진단 결과 상세 조회", description = """
            각 스탠다드별 점수(Yes로 대답한 개수), 미해당 및 미응답으로 대답한 문항의 QNA 리스트, 총점 반환
            """)
    @GetMapping("/developer/result/detail/{diagnosisId}")
    public ResponseEntity<ResponseDto<?>> getSingleDiagnosisResult(@PathVariable Long diagnosisId) {
        DiagnosisSubmitResponse response = diagnosisService.getDiagnosisDetails(diagnosisId);
        return ResponseEntity.ok(new ResponseDto<>("회원의 단일 자가진단 결과를 조회합니다.", response));
    }
}