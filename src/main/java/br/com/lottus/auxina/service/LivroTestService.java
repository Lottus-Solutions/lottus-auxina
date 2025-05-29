package br.com.lottus.auxina.service;

import br.com.lottus.auxina.dto.*;
import br.com.lottus.auxina.service.engine.TestExecutionService;
import com.github.javafaker.Faker;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LivroTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    public LivroTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private TestCaseConfigDTO getConfigCadastrarLivroSucesso() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nome", "Faker::Book.title");
        requestBody.put("autor", "Faker::Name.fullName");
        requestBody.put("quantidade", "Faker::Number.numberBetween(1,100)");
        requestBody.put("categoriaId", "Faker::Number.numberBetween(1,5)");
        requestBody.put("descricao", "Faker::Lorem.sentence(10,5)");
        requestBody.put("preco", "Faker::Number.randomDouble(2,10,200)");

        return TestCaseConfigDTO.builder()
                .testName("Livro_Cadastrar_Sucesso")
                .methodGroupKey("Cadastrar_Livro")
                .httpMethod("POST")
                .endpoint("/livros")
                .requestBodyTemplate(requestBody)
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(201)
                .build();
    }

    private TestCaseConfigDTO getConfigCadastrarLivroNomeEmBranco() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nome", "Faker::Book.title");
        requestBody.put("autor", "Faker::Name.fullName");
        requestBody.put("quantidade", "Faker::Number.numberBetween(1,100)");
        requestBody.put("categoriaId", "1");
        requestBody.put("descricao", "Faker::Lorem.sentence(10,5)");
        requestBody.put("preco", "Faker::Number.randomDouble(2,10,200)");

        return TestCaseConfigDTO.builder()
                .testName("Livro_Cadastrar_Erro_NomeEmBranco")
                .methodGroupKey("Cadastrar Livro")
                .httpMethod("POST")
                .endpoint("/livros")
                .requestBodyTemplate(requestBody)
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST)
                .expectedHtppStatus(400)
                .build();
    }

    private TestCaseConfigDTO getConfigBuscarLivrosPaginado() {
        String paginaValue = String.valueOf(faker.number().numberBetween(0, 5));
        String tamanhoValue = String.valueOf(faker.number().numberBetween(5, 15));

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pagina", paginaValue);
        queryParams.put("tamanho", tamanhoValue);

        return TestCaseConfigDTO.builder()
                .testName("Livro_Buscar_Paginado")
                .methodGroupKey("Buscar Livros")
                .httpMethod("GET")
                .endpoint("/livros")
                .queryParamsTemplate(queryParams)
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(200)
                .build();
    }

    private TestCaseConfigDTO getConfigBuscarLivroPorIdExistente() {
        String livroIdParaBuscar = "1";

        return TestCaseConfigDTO.builder()
                .testName("Livro_Buscar_PorId_Existente")
                .methodGroupKey("Buscar Livro Por Id")
                .httpMethod("GET")
                .endpoint("/livros/" + livroIdParaBuscar)
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(200)
                .build();
    }

    private TestCaseConfigDTO getConfigBuscarLivroPorIdNaoExistente() {
        String idNaoExistente = String.valueOf(faker.number().randomNumber(7, true) + 9000000L);

        return TestCaseConfigDTO.builder()
                .testName("Livro_Buscar_PorId_NaoExistente")
                .methodGroupKey("Buscar Livro Por Id")
                .httpMethod("GET")
                .endpoint("/livros/" + idNaoExistente)
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND)
                .expectedHtppStatus(404)
                .build();
    }

    public Mono<ModuleTestDTO> runAllLivroTests() {

        List<Mono<TestResult>> testMonos = new ArrayList<>();
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarLivroSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarLivroNomeEmBranco()));
        testMonos.add(testExecutionService.executeTest(getConfigBuscarLivrosPaginado()));
        testMonos.add(testExecutionService.executeTest(getConfigBuscarLivroPorIdExistente()));
        testMonos.add(testExecutionService.executeTest(getConfigBuscarLivroPorIdNaoExistente()));
        // Adicione outros Monos de teste aqui

        return Flux.mergeSequential(testMonos) // Ou Flux.merge para execução paralela
                .collectList()
                .map(allIndividualResults -> {

                    // Agrupar resultados por methodGroupKey
                    Map<String, List<TestResult>> groupedByMethod = allIndividualResults.stream()
                            .filter(tr -> tr.getMethodGroupKey() != null)
                            .collect(Collectors.groupingBy(TestResult::getMethodGroupKey));

                    List<MethodTestDTO> methodSummaries = new ArrayList<>();
                    for (Map.Entry<String, List<TestResult>> entry : groupedByMethod.entrySet()) {
                        String methodGroupKey = entry.getKey();
                        List<TestResult> testsInGroup = entry.getValue();

                        int totalInGroup = testsInGroup.size();
                        long successfulInGroup = testsInGroup.stream().filter(TestResult::isSuccess).count();
                        double avgDuration = testsInGroup.stream()
                                .mapToLong(TestResult::getDurationMillis)
                                .average().orElse(0.0);
                        Double avgMemory = testsInGroup.stream()
                                .filter(tr -> tr.getTargetServiceMemoryUsedMB() != null)
                                .mapToDouble(TestResult::getTargetServiceMemoryUsedMB)
                                .average().orElse(0.0);

                        methodSummaries.add(MethodTestDTO.builder()
                                .methodName(methodGroupKey)
                                .totalTests(totalInGroup)
                                .successTests((int)successfulInGroup)
                                .failedTests(totalInGroup - (int) successfulInGroup)
                                .avarageDurationMillisInGroup(avgDuration)
                                .avarageMemoryUsageMbInGroup(avgMemory)
                                .individualTestResults(testsInGroup)
                                .build());
                    }

                    int totalModuleTests = allIndividualResults.size();
                    long successfulModuleTests = allIndividualResults.stream().filter(TestResult::isSuccess).count();
                    double moduleSuccessPercentage = (totalModuleTests > 0) ? ((double) successfulModuleTests / totalModuleTests) * 100.0 : 0.0;


                    return ModuleTestDTO.builder()
                            .moduleName("Livros")
                            .totalTests(totalModuleTests)
                            .sucessfulTests((int) successfulModuleTests) // Lembre-se do typo 'sucessfulTests' no seu DTO
                            .failedTests(totalModuleTests - (int) successfulModuleTests)
                            .successPercentage(moduleSuccessPercentage)
                            .methodTestsResults(methodSummaries)
                            .build();
                });
    }
}