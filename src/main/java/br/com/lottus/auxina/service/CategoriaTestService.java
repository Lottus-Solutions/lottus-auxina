package br.com.lottus.auxina.service;

import br.com.lottus.auxina.dto.*;
import br.com.lottus.auxina.service.engine.TestExecutionService;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CategoriaTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    // --- IDs de Recursos ---
    // Categorias que podem ter livros associados por outros módulos
    private static final String CATEGORIA_ID_1_AVENTURA = "1";
    private static final String CATEGORIA_ID_2_CIENCIA = "2";

    // Categoria para testes de edição e remoção bem-sucedida (sem dependências)
    private static final String CATEGORIA_ID_3_PARA_EDITAR_E_REMOVER = "3";

    // IDs para cenários de erro
    private static final String CATEGORIA_ID_INEXISTENTE = "999";

    // --- GRUPOS DE TESTE POR MÉTODO ---
    private static final String GROUP_0_SETUP = "0. Setup";
    private static final String GROUP_1_ADICIONAR = "1. Adicionar Categoria";
    private static final String GROUP_2_LISTAR = "2. Listar Categorias";
    private static final String GROUP_3_EDITAR = "3. Editar Categoria";
    private static final String GROUP_4_REMOVER = "4. Remover Categoria";

    public CategoriaTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private Mono<TestResult> executeAndLog(TestCaseConfigDTO config) {
        return testExecutionService.executeTest(config)
                .doOnSubscribe(subscription -> log.info("➡️  INICIANDO TESTE DE CATEGORIA: {}", config.getTestName()));
    }

    private Map<String, Object> getCategoriaBody(String nome, String cor) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        if (cor != null) {
            body.put("cor", cor);
        }
        return body;
    }

    // =================================================================================
    // 0. SETUP
    // =================================================================================
    private TestCaseConfigDTO getConfigSetup_Aventura() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_Aventura").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("Aventura", "#FFA500")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_Ciencia() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_Ciencia").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("Ciência", "#0000FF")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_ParaEditarERemover() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_ParaEditarERemover").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("História", "#FFD700")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    // =================================================================================
    // 1. ADICIONAR CATEGORIA
    // =================================================================================
    private TestCaseConfigDTO getConfigAdicionar_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("Adicionar_C1_Sucesso_Fantasia").methodGroupKey(GROUP_1_ADICIONAR).httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("Fantasia", "#8A2BE2")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigAdicionar_C2_Erro_NomeJaExistente() {
        return TestCaseConfigDTO.builder().testName("Adicionar_C2_Erro_NomeJaExistente").methodGroupKey(GROUP_1_ADICIONAR).httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("Aventura", null)).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigAdicionar_C3_Erro_NomeEmBranco() {
        return TestCaseConfigDTO.builder().testName("Adicionar_C3_Erro_NomeEmBranco").methodGroupKey(GROUP_1_ADICIONAR).httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("", null)).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // =================================================================================
    // 2. LISTAR CATEGORIAS
    // =================================================================================
    private TestCaseConfigDTO getConfigListar_AposSetup() {
        return TestCaseConfigDTO.builder().testName("Listar_AposSetup").methodGroupKey(GROUP_2_LISTAR).httpMethod("GET").endpoint("/categorias").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListar_AposRemocao() {
        return TestCaseConfigDTO.builder().testName("Listar_AposRemocao").methodGroupKey(GROUP_2_LISTAR).httpMethod("GET").endpoint("/categorias").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // =================================================================================
    // 3. EDITAR CATEGORIA
    // =================================================================================
    private TestCaseConfigDTO getConfigEditar_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("Editar_C1_Sucesso_MudarNomeECor").methodGroupKey(GROUP_3_EDITAR).httpMethod("PUT").endpoint("/categorias/" + CATEGORIA_ID_3_PARA_EDITAR_E_REMOVER).requestBodyTemplate(getCategoriaBody("Biografia", "#008000")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditar_C2_Erro_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Editar_C2_Erro_NaoExiste").methodGroupKey(GROUP_3_EDITAR).httpMethod("PUT").endpoint("/categorias/" + CATEGORIA_ID_INEXISTENTE).requestBodyTemplate(getCategoriaBody("Qualquer Nome", null)).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigEditar_C3_Erro_NomeJaExistente() {
        return TestCaseConfigDTO.builder().testName("Editar_C3_Erro_NomeJaExistente").methodGroupKey(GROUP_3_EDITAR).httpMethod("PUT").endpoint("/categorias/" + CATEGORIA_ID_3_PARA_EDITAR_E_REMOVER).requestBodyTemplate(getCategoriaBody("Aventura", null)).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(500).build();
    }

    // =================================================================================
    // 4. REMOVER CATEGORIA
    // =================================================================================
    private TestCaseConfigDTO getConfigRemover_C1_Sucesso_SemLivros() {
        return TestCaseConfigDTO.builder().testName("Remover_C1_Sucesso_CategoriaLimpa").methodGroupKey(GROUP_4_REMOVER).httpMethod("DELETE").endpoint("/categorias/" + CATEGORIA_ID_3_PARA_EDITAR_E_REMOVER).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(204).build();
    }
    private TestCaseConfigDTO getConfigRemover_C3_Erro_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Remover_C3_Erro_NaoExiste").methodGroupKey(GROUP_4_REMOVER).httpMethod("DELETE").endpoint("/categorias/" + CATEGORIA_ID_INEXISTENTE).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRemover_C4_Erro_JaRemovida() {
        return TestCaseConfigDTO.builder().testName("Remover_C4_Erro_JaRemovida").methodGroupKey(GROUP_4_REMOVER).httpMethod("DELETE").endpoint("/categorias/" + CATEGORIA_ID_3_PARA_EDITAR_E_REMOVER).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }


    public Mono<ModuleTestDTO> runAllCategoriaTests() {
        List<TestCaseConfigDTO> testConfigsInOrder = new ArrayList<>();

        // FASE 0: SETUP
        testConfigsInOrder.add(getConfigSetup_Aventura());
        testConfigsInOrder.add(getConfigSetup_Ciencia());
        testConfigsInOrder.add(getConfigSetup_ParaEditarERemover());

        // FASE 1: ADICIONAR
        testConfigsInOrder.add(getConfigAdicionar_C1_Sucesso());
        testConfigsInOrder.add(getConfigAdicionar_C2_Erro_NomeJaExistente());
        testConfigsInOrder.add(getConfigAdicionar_C3_Erro_NomeEmBranco());

        // FASE 2: LISTAR
        testConfigsInOrder.add(getConfigListar_AposSetup());

        // FASE 3: EDITAR
        testConfigsInOrder.add(getConfigEditar_C1_Sucesso());
        testConfigsInOrder.add(getConfigEditar_C2_Erro_NaoExiste());
        testConfigsInOrder.add(getConfigEditar_C3_Erro_NomeJaExistente());

        // FASE 4: REMOVER
        testConfigsInOrder.add(getConfigRemover_C1_Sucesso_SemLivros());
        testConfigsInOrder.add(getConfigRemover_C4_Erro_JaRemovida());
        testConfigsInOrder.add(getConfigRemover_C3_Erro_NaoExiste());

        // FASE 5: VERIFICAÇÃO FINAL
        testConfigsInOrder.add(getConfigListar_AposRemocao());

        return Flux.fromIterable(testConfigsInOrder)
                .concatMap(this::executeAndLog)
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
                        double avgDuration = testsInGroup.stream().mapToLong(TestResult::getDurationMillis).average().orElse(0.0);
                        Double avgMemory = testsInGroup.stream().filter(tr -> tr.getTargetServiceMemoryUsedMB() != null).mapToDouble(TestResult::getTargetServiceMemoryUsedMB).average().orElse(0.0);

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