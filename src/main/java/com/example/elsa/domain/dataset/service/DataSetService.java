package com.example.elsa.domain.dataset.service;

import com.example.elsa.domain.dataset.dto.DataSetDto;
import com.example.elsa.domain.dataset.dto.KeywordToDataSetDto;
import com.example.elsa.domain.dataset.entity.DataSet;
import com.example.elsa.domain.dataset.repository.DataSetRepository;
import com.example.elsa.global.error.CustomException;
import com.example.elsa.global.error.ErrorCode;
import com.example.elsa.global.util.DataFormatting;
import com.example.elsa.global.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSetService {

    private final DataSetRepository dataSetRepository;

    public void addDataSet(DataSetDto dataSetDto) {

        String dataSetName = dataSetDto.getName().trim(); //공백 제거 후 저장

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

    //기존 데이터 세트에 키워드를 추가
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

    //모든 데이터 셋의 목록 반환
    public List<String> getAllDataSetNames() {
        return dataSetRepository.findAll()
                .stream()
                .map(DataSet::getName) //이름만
                .collect(Collectors.toList());
    }

    //데이터 세트 이름에 해당하는 키워드 목록을 반환
    public List<String> getKeywordsByDataSetName(String dataSetName) {
        return dataSetRepository.findByName(dataSetName)
                .map(DataSet::getKeywords)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));
    }


    //새로운 데이터 세트를 생성
    private void createNewDataSet(String dataSetName) {
        DataSet dataSet = new DataSet(dataSetName); //주어진 이름으로 새 DataSet 객체를 생성
        try {
            dataSetRepository.save(dataSet);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    //기존 데이터 세트에 새 키워드를 추가
    private void addItemToExistingDataSet(DataSet dataSet, String keywordName) {
        // 해당 dataSet의 하위 목록에 keyword가 존재하지 않는 경우에만 새로운 항목을 추가한다.
        List<String> keywords = dataSet.getKeywords();

        if (keywords.contains(keywordName)) { //키워드 하면
            throw new CustomException(ErrorCode.DUPLICATE_DATA);
        }

        try {
            dataSet.addKeyword(keywordName); // 새 키워드 추가
            dataSetRepository.save(dataSet); //변경된 데이터 세트를 저장
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void uploadDataSetAndKeyword(MultipartFile file) {
        try {
            // dataSet, keyword의 리스트 형태의 map으로 가져옴
            Map<String, List<String>> data = ExcelHelper.parseDataSetAndKewordFile(file);
            data.forEach(this::addDataSetWithKeywords);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    //이 메소드는 데이터 세트 이름과 키워드 목록을 매개변수로 받아, 데이터 세트를 생성하거나 업데이트합니다.
    private void addDataSetWithKeywords(String name, List<String> keywords) {
        DataSet dataSet = dataSetRepository.findByName(name)
                .orElseGet(() -> new DataSet(name)); //데이터 세트가 존재하면 그 데이터 세트를 반환

        keywords.forEach(dataSet::addKeyword);
        dataSetRepository.save(dataSet);
    }

}
