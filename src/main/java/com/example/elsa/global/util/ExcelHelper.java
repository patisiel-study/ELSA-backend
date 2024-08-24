package com.example.elsa.global.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
public class ExcelHelper {
    /**
     * 엑셀 파일을 파싱하여 데이터 셋과 해당 키워드를 추출합니다.
     *
     * @param file 파싱할 엑셀 파일
     * @return 데이터 셋 이름을 키로 하고 키워드 리스트를 값으로 갖는 맵
     * @throws IOException 파일 읽기 오류 발생 시 예외 처리
     */

    public static Map<String, List<String>> parseDataSetAndKewordFile(MultipartFile file) throws IOException {
        // 데이터 셋과 해당 키워드를 저장할 맵 생성
        Map<String, List<String>> data = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            for (Sheet sheet : workbook) {
                // 시트의 첫 번째 행(헤더 행)을 가져옵니다.
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) continue; // 헤더 행이 없으면 다음 시트로 넘어갑니다.

                // 헤더 행의 각 셀을 순회합니다.(dataSet 목록들)
                for (Cell headerCell : headerRow) {
                    String dataset = headerCell.getStringCellValue();
                    // 해당 데이터 셋의 키워드를 저장할 리스트를 생성합니다.
                    List<String> keywords = new ArrayList<>();

                    // 헤더 행 이후의 각 행을 순회하며 키워드를 추출합니다.
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row != null) {
                            Cell cell = row.getCell(headerCell.getColumnIndex());
                            if (cell != null && cell.getCellType() == CellType.STRING) {
                                // 셀이 null이 아니고 문자열 타입인 경우 키워드 리스트에 추가합니다.
                                keywords.add(cell.getStringCellValue());
                            }
                        }
                    }
                    data.put(dataset, keywords);
                }
            }
        }
        return data;
    }

    public static Map<String, List<String>> parseQnaFile(MultipartFile file) throws IOException {
        Map<String, List<String>> data = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            for (Sheet sheet: workbook) {
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) continue;

                for (Cell headerCell: headerRow) {
                    String standard = headerCell.getStringCellValue();
                    List<String> question = new ArrayList<>();

                        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                            Row row = sheet.getRow(i);
                            if (row != null) {
                                Cell cell = row.getCell(headerCell.getColumnIndex());
                                if (cell != null && cell.getCellType() == CellType.STRING) {
                                    question.add(cell.getStringCellValue());
                                }
                            }
                        }
                        data.put(standard, question);
                }
            }
        }
        return data;
    }
}
