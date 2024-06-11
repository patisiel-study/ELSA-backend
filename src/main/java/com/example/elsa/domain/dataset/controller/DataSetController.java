package com.example.elsa.domain.dataset.controller;

import com.example.elsa.domain.dataset.dto.DataSetDto;
import com.example.elsa.domain.dataset.dto.KeywordToDataSetDto;
import com.example.elsa.domain.dataset.service.DataSetService;
import com.example.elsa.global.util.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "데이터 셋 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DataSetController {
    private final DataSetService dataSetService;

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
    @GetMapping("/{dataSetName}/list/keywords")
    public ResponseEntity<ResponseDto<?>> getKeywordsByDataSetName(@PathVariable String dataSetName) {
        return ResponseEntity.ok(new ResponseDto<>(dataSetName + "의 하위 키워드 리스트 조회가 완료되었습니다.", dataSetService.getKeywordsByDataSetName(dataSetName)));
    }

    @Operation(summary = "엑셀 파일 업로드를 통해 데이터 셋 및 키워드 생성")
    @PostMapping(value = "/upload/dataset-keyword", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<?>> uploadDataSetAndKeyword(@RequestPart("file") @Schema(type = "string", format = "binary") MultipartFile file) {
        dataSetService.uploadDataSetAndKeyword(file);
        return ResponseEntity.ok(new ResponseDto<>("데이터 셋과 하위 키워드에 대한 업로드가 완료되었습니다.", null));
    }
}
