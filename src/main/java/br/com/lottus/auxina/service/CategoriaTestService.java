package br.com.lottus.auxina.service;

import br.com.lottus.auxina.dto.ScenarioType;
import br.com.lottus.auxina.dto.TestCaseConfigDTO;
import br.com.lottus.auxina.dto.TestResult;
import br.com.lottus.auxina.service.engine.TestExecutionService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class CategoriaTestService {

    private final TestExecutionService testExecutionService;

    public CategoriaTestService(TestExecutionService testExecutionService) {
        this.testExecutionService = testExecutionService;
    }

    public Mono<TestResult> runCadastrarCategoriaSucesso() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nome", "Faker::Book.genre"); // Usando um gerador de gênero de livro como nome de categoria

        TestCaseConfigDTO config = TestCaseConfigDTO.builder()
                .testName("Categoria_Cadastrar_Sucesso")
                .httpMethod("POST")
                .endpoint("/categorias")
                .requestBodyTemplate(requestBody)
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(201)
                .build();
        return testExecutionService.executeTest(config);
    }

    public Mono<TestResult> runCadastrarCategoriaNomeEmBranco() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nome", "Faker::Book.genre"); // Será invalidado

        TestCaseConfigDTO config = TestCaseConfigDTO.builder()
                .testName("Categoria_Cadastrar_Erro_NomeEmBranco")
                .httpMethod("POST")
                .endpoint("/categorias")
                .requestBodyTemplate(requestBody)
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST)
                .expectedHtppStatus(400)
                .build();
        return testExecutionService.executeTest(config);
    }
    // ... outros testes para Categoria
}