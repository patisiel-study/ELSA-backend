package com.example.elsa.domain.diagnosis.controller;

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
        List<StandardQuestionsDto> questionsGroupedByStandard = diagnosisService.getDiagnosisQuestions();
        return ResponseEntity.ok(new ResponseDto<>("자가진단 문제 리스트 조회가 완료되었습니다.", questionsGroupedByStandard));
    }
}