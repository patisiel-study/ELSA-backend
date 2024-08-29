package com.example.elsa.domain.qna.controller;

import com.example.elsa.domain.qna.dto.QnaToDeleteRequest;
import com.example.elsa.domain.qna.dto.QnaToStandardDto;
import com.example.elsa.domain.qna.dto.StandardDto;
import com.example.elsa.domain.qna.entity.QnaSet;
import com.example.elsa.domain.qna.enums.LLMModel;
import com.example.elsa.domain.qna.service.AnswerService;
import com.example.elsa.domain.qna.service.StandardService;
import com.example.elsa.global.util.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Tag(name = "스탠다드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/standard")
public class StandardController {
    private final StandardService standardService;
    private final AnswerService answerService;

    @Operation(summary = "모든 스탠다드의 답변들에 대한 점수 반환")
    @GetMapping("/analyze/sentiments")
    public ResponseEntity<ResponseDto<Map<String, Double>>> analyzeAllStandardQnaSentiments() {
        Map<String, Double> sentimentScores = standardService.analyzeAllStandardQnaSentiments();
        return ResponseEntity.ok(new ResponseDto<>("해당 모델에 대한 윤리 평가가 완료되었습니다.", sentimentScores));
    }

    @Operation(summary = "엑셀 파일 업로드를 통해 Q&A 생성")
    @PostMapping(value = "/upload/qna", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<?>> uploadQna(@RequestPart("file") MultipartFile file, @RequestParam LLMModel model) {
        standardService.uploadAndProcessQna(file, model);
        return ResponseEntity.ok(new ResponseDto<>("Q&A 업로드가 완료되었습니다.", null));
    }

    @Operation(summary = "스탠다드 생성")
    @PostMapping("/create/standard")
    public ResponseEntity<ResponseDto<?>> addStandard(@RequestBody StandardDto standardDto) {
        standardService.addStandard(standardDto);
        return ResponseEntity.ok(new ResponseDto<>("스탠다드 추가가 완료되었습니다.", null));
    }

    @Operation(summary = "질문/답변 추가")
    @PostMapping("/create/qna")
    public ResponseEntity<ResponseDto<?>> addQna(@RequestBody QnaToStandardDto qnaToStandardDto, @RequestParam LLMModel model) {
        CompletableFuture<Void> future = standardService.addQnaToStandard(qnaToStandardDto, model);
        future.join(); // CompletableFuture의 결과를 기다림
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


    @Operation(summary = "선택한 LLM 모델로 질문에 대한 답변 얻기")
    @PostMapping("/answer")
    public ResponseEntity<ResponseDto<String>> getAnswerFromLLM(@RequestBody String question, @RequestParam LLMModel model) {
        String answer = answerService.getAnswer(question, model).join();
        return ResponseEntity.ok(new ResponseDto<>(model.name() + "의 답변이 생성되었습니다.", answer));
    }

    @Operation(summary = "특정 스탠다드 항목의 선택한 LLM 모델 답변 조회")
    @GetMapping("/{standardName}/list/qna/answers")
    public ResponseEntity<ResponseDto<?>> getQnaAnswersByModel(@PathVariable String standardName, @RequestParam LLMModel model) {
        List<QnaSet> qnaSets = standardService.getAllQnaByStandardName(standardName);
        List<String> answerList = qnaSets.stream()
                .map(qnaSet -> {
                    CompletableFuture<String> answerFuture = answerService.getAnswer(qnaSet.getQuestion(), model);
                    try {
                        return answerFuture.get();
                    } catch (Exception e) {
                        return "Error occurred while getting the answer.";
                    }
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ResponseDto<>(standardName + "의 " + model.name() + " 모델 답변 조회가 완료되었습니다.", answerList));
    }


    /*@Operation(summary = "선택한 LLM 모델로 질문에 대한 답변 얻기")
    @PostMapping("/answer")
    public ResponseEntity<ResponseDto<String>> getAnswerFromLLM(@RequestBody String question, @RequestParam LLMModel model) {
        String answer = answerService.getAnswer(question, model).join();
        return ResponseEntity.ok(new ResponseDto<>(model.name() + "의 답변이 생성되었습니다.", answer));
    }

    @Operation(summary = "특정 스탠다드 항목의 Qna 리스트와 LLM 모델별 답변 조회")
    @GetMapping("/{standardName}/list/qna/with-answers")
    public ResponseEntity<ResponseDto<?>> getQnaListWithAnswersByStandard(@PathVariable String standardName) {
        List<QnaSet> qnaSets = standardService.getAllQnaByStandardName(standardName);
        List<Map<String, Object>> qnaList = qnaSets.stream()
                .map(qnaSet -> {
                    Map<String, Object> qnaInfo = new HashMap<>();
                    qnaInfo.put("question", qnaSet.getQuestion());

                    // 각 LLM 모델별로 답변을 비동기적으로 받아와서 qnaInfo에 추가
                    Map<String, CompletableFuture<String>> answerFutures = new HashMap<>();
                    for (LLMModel model : LLMModel.values()) {
                        answerFutures.put(model.name(), answerService.getAnswer(qnaSet.getQuestion(), model));
                    }

                    // 모든 비동기 작업이 완료될 때까지 대기
                    CompletableFuture.allOf(answerFutures.values().toArray(new CompletableFuture[0])).join();

                    // 각 LLM 모델의 답변을 qnaInfo에 추가
                    answerFutures.forEach((modelName, future) -> {
                        try {
                            qnaInfo.put(modelName, future.get());
                        } catch (Exception e) {
                            qnaInfo.put(modelName, "Error occurred while getting the answer.");
                        }
                    });

                    return qnaInfo;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ResponseDto<>(standardName + "의 Qna 리스트와 LLM 모델별 답변 조회가 완료되었습니다.", qnaList));
    }*/

    /*@Operation(summary = "GPT-3.5로 질문에 대한 답변 얻기")
    @PostMapping("/answer/gpt3.5")
    public ResponseEntity<ResponseDto<String>> getAnswerFromGPT3_5(@RequestBody String question) {
        String answer = answerService.getAnswer(question, LLMModel.GPT_3_5).join();
        return ResponseEntity.ok(new ResponseDto<>("GPT-3.5의 답변이 생성되었습니다.", answer));
    }

    @Operation(summary = "GPT-4로 질문에 대한 답변 얻기")
    @PostMapping("/answer/gpt4")
    public ResponseEntity<ResponseDto<String>> getAnswerFromGPT4(@RequestBody String question) {
        String answer = answerService.getAnswer(question, LLMModel.GPT_4).join();
        return ResponseEntity.ok(new ResponseDto<>("GPT-4의 답변이 생성되었습니다.", answer));
    }

    @Operation(summary = "GPT-4o 로 질문에 대한 답변 얻기")
    @PostMapping("/answer/gpt4o")
    public ResponseEntity<ResponseDto<String>> getAnswerFromGPT4o(@RequestBody String question) {
        String answer = answerService.getAnswer(question, LLMModel.GPT_4o).join();
        return ResponseEntity.ok(new ResponseDto<>("GPT-4 Turbo의 답변이 생성되었습니다.", answer));
    }


    @Operation(summary = "Gemini로 질문에 대한 답변 얻기")
    @PostMapping("/answer/gemini")
    public ResponseEntity<ResponseDto<String>> getAnswerFromGemini(@RequestBody String question) {
        String answer = answerService.getAnswer(question, LLMModel.GEMINI).join();
        return ResponseEntity.ok(new ResponseDto<>("Gemini의 답변이 생성되었습니다.", answer));
    }*/
}