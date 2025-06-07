package br.com.lottus.auxina.service;

import br.com.lottus.auxina.dto.MethodTestDTO;
import br.com.lottus.auxina.dto.ModuleTestDTO;
import br.com.lottus.auxina.dto.ScenarioType;
import br.com.lottus.auxina.dto.TestCaseConfigDTO;
import br.com.lottus.auxina.dto.TestResult;
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
public class TurmaTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    // IDs de Recursos
    private static final String TURMA_ID_1_COM_ALUNOS = "1";
    private static final String TURMA_ID_2_COM_ALUNOS = "2";
    private static final String TURMA_ID_3_PARA_REMOVER = "3";
    private static final String TURMA_ID_INEXISTENTE = "99999";
    private static final String TURMA_ID_FORMATO_INVALIDO = "abc";

    // Grupos de Teste
    private static final String GROUP_0_SETUP = "0. Setup";
    private static final String GROUP_1_CADASTRAR = "1. Cadastrar Turma";
    private static final String GROUP_2_LISTAR = "2. Listar Turmas";
    private static final String GROUP_3_EDITAR = "3. Editar Turma";
    private static final String GROUP_4_REMOVER = "4. Remover Turma";

    public TurmaTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private Mono<TestResult> executeAndLog(TestCaseConfigDTO config) {
        return testExecutionService.executeTest(config)
                .doOnSubscribe(subscription -> log.info("➡️  INICIANDO TESTE DE TURMA: {}", config.getTestName()));
    }

    private Map<String, Object> getTurmaBody(String serie) {
        Map<String, Object> body = new HashMap<>();
        body.put("serie", serie);
        return body;
    }

    // =================================================================================
    // 0. SETUP
    // =================================================================================

    private TestCaseConfigDTO getConfigSetup_Turma1() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_1AnoA").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody("1 Ano A - Matutino")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_Turma2() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_2AnoB").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody("2 Ano B - Vespertino")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_TurmaParaRemover() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_TurmaParaRemover").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody("Turma Removível")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    /**
     * NOVO MÉTODO: Cria a dependência (um aluno) necessária para o teste de remoção falhar.
     * Este é um passo de setup específico para um cenário de teste.
     */
    private TestCaseConfigDTO getConfigSetup_CriarAlunoNaTurma1() {
        Map<String, Object> alunoBody = new HashMap<>();
        alunoBody.put("nome", "Aluno Teste de Dependencia");
        alunoBody.put("turmaId", TURMA_ID_1_COM_ALUNOS);
        alunoBody.put("qtdBonus", 0);
        alunoBody.put("qtdLivrosLidos", 0);

        return TestCaseConfigDTO.builder().testName("Setup_CriarAlunoNaTurma1_ParaTesteDeRemocao").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(alunoBody).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    // =================================================================================
    // 1. CADASTRAR TURMA
    // =================================================================================

    private TestCaseConfigDTO getConfigCadastrar_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("Cadastrar_C1_Sucesso").methodGroupKey(GROUP_1_CADASTRAR).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody("3 Ano C - Noturno")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C2_Erro_SerieEmBranco() {
        return TestCaseConfigDTO.builder().testName("Cadastrar_C2_Erro_SerieEmBranco").methodGroupKey(GROUP_1_CADASTRAR).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody("")).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C3_Erro_SerieJaExiste() {
        return TestCaseConfigDTO.builder().testName("Cadastrar_C3_Erro_SerieJaExiste").methodGroupKey(GROUP_1_CADASTRAR).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody("1 Ano A - Matutino")).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(409).build();
    }

    // =================================================================================
    // 2. LISTAR TURMAS
    // =================================================================================

    private TestCaseConfigDTO getConfigListar_C1_AposSetup() {
        return TestCaseConfigDTO.builder().testName("Listar_C1_AposSetup").methodGroupKey(GROUP_2_LISTAR).httpMethod("GET").endpoint("/turmas").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListar_C2_AposRemocao() {
        return TestCaseConfigDTO.builder().testName("Listar_C2_AposRemocao").methodGroupKey(GROUP_2_LISTAR).httpMethod("GET").endpoint("/turmas").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // =================================================================================
    // 3. EDITAR TURMA
    // =================================================================================

    private TestCaseConfigDTO getConfigEditar_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("Editar_C1_Sucesso").methodGroupKey(GROUP_3_EDITAR).httpMethod("PUT").endpoint("/turmas/" + TURMA_ID_2_COM_ALUNOS).requestBodyTemplate(getTurmaBody("2 Ano B - Integral")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditar_C2_Erro_IdNaoExiste() {
        return TestCaseConfigDTO.builder().testName("Editar_C2_Erro_IdNaoExiste").methodGroupKey(GROUP_3_EDITAR).httpMethod("PUT").endpoint("/turmas/" + TURMA_ID_INEXISTENTE).requestBodyTemplate(getTurmaBody("Turma Fantasma")).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigEditar_C3_Erro_SerieJaExiste() {
        return TestCaseConfigDTO.builder().testName("Editar_C3_Erro_SerieJaExiste").methodGroupKey(GROUP_3_EDITAR).httpMethod("PUT").endpoint("/turmas/" + TURMA_ID_2_COM_ALUNOS).requestBodyTemplate(getTurmaBody("1 Ano A - Matutino")).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(409).build();
    }

    // =================================================================================
    // 4. REMOVER TURMA
    // =================================================================================

    private TestCaseConfigDTO getConfigRemover_C1_Sucesso_SemAlunos() {
        return TestCaseConfigDTO.builder().testName("Remover_C1_Sucesso_SemAlunos").methodGroupKey(GROUP_4_REMOVER).httpMethod("DELETE").endpoint("/turmas/" + TURMA_ID_3_PARA_REMOVER).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(204).build();
    }
    private TestCaseConfigDTO getConfigRemover_C2_Erro_ComAlunos() {
        return TestCaseConfigDTO.builder().testName("Remover_C2_Erro_ComAlunos").methodGroupKey(GROUP_4_REMOVER).httpMethod("DELETE").endpoint("/turmas/" + TURMA_ID_1_COM_ALUNOS).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(500).build();
    }
    private TestCaseConfigDTO getConfigRemover_C3_Erro_IdNaoExiste() {
        return TestCaseConfigDTO.builder().testName("Remover_C3_Erro_IdNaoExiste").methodGroupKey(GROUP_4_REMOVER).httpMethod("DELETE").endpoint("/turmas/" + TURMA_ID_INEXISTENTE).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRemover_C4_Erro_JaRemovida() {
        return TestCaseConfigDTO.builder().testName("Remover_C4_Erro_JaRemovida").methodGroupKey(GROUP_4_REMOVER).httpMethod("DELETE").endpoint("/turmas/" + TURMA_ID_3_PARA_REMOVER).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    public Mono<ModuleTestDTO> runAllTurmaTests() {
        List<TestCaseConfigDTO> testConfigsInOrder = new ArrayList<>();

        // FASE 0: SETUP INICIAL
        testConfigsInOrder.add(getConfigSetup_Turma1());
        testConfigsInOrder.add(getConfigSetup_Turma2());
        testConfigsInOrder.add(getConfigSetup_TurmaParaRemover());
        testConfigsInOrder.add(getConfigSetup_CriarAlunoNaTurma1());

        // FASE 1: TESTES DE CADASTRO
        testConfigsInOrder.add(getConfigCadastrar_C1_Sucesso());
        testConfigsInOrder.add(getConfigCadastrar_C2_Erro_SerieEmBranco());
        testConfigsInOrder.add(getConfigCadastrar_C3_Erro_SerieJaExiste());

        // FASE 2: TESTES DE LISTAGEM E EDIÇÃO
        testConfigsInOrder.add(getConfigListar_C1_AposSetup());
        testConfigsInOrder.add(getConfigEditar_C1_Sucesso());
        testConfigsInOrder.add(getConfigEditar_C2_Erro_IdNaoExiste());
        testConfigsInOrder.add(getConfigEditar_C3_Erro_SerieJaExiste());

        // FASE 3: TESTES DE REMOÇÃO (FLUXO LÓGICO)
        testConfigsInOrder.add(getConfigRemover_C2_Erro_ComAlunos());
        testConfigsInOrder.add(getConfigRemover_C1_Sucesso_SemAlunos());
        testConfigsInOrder.add(getConfigRemover_C4_Erro_JaRemovida());
        testConfigsInOrder.add(getConfigRemover_C3_Erro_IdNaoExiste());

        // FASE 4: VERIFICAÇÃO FINAL
        testConfigsInOrder.add(getConfigListar_C2_AposRemocao());

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
                            .moduleName("Turmas")
                            .totalTests(totalModuleTests)
                            .sucessfulTests((int) successfulModuleTests)
                            .failedTests(totalModuleTests - (int) successfulModuleTests)
                            .successPercentage(moduleSuccessPercentage)
                            .methodTestsResults(methodSummaries)
                            .build();
                });
    }
}