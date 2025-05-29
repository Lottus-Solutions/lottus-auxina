package br.com.lottus.auxina.service;

import br.com.lottus.auxina.dto.MethodTestDTO;
import br.com.lottus.auxina.dto.ModuleTestDTO;
import br.com.lottus.auxina.dto.ScenarioType;
import br.com.lottus.auxina.dto.TestCaseConfigDTO;
import br.com.lottus.auxina.dto.TestResult;
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
public class CategoriaTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    public CategoriaTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private TestCaseConfigDTO getConfigCadastrarCategoriaSucesso() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nome", "Faker::Book.genre");

        return TestCaseConfigDTO.builder()
                .testName("Categoria_Cadastrar_Sucesso")
                .methodGroupKey("CadastrarCategoria")
                .httpMethod("POST")
                .endpoint("/categorias")
                .requestBodyTemplate(requestBody)
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(201)
                .build();
    }

    private TestCaseConfigDTO getConfigCadastrarCategoriaNomeEmBranco() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nome", "Faker::Book.genre");

        return TestCaseConfigDTO.builder()
                .testName("Categoria_Cadastrar_Erro_NomeEmBranco")
                .methodGroupKey("CadastrarCategoria")
                .httpMethod("POST")
                .endpoint("/categorias")
                .requestBodyTemplate(requestBody)
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST)
                .expectedHtppStatus(400)
                .build();
    }

    private TestCaseConfigDTO getConfigBuscarCategorias() {
        return TestCaseConfigDTO.builder()
                .testName("Categoria_Listar_Todas")
                .methodGroupKey("ListarCategorias")
                .httpMethod("GET")
                .endpoint("/categorias")
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(200)
                .build();
    }

    private TestCaseConfigDTO getConfigCadastrarCategoriaNomeJaExistente() {
        Map<String, Object> requestBody = new HashMap<>();
        String nomeCategoriaExistente = "Categoria Teste Duplicado";
        requestBody.put("nome", nomeCategoriaExistente);

        return TestCaseConfigDTO.builder()
                .testName("Categoria_Cadastrar_Erro_NomeJaExistente")
                .methodGroupKey("CadastrarCategoria")
                .httpMethod("POST")
                .endpoint("/categorias")
                .requestBodyTemplate(requestBody)
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(409)
                .build();
    }

    public Mono<ModuleTestDTO> runAllCategoriaTests() {
        List<Mono<TestResult>> testMonos = new ArrayList<>();

        testMonos.add(testExecutionService.executeTest(getConfigCadastrarCategoriaSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarCategoriaNomeEmBranco()));
        testMonos.add(testExecutionService.executeTest(getConfigBuscarCategorias()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarCategoriaNomeJaExistente()));

        return Flux.mergeSequential(testMonos)
                .collectList()
                .map(allIndividualResults -> {
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
                                .successTests((int) successfulInGroup)
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
                            .moduleName("Categorias")
                            .totalTests(totalModuleTests)
                            .sucessfulTests((int) successfulModuleTests)
                            .failedTests(totalModuleTests - (int) successfulModuleTests)
                            .successPercentage(moduleSuccessPercentage)
                            .methodTestsResults(methodSummaries)
                            .build();
                });
    }
}