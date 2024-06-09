package com.example.elsa.domain.qna.controller;

import com.example.elsa.domain.qna.dto.QnaToDeleteRequest;
import com.example.elsa.domain.qna.dto.QnaToStandardDto;
import com.example.elsa.domain.qna.dto.StandardDto;
import com.example.elsa.domain.qna.service.StandardService;
import com.example.elsa.global.util.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "스탠다드 API")
@RestController
@RequiredArgsConstructor
public class StandardController {
    private final StandardService standardService;

    @Operation(summary = "스탠다드 생성")
    @PostMapping("/create/standard")
    public ResponseEntity<ResponseDto<?>> addStandard(@RequestBody StandardDto standardDto) {
        standardService.addStandard(standardDto);
        return ResponseEntity.ok(new ResponseDto<>("스탠다드 추가가 완료되었습니다.", null));
    }

    @Operation(summary = "질문/답변 추가")
    @PostMapping("/create/qna")
    public ResponseEntity<ResponseDto<?>> addQna(@RequestBody QnaToStandardDto qnaToStandardDto) {
        standardService.addQnaToStandard(qnaToStandardDto);
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
        return ResponseEntity.ok(new ResponseDto<>(standardName + "의 Qna 리스트 조회가 완료되었습니다.", standardService.getAllQnaByStandardName(standardName)));
    }

    @Operation(summary = "질문/답변 삭제")
    @DeleteMapping("/delete/qna")
    public ResponseEntity<ResponseDto<?>> deleteQnaFromStandard(@RequestBody QnaToDeleteRequest request) {
        standardService.removeQnaFromStandard(request.getStandardName(), request.getQnaSetId());
        return ResponseEntity.ok(new ResponseDto<>("질문/답변 삭제가 완료되었습니다.", null));
    }
}
