package com.example.elsa.domain.dataset.controller;

import com.example.elsa.domain.dataset.dto.DataSetDto;
import com.example.elsa.domain.dataset.dto.KeywordToDataSetDto;
import com.example.elsa.domain.dataset.service.DataSetService;
import com.example.elsa.global.util.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "데이터 셋 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DataSetController {
    private final DataSetService dataSetService;

//    @PostMapping("/collect/qna")
//    public ResponseEntity<ResponseDto<?>> collectQna() {
//
//    }

    @Operation(summary = "데이터 셋 생성")
    @PostMapping("/create/data-set")
    public ResponseEntity<ResponseDto<?>> addDataSet(@RequestBody DataSetDto dataSetDto) {
        dataSetService.addDataSet(dataSetDto);
        return ResponseEntity.ok(new ResponseDto<>("데이터 셋 추가가 완료되었습니다.", null));
    }

    @Operation(summary = "데이터 셋 하위 키워드 생성")
    @PostMapping("/collect/keyword")
    public ResponseEntity<ResponseDto<?>> collectKeyword(@RequestBody KeywordToDataSetDto keywordToDataSetDto) {
        dataSetService.addKeywordToDataSet(keywordToDataSetDto);
        return ResponseEntity.ok(new ResponseDto<>("해당 데이터 셋에 대한 키워드 추가가 완료되었습니다.", null));
    }

    @Operation(summary = "모든 데이터 셋 리스트 조회")
    @GetMapping("/list/data-set")
    public ResponseEntity<ResponseDto<?>> getAllDataSet() {
        return ResponseEntity.ok(new ResponseDto<>("모든 데이터셋 리스트 조회가 완료되었습니다.", dataSetService.getAllDataSetNames()));
    }

    @Operation(summary = "특정 데이터 셋의 하위 키워드 목록 조회")
    @GetMapping("/{dataSetName}/keywords")
    public ResponseEntity<ResponseDto<?>> getKeywordsByDataSetName(@PathVariable String dataSetName) {
        return ResponseEntity.ok(new ResponseDto<>(dataSetName + "의 하위 키워드 리스트 조회가 완료되었습니다.", dataSetService.getKeywordsByDataSetName(dataSetName)));
    }
}
