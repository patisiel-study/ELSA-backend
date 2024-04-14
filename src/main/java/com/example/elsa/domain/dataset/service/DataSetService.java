package com.example.elsa.domain.dataset.service;

import com.example.elsa.domain.dataset.dto.DataSetDto;
import com.example.elsa.domain.dataset.dto.KeywordToDataSetDto;
import com.example.elsa.domain.dataset.entity.DataSet;
import com.example.elsa.domain.dataset.repository.DataSetRepository;
import com.example.elsa.global.error.CustomException;
import com.example.elsa.global.error.ErrorCode;
import com.example.elsa.global.util.DataFormatting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSetService {

    private final DataSetRepository dataSetRepository;

    public void addDataSet(DataSetDto dataSetDto) {

        String dataSetName = dataSetDto.getName().trim();

        if (DataFormatting.isNullOrEmpty(dataSetName)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_BODY);
        }

        // 해당 이름의 데이터 셋 있는지 확인
        dataSetRepository.findByName(dataSetName)
                // 이미 있으면 에러 발생, 없으면 새로 만들기
            .ifPresentOrElse(
                dataSet -> {
                    throw new CustomException(ErrorCode.DUPLICATE_DATA);
                },
                    () -> createNewDataSet(dataSetName)

            );
    }

    public void addKeywordToDataSet(KeywordToDataSetDto keywordToDataSetDto) {
        // 데이터가 있으면 하위 항목 추가, 없으면 에러 발생.
        String dataSetName = keywordToDataSetDto.getDataSetName().trim();
        String keywordName = keywordToDataSetDto.getKeywordName().trim();

        if (DataFormatting.isNullOrEmpty(dataSetName) || DataFormatting.isNullOrEmpty(keywordName)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_BODY);
        }

        dataSetRepository.findByName(dataSetName)
                .ifPresentOrElse(
                        dataSet -> addItemToExistingDataSet(dataSet, keywordName),
                        () -> {
                            throw new CustomException(ErrorCode.DATA_NOT_FOUND);
                        }
                );

    }

    public List<String> getAllDataSetNames() {
        return dataSetRepository.findAll()
                .stream()
                .map(DataSet::getName)
                .collect(Collectors.toList());
    }

    public List<String> getKeywordsByDataSetName(String dataSetName) {
        return dataSetRepository.findByName(dataSetName)
                .map(DataSet::getKeywords)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));
    }


    private void createNewDataSet(String dataSetName) {
        DataSet dataSet = new DataSet(dataSetName);
        try {
            dataSetRepository.save(dataSet);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void addItemToExistingDataSet(DataSet dataSet, String keywordName) {
        // 해당 dataSet의 하위 목록에 keyword가 존재하지 않는 경우에만 새로운 항목을 추가한다.
        List<String> keywords = dataSet.getKeywords();

        if (keywords.contains(keywordName)) {
            throw new CustomException(ErrorCode.DUPLICATE_DATA);
        }

        try {
            dataSet.addKeyword(keywordName);
            dataSetRepository.save(dataSet);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}

