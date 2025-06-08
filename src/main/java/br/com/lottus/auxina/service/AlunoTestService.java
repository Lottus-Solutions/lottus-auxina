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
public class AlunoTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    // --- IDs DE RECURSOS ---
    // Turmas
    private static final String TURMA_ID_1 = "1";
    private static final String TURMA_ID_2 = "2";
    private static final String TURMA_ID_INEXISTENTE = "99999";

    // Alunos (IDs de Matrícula)
    // Usados em EmprestimoTestService (NÃO DEVEM SER REMOVIDOS COM SUCESSO)
    private static final String ALUNO_ID_1_CARLOS = "1";
    private static final String ALUNO_ID_2_FERNANDA = "2";
    private static final String ALUNO_ID_3_RICARDO = "3";
    private static final String ALUNO_ID_4_MARIANA = "4";
    private static final String ALUNO_ID_5_HIST_VARIOS = "5";

    // Alunos para testes específicos deste módulo
    private static final String ALUNO_ID_6_PARA_EDITAR = "6"; // Um aluno para editar sem impacto
    private static final String ALUNO_ID_7_PARA_REMOVER = "7"; // Aluno limpo, para ser removido com sucesso
    private static final String ALUNO_ID_INEXISTENTE = "99999";

    // --- GRUPOS DE TESTE POR MÉTODO ---
    private static final String GROUP_0_SETUP = "0. Setup";
    private static final String GROUP_1_CADASTRAR = "1. Cadastrar Aluno";
    private static final String GROUP_2_EDITAR = "2. Editar Aluno";
    private static final String GROUP_3_BUSCAR_LISTAR = "3. Buscar e Listar Alunos";
    private static final String GROUP_4_PERFIL = "4. Construir Perfil Aluno";
    private static final String GROUP_5_REMOVER = "5. Remover Aluno";

    public AlunoTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private Mono<TestResult> executeAndLog(TestCaseConfigDTO config) {
        return testExecutionService.executeTest(config)
                .doOnSubscribe(subscription -> log.info("➡️  INICIANDO TESTE DE ALUNO: {}", config.getTestName()));
    }

    private Map<String, Object> getAlunoBody(String nome, String turmaId, int qtdBonus, int qtdLivrosLidos) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        body.put("turmaId", turmaId);
        body.put("qtdBonus", qtdBonus);
        body.put("qtdLivrosLidos", qtdLivrosLidos);
        return body;
    }

    // =================================================================================
    // 0. SETUP - CADASTRO DE ALUNOS BASE
    // =================================================================================

    private TestCaseConfigDTO getConfigSetup_Carlos() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_CarlosAndrade").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Carlos Andrade", TURMA_ID_1, 0, 0)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_Fernanda() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_FernandaLima").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Fernanda Lima", TURMA_ID_1, 0, 0)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_Ricardo() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_RicardoPereira").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Ricardo Pereira", TURMA_ID_1, 0, 0)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_Mariana() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_MarianaCosta").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Mariana Costa", TURMA_ID_1, 0, 0)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_AlunoHistVarios() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_AlunoHistVarios").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Aluno Hist Varios", TURMA_ID_2, 10, 4)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_AlunoParaEditar() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_AlunoParaEditar").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Aluno Original Para Editar", TURMA_ID_2, 0, 0)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_AlunoParaRemover() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_AlunoParaRemover").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Aluno Descartável", TURMA_ID_2, 0, 0)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    // =================================================================================
    // 1. CADASTRAR ALUNO
    // =================================================================================

    private TestCaseConfigDTO getConfigCadastrar_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("Cadastrar_C1_Sucesso").methodGroupKey(GROUP_1_CADASTRAR).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Aluno Novo Sucesso", TURMA_ID_1, 0, 0)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C2_Erro_TurmaInexistente() {
        return TestCaseConfigDTO.builder().testName("Cadastrar_C2_Erro_TurmaInexistente").methodGroupKey(GROUP_1_CADASTRAR).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Aluno Turma Fantasma", TURMA_ID_INEXISTENTE, 0, 0)).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C3_Erro_NomeEmBranco() {
        return TestCaseConfigDTO.builder().testName("Cadastrar_C3_Erro_NomeEmBranco").methodGroupKey(GROUP_1_CADASTRAR).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("", TURMA_ID_1, 0, 0)).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // =================================================================================
    // 2. EDITAR ALUNO
    // =================================================================================

    private TestCaseConfigDTO getConfigEditar_C1_Sucesso() {
        Map<String, Object> body = getAlunoBody("Aluno Editado Com Sucesso", TURMA_ID_1, 5, 10);
        return TestCaseConfigDTO.builder().testName("Editar_C1_Sucesso_TodosOsDados").methodGroupKey(GROUP_2_EDITAR).httpMethod("PUT").endpoint("/alunos/editar/" + ALUNO_ID_6_PARA_EDITAR).requestBodyTemplate(body).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditar_C2_Erro_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Editar_C2_Erro_NaoExiste").methodGroupKey(GROUP_2_EDITAR).httpMethod("PUT").endpoint("/alunos/editar/" + ALUNO_ID_INEXISTENTE).requestBodyTemplate(getAlunoBody("Aluno Fantasma", TURMA_ID_1, 0, 0)).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigEditar_C3_Erro_TurmaInexistente() {
        Map<String, Object> body = getAlunoBody("Fernanda Lima Editada", TURMA_ID_INEXISTENTE, 0, 0);
        return TestCaseConfigDTO.builder().testName("Editar_C3_Erro_TurmaInexistente").methodGroupKey(GROUP_2_EDITAR).httpMethod("PUT").endpoint("/alunos/editar/" + ALUNO_ID_2_FERNANDA).requestBodyTemplate(body).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(404).build();
    }

    // =================================================================================
    // 3. BUSCAR E LISTAR ALUNOS
    // =================================================================================

    private TestCaseConfigDTO getConfigListar_C1_AlunosDaTurma() {
        return TestCaseConfigDTO.builder().testName("Listar_C1_AlunosDaTurma1").methodGroupKey(GROUP_3_BUSCAR_LISTAR).httpMethod("GET").endpoint("/alunos/turma/" + TURMA_ID_1).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigBuscar_C2_PorMatricula() {
        return TestCaseConfigDTO.builder().testName("Buscar_C2_PorMatricula_Carlos").methodGroupKey(GROUP_3_BUSCAR_LISTAR).httpMethod("GET").endpoint("/alunos/" + ALUNO_ID_1_CARLOS).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigBuscar_C3_PorParteDoNome() {
        return TestCaseConfigDTO.builder().testName("Buscar_C3_PorParteDoNome_Car").methodGroupKey(GROUP_3_BUSCAR_LISTAR).httpMethod("GET").endpoint("/alunos/nome/Car").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // =================================================================================
    // 4. CONSTRUIR PERFIL ALUNO
    // =================================================================================

    private TestCaseConfigDTO getConfigPerfil_C1_SemEmprestimos() {
        return TestCaseConfigDTO.builder().testName("Perfil_C1_Ricardo_SemEmprestimos").methodGroupKey(GROUP_4_PERFIL).httpMethod("GET").endpoint("/alunos/perfil/" + ALUNO_ID_3_RICARDO).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigPerfil_C2_ComEmprestimoAtivo() {
        return TestCaseConfigDTO.builder().testName("Perfil_C2_Carlos_ComEmprestimoAtivo").methodGroupKey(GROUP_4_PERFIL).httpMethod("GET").endpoint("/alunos/perfil/" + ALUNO_ID_1_CARLOS).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigPerfil_C4_Erro_AlunoInexistente() {
        return TestCaseConfigDTO.builder().testName("Perfil_C4_Erro_AlunoInexistente").methodGroupKey(GROUP_4_PERFIL).httpMethod("GET").endpoint("/alunos/perfil/" + ALUNO_ID_INEXISTENTE).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    // =================================================================================
    // 5. REMOVER ALUNO
    // =================================================================================

    private TestCaseConfigDTO getConfigRemover_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("Remover_C1_Sucesso_SemDependencias").methodGroupKey(GROUP_5_REMOVER).httpMethod("DELETE").endpoint("/alunos/remover/" + ALUNO_ID_7_PARA_REMOVER).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigRemover_C2_Erro_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Remover_C2_Erro_NaoExiste").methodGroupKey(GROUP_5_REMOVER).httpMethod("DELETE").endpoint("/alunos/remover/" + ALUNO_ID_INEXISTENTE).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRemover_C3_Erro_ComEmprestimosAtivos() {
        // Tenta remover Carlos (ID 1), que tem empréstimos. A remoção DEVE falhar.
        return TestCaseConfigDTO.builder().testName("Remover_C3_ComEmprestimosAtivos").methodGroupKey(GROUP_5_REMOVER).httpMethod("DELETE").endpoint("/alunos/remover/" + ALUNO_ID_1_CARLOS).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigRemover_C4_Erro_JaRemovido() {
        // Tenta remover o mesmo aluno do C1 novamente.
        return TestCaseConfigDTO.builder().testName("Remover_C4_Erro_JaRemovido").methodGroupKey(GROUP_5_REMOVER).httpMethod("DELETE").endpoint("/alunos/remover/" + ALUNO_ID_7_PARA_REMOVER).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    // --- MÉTODO PRINCIPAL DE EXECUÇÃO ---

    public Mono<ModuleTestDTO> runAllAlunoTests() {
        List<TestCaseConfigDTO> testConfigsInOrder = new ArrayList<>();

        // FASE 0: SETUP
        testConfigsInOrder.add(getConfigSetup_Carlos());
        testConfigsInOrder.add(getConfigSetup_Fernanda());
        testConfigsInOrder.add(getConfigSetup_Ricardo());
        testConfigsInOrder.add(getConfigSetup_Mariana());
        testConfigsInOrder.add(getConfigSetup_AlunoHistVarios());
        testConfigsInOrder.add(getConfigSetup_AlunoParaEditar());
        testConfigsInOrder.add(getConfigSetup_AlunoParaRemover());

        // FASE 1: CADASTRAR ALUNO
        testConfigsInOrder.add(getConfigCadastrar_C1_Sucesso());
        testConfigsInOrder.add(getConfigCadastrar_C2_Erro_TurmaInexistente());
        testConfigsInOrder.add(getConfigCadastrar_C3_Erro_NomeEmBranco());

        // FASE 2: EDITAR ALUNO
        testConfigsInOrder.add(getConfigEditar_C1_Sucesso());
        testConfigsInOrder.add(getConfigEditar_C2_Erro_NaoExiste());
        testConfigsInOrder.add(getConfigEditar_C3_Erro_TurmaInexistente());

        // FASE 3: BUSCAR E LISTAR ALUNOS
        testConfigsInOrder.add(getConfigListar_C1_AlunosDaTurma());
        testConfigsInOrder.add(getConfigBuscar_C2_PorMatricula());
        testConfigsInOrder.add(getConfigBuscar_C3_PorParteDoNome());

        // FASE 4: CONSTRUIR PERFIL DO ALUNO
        testConfigsInOrder.add(getConfigPerfil_C1_SemEmprestimos());
        testConfigsInOrder.add(getConfigPerfil_C2_ComEmprestimoAtivo());
        testConfigsInOrder.add(getConfigPerfil_C4_Erro_AlunoInexistente());

        // FASE 5: REMOVER ALUNO
        testConfigsInOrder.add(getConfigRemover_C3_Erro_ComEmprestimosAtivos());
        testConfigsInOrder.add(getConfigRemover_C1_Sucesso());
        testConfigsInOrder.add(getConfigRemover_C4_Erro_JaRemovido());
        testConfigsInOrder.add(getConfigRemover_C2_Erro_NaoExiste());

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
                            .moduleName("Alunos")
                            .totalTests(totalModuleTests)
                            .sucessfulTests((int) successfulModuleTests)
                            .failedTests(totalModuleTests - (int) successfulModuleTests)
                            .successPercentage(moduleSuccessPercentage)
                            .methodTestsResults(methodSummaries)
                            .build();
                });
    }
}