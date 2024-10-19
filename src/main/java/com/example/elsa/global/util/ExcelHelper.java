package com.example.elsa.global.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

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
				if (headerRow == null)
					continue; // 헤더 행이 없으면 다음 시트로 넘어갑니다.

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

		try (InputStream is = file.getInputStream();
			 Workbook workbook = new XSSFWorkbook(is)) {

			for (Sheet sheet : workbook) {
				String standardName = sheet.getRow(0).getCell(0).getStringCellValue().trim();
				List<String> qnaList = new ArrayList<>();

				Row questionRow = sheet.getRow(1);
				Row answerRow = sheet.getRow(2);

				if (questionRow != null && answerRow != null) {
					for (int i = 0; i < questionRow.getLastCellNum(); i++) {
						Cell questionCell = questionRow.getCell(i);
						Cell answerCell = answerRow.getCell(i);

						if (questionCell != null) {
							String question = getCellValueAsString(questionCell);
							String answer = (answerCell != null) ? getCellValueAsString(answerCell) : "";

							qnaList.add(question);
							qnaList.add(answer);
						}
					}
				}

				data.put(standardName, qnaList);
			}
		}

		return data;
	}

	public static Map<String, List<String>> parseDiagnosisQnaFile(MultipartFile file) throws IOException {
		Map<String, List<String>> data = new LinkedHashMap<>();

		try (InputStream is = file.getInputStream();
			 Workbook workbook = new XSSFWorkbook(is)) {

			Sheet sheet = workbook.getSheetAt(0); // Assuming we're working with the first sheet

			Row headerRow = sheet.getRow(0);
			int columnCount = headerRow.getLastCellNum();

			for (int i = 0; i < columnCount; i++) {
				Cell headerCell = headerRow.getCell(i);
				if (headerCell != null) {
					String standardName = getCellValueAsString(headerCell);
					List<String> questions = new ArrayList<>();

					// Iterate through rows for this column
					for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
						Row row = sheet.getRow(rowNum);
						if (row != null) {
							Cell cell = row.getCell(i);
							if (cell != null) {
								String question = getCellValueAsString(cell);
								if (!question.isEmpty()) {
									questions.add(question);
								}
							}
						}
					}

					if (!questions.isEmpty()) {
						data.put(standardName, questions);
					}
				}
			}
		}

		return data;
	}

	private static String getCellValueAsString(Cell cell) {
		if (cell == null) {
			return "";
		}
		switch (cell.getCellType()) {
			case STRING:
				return cell.getStringCellValue().trim();
			case NUMERIC:
				return String.valueOf(cell.getNumericCellValue()).trim();
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue()).trim();
			default:
				return "";
		}
	}
}
