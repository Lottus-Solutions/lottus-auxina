package br.com.lottus.auxina.service;

import br.com.lottus.auxina.dto.ScenarioType;
import br.com.lottus.auxina.dto.TestCaseConfigDTO;
import br.com.lottus.auxina.dto.TestResult;
import br.com.lottus.auxina.service.engine.TestExecutionService; // Importa o motor
import com.github.javafaker.Faker; // Pode precisar para alguma lógica de dados específica aqui
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class LivroTestService {

    private final TestExecutionService testExecutionService; // Injeta o motor
    private final Faker faker; // Injeta o Faker se for usar aqui também

    public LivroTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    public Mono<TestResult> runCadastrarLivroSucesso() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nome", "Faker::Book.title");
        requestBody.put("autor", "Faker::Name.fullName");
        requestBody.put("quantidade", "Faker::Number.numberBetween(1,100)");
        requestBody.put("categoriaId", "Faker::Number.numberBetween(1,5)"); // Supondo que você tem um teste de categoria ou IDs fixos
        requestBody.put("descricao", "Faker::Lorem.sentence(10,5)");
        requestBody.put("preco", "Faker::Number.randomDouble(2,10,200)");


        TestCaseConfigDTO config = TestCaseConfigDTO.builder()
                .testName("Livro_Cadastrar_Sucesso")
                .httpMethod("POST")
                .endpoint("/livros")
                .requestBodyTemplate(requestBody)
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(201)
                .build();

        return testExecutionService.executeTest(config); // Delega a execução
    }

    public Mono<TestResult> runCadastrarLivroNomeEmBranco() {
        Map<String, Object> requestBody = new HashMap<>();
        // O template define 'nome', mas applyScenarioSpecificModifications o tornará inválido
        requestBody.put("nome", "Faker::Book.title");
        requestBody.put("autor", "Faker::Name.fullName");
        requestBody.put("quantidade", "Faker::Number.numberBetween(1,100)");
        requestBody.put("categoriaId", "1"); // Exemplo, poderia ser do Faker
        requestBody.put("descricao", "Faker::Lorem.sentence(10,5)");
        requestBody.put("preco", "Faker::Number.randomDouble(2,10,200)");


        TestCaseConfigDTO config = TestCaseConfigDTO.builder()
                .testName("Livro_Cadastrar_Erro_NomeEmBranco")
                .httpMethod("POST")
                .endpoint("/livros")
                .requestBodyTemplate(requestBody)
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST)
                .expectedHtppStatus(400) // Supondo que nome em branco retorna 400
                .build();

        return testExecutionService.executeTest(config);
    }

    public Mono<TestResult> runBuscarLivrosPaginado() {
        final String testName = "Livro_Buscar_Paginado";
        final String targetEndpoint = "/livros";

        // Usando Faker diretamente aqui para simplicidade, mas poderia ser placeholder
        String paginaValue = String.valueOf(faker.number().numberBetween(0, 5));
        String tamanhoValue = String.valueOf(faker.number().numberBetween(5, 15));

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pagina", paginaValue);
        queryParams.put("tamanho", tamanhoValue);

        TestCaseConfigDTO config = TestCaseConfigDTO.builder()
                .testName(testName)
                .httpMethod("GET")
                .endpoint(targetEndpoint)
                .queryParamsTemplate(queryParams)
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(200)
                .build();

        return testExecutionService.executeTest(config);
    }

    // ... outros métodos de teste para Livro (buscar por ID, atualizar, deletar, etc.)
}