package br.com.lottus.auxina.service;

import br.com.lottus.auxina.dto.*;
import br.com.lottus.auxina.service.engine.TestExecutionService;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmprestimoTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    // IDs de Alunos e Livros (consistentes com o setup geral)
    private static final String ALUNO_ID_1_CARLOS = "1";
    private static final String ALUNO_ID_2_FERNANDA = "2";
    private static final String ALUNO_ID_3_RICARDO = "3";
    private static final String ALUNO_ID_4_MARIANA = "4";
    private static final String ALUNO_ID_5_META_LIVROS = "5";
    private static final String ALUNO_ID_INEXISTENTE = "99999";

    private static final String LIVRO_ID_1_REVOLUCAO = "1";
    private static final String LIVRO_ID_2_DOM_CASMURRO = "2";
    private static final String LIVRO_ID_3_SENHOR_ANEIS = "3";
    private static final String LIVRO_ID_4_1984_INDISPONIVEL = "4";
    private static final String LIVRO_ID_6_HOBBIT_ULTIMA_COPIA = "6";
    private static final String LIVRO_ID_INEXISTENTE = "9998";

    // IDs de Empréstimos ASSUMIDOS pela ordem de criação no SETUP
    private static final String EMPRESTIMO_ID_1_FERNANDA = "1"; // C1_Sucesso
    private static final String EMPRESTIMO_ID_2_MARIANA_HOBBIT = "2"; // C5_UltimaCopia
    private static final String EMPRESTIMO_ID_3_CARLOS_ATRASADO = "3"; // Novo Setup
    private static final String EMPRESTIMO_ID_4_RICARDO_RENOVAR = "4"; // Novo Setup
    private static final String EMPRESTIMO_ID_5_ALUNO_META = "5"; // Novo Setup
    private static final String EMPRESTIMO_ID_INEXISTENTE = "999";

    // Grupos de Teste
    private static final String GROUP_SETUP = "0. Setup Empréstimos";
    private static final String GROUP_FAZER_EMPRESTIMO = "1. FazerEmprestimo";
    private static final String GROUP_RENOVAR_EMPRESTIMO = "2. RenovarEmprestimo";
    private static final String GROUP_LISTAR_EMPRESTIMOS = "3. ListarEmprestimos";
    private static final String GROUP_FINALIZAR_EMPRESTIMO = "4. FinalizarEmprestimo";
    private static final String GROUP_HISTORICO = "5. BuscarHistorico";


    public EmprestimoTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private Mono<TestResult> executeAndLog(TestCaseConfigDTO config) {
        return testExecutionService.executeTest(config)
                .doOnSubscribe(subscription -> log.info("➡️  INICIANDO TESTE DE EMPRÉSTIMO: {}", config.getTestName()));
    }

    private Map<String, Object> createBody(String matricula, String livroId, String data) {
        Map<String, Object> body = new HashMap<>();
        body.put("matriculaAluno", matricula);
        body.put("fk_livro", livroId);
        body.put("dataEmprestimo", data);
        return body;
    }

    // =================================================================================
    // 0. SETUP - Cria os empréstimos que serão manipulados pelos outros testes
    // =================================================================================
    private TestCaseConfigDTO getConfigSetup_Emprestimo1_Fernanda() {
        return TestCaseConfigDTO.builder().testName("Setup_Emprestimo1_Fernanda_Revolucao").methodGroupKey(GROUP_SETUP)
                .httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(createBody(ALUNO_ID_2_FERNANDA, LIVRO_ID_1_REVOLUCAO, LocalDate.now().toString()))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_Emprestimo2_Mariana() {
        return TestCaseConfigDTO.builder().testName("Setup_Emprestimo2_Mariana_Hobbit").methodGroupKey(GROUP_SETUP)
                .httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(createBody("9", "5", LocalDate.now().toString()))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_Emprestimo3_Atrasado() {
        // Cria um empréstimo com data de 30 dias atrás para garantir que esteja atrasado
        return TestCaseConfigDTO.builder().testName("Setup_Emprestimo3_Carlos_Atrasado").methodGroupKey(GROUP_SETUP)
                .httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(createBody("8", LIVRO_ID_2_DOM_CASMURRO, LocalDate.now().minusDays(30).toString()))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_Emprestimo4_ParaRenovar() {
        return TestCaseConfigDTO.builder().testName("Setup_Emprestimo4_Ricardo_SenhorAneis").methodGroupKey(GROUP_SETUP)
                .httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(createBody(ALUNO_ID_3_RICARDO, LIVRO_ID_3_SENHOR_ANEIS, LocalDate.now().toString()))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_Emprestimo5_AlunoMeta() {
        return TestCaseConfigDTO.builder().testName("Setup_Emprestimo5_AlunoMeta").methodGroupKey(GROUP_SETUP)
                .httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(createBody(ALUNO_ID_5_META_LIVROS, "1", LocalDate.now().toString()))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }


    // =================================================================================
    // 1. MÉTODO: fazerEmprestimo (Apenas cenários de erro, pois os de sucesso estão no setup)
    // =================================================================================
    private TestCaseConfigDTO getConfigFazer_C2_AlunoJaComEmprestimo() {
        // Fernanda (ID 2) já pegou um livro no setup, esta tentativa deve falhar.
        return TestCaseConfigDTO.builder().testName("FazerEmprestimo_C2_Erro_AlunoJaTem").methodGroupKey(GROUP_FAZER_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(createBody(ALUNO_ID_2_FERNANDA, LIVRO_ID_3_SENHOR_ANEIS, LocalDate.now().toString()))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(409).build();
    }
    private TestCaseConfigDTO getConfigFazer_C3_LivroIndisponivel() {
        return TestCaseConfigDTO.builder().testName("FazerEmprestimo_C3_Erro_LivroIndisponivel").methodGroupKey(GROUP_FAZER_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(createBody("10", LIVRO_ID_4_1984_INDISPONIVEL, LocalDate.now().toString())) // Aluno 10 não tem empréstimo
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigFazer_C4_AlunoInexistente() {
        return TestCaseConfigDTO.builder().testName("FazerEmprestimo_C4_Erro_AlunoInexistente").methodGroupKey(GROUP_FAZER_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(createBody(ALUNO_ID_INEXISTENTE, LIVRO_ID_3_SENHOR_ANEIS, LocalDate.now().toString()))
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    // =================================================================================
    // 2. MÉTODO: renovarEmprestimo
    // =================================================================================
    private TestCaseConfigDTO getConfigRenovar_C1_Sucesso() {
        // Renova o empréstimo da Fernanda, que foi o primeiro a ser criado (ID 1)
        return TestCaseConfigDTO.builder().testName("RenovarEmprestimo_C1_Sucesso").methodGroupKey(GROUP_RENOVAR_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_1_FERNANDA + "/renovar")
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigRenovar_C2_Atrasado() {
        // Renova o empréstimo do Carlos, criado para estar atrasado (ID 3)
        return TestCaseConfigDTO.builder().testName("RenovarEmprestimo_C2_Atrasado").methodGroupKey(GROUP_RENOVAR_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_3_CARLOS_ATRASADO + "/renovar")
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigRenovar_C3_Inexistente() {
        return TestCaseConfigDTO.builder().testName("RenovarEmprestimo_C3_Erro_Inexistente").methodGroupKey(GROUP_RENOVAR_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_INEXISTENTE + "/renovar")
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }


    // =================================================================================
    // 3. MÉTODO: listarEmprestimos
    // =================================================================================
    private TestCaseConfigDTO getConfigListar_C1_SemFiltros() {
        Map<String, String> qp = new HashMap<>(); qp.put("pagina", "0"); qp.put("tamanho", "10");
        return TestCaseConfigDTO.builder().testName("ListarEmprestimos_C1_SemFiltros").methodGroupKey(GROUP_LISTAR_EMPRESTIMOS)
                .httpMethod("GET").endpoint("/emprestimos").queryParamsTemplate(qp)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListar_C3_ApenasAtrasados() {
        Map<String, String> qp = new HashMap<>(); qp.put("pagina", "0"); qp.put("tamanho", "10"); qp.put("atrasados", "true");
        return TestCaseConfigDTO.builder().testName("ListarEmprestimos_C3_ApenasAtrasados").methodGroupKey(GROUP_LISTAR_EMPRESTIMOS)
                .httpMethod("GET").endpoint("/emprestimos").queryParamsTemplate(qp)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // =================================================================================
    // 4. MÉTODO: finalizarEmprestimo
    // =================================================================================
    private TestCaseConfigDTO getConfigFinalizar_C1_AtivoSucesso() {
        // Finaliza o empréstimo da Fernanda (ID 1), que já foi renovado.
        return TestCaseConfigDTO.builder().testName("FinalizarEmprestimo_C1_AtivoSucesso").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_1_FERNANDA + "/finalizar")
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigFinalizar_C2_Atrasado() {
        // Finaliza o empréstimo do Carlos (ID 3), que estava atrasado.
        return TestCaseConfigDTO.builder().testName("FinalizarEmprestimo_C2_Atrasado").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_3_CARLOS_ATRASADO + "/finalizar")
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigFinalizar_C4_DevolucaoUltimaCopia() {
        // Finaliza o empréstimo da Mariana (ID 2), que era a última cópia do Hobbit.
        return TestCaseConfigDTO.builder().testName("FinalizarEmprestimo_C4_DevolucaoUltimaCopia").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_2_MARIANA_HOBBIT + "/finalizar")
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigFinalizar_C5_AlunoAtingeMetaBonus() {
        // Finaliza o empréstimo do aluno que vai atingir a meta (ID 5).
        return TestCaseConfigDTO.builder().testName("FinalizarEmprestimo_C5_AlunoAtingeMetaBonus").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos/" + "3" + "/finalizar")
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigFinalizar_C3_Inexistente() {
        return TestCaseConfigDTO.builder().testName("FinalizarEmprestimo_C3_Erro_Inexistente").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO)
                .httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_INEXISTENTE + "/finalizar")
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    // =================================================================================
    // 5. MÉTODO: Históricos
    // =================================================================================
    private TestCaseConfigDTO getConfigHistAluno_C1_ComFinalizados() {
        // Verifica o histórico da Fernanda (ID 2), que finalizou o empréstimo ID 1.
        return TestCaseConfigDTO.builder().testName("HistoricoAluno_C1_ComFinalizados").methodGroupKey(GROUP_HISTORICO)
                .httpMethod("GET").endpoint("/emprestimos/historico/aluno/" + ALUNO_ID_2_FERNANDA)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigHistLivro_C4_LimitadoASete() {
        return TestCaseConfigDTO.builder().testName("HistoricoLivro_C4_LimitadoASete").methodGroupKey(GROUP_HISTORICO)
                .httpMethod("GET").endpoint("/emprestimos/historico/livro/" + LIVRO_ID_3_SENHOR_ANEIS)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // ... ORQUESTRAÇÃO ...
    public Mono<ModuleTestDTO> runAllEmprestimoTests() {
        List<TestCaseConfigDTO> testConfigsInOrder = new ArrayList<>();

        // FASE 0: Criar o estado inicial da base com todos os empréstimos necessários.
        testConfigsInOrder.add(getConfigSetup_Emprestimo1_Fernanda());
        testConfigsInOrder.add(getConfigSetup_Emprestimo2_Mariana());
        testConfigsInOrder.add(getConfigSetup_Emprestimo3_Atrasado());
        testConfigsInOrder.add(getConfigSetup_Emprestimo4_ParaRenovar());
        testConfigsInOrder.add(getConfigSetup_Emprestimo5_AlunoMeta());

        // FASE 1: Testar cenários de ERRO ao tentar criar novos empréstimos.
        testConfigsInOrder.add(getConfigFazer_C2_AlunoJaComEmprestimo());
        testConfigsInOrder.add(getConfigFazer_C3_LivroIndisponivel());
        testConfigsInOrder.add(getConfigFazer_C4_AlunoInexistente());

        // FASE 2: Testar a renovação dos empréstimos criados no setup.
        testConfigsInOrder.add(getConfigRenovar_C1_Sucesso());
        testConfigsInOrder.add(getConfigRenovar_C2_Atrasado());
        testConfigsInOrder.add(getConfigRenovar_C3_Inexistente());

        // FASE 3: Listar e verificar o estado da base antes de finalizar.
        testConfigsInOrder.add(getConfigListar_C1_SemFiltros());
        testConfigsInOrder.add(getConfigListar_C3_ApenasAtrasados());

        // FASE 4: Finalizar os empréstimos, alterando o estado para "FINALIZADO".
        testConfigsInOrder.add(getConfigFinalizar_C1_AtivoSucesso());
        testConfigsInOrder.add(getConfigFinalizar_C2_Atrasado());
        testConfigsInOrder.add(getConfigFinalizar_C4_DevolucaoUltimaCopia());
        testConfigsInOrder.add(getConfigFinalizar_C5_AlunoAtingeMetaBonus());
        testConfigsInOrder.add(getConfigFinalizar_C3_Inexistente());

        // FASE 5: Verificar os históricos, que agora devem conter os empréstimos finalizados.
        testConfigsInOrder.add(getConfigHistAluno_C1_ComFinalizados());
        testConfigsInOrder.add(getConfigHistLivro_C4_LimitadoASete());

        return Flux.fromIterable(testConfigsInOrder)
                .concatMap(this::executeAndLog)
                .collectList()
                .map(allIndividualResults -> {
                    // ... (lógica de agregação de resultados, sem alterações)
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
                            .moduleName("Emprestimos")
                            .totalTests(totalModuleTests)
                            .sucessfulTests((int) successfulModuleTests)
                            .failedTests(totalModuleTests - (int) successfulModuleTests)
                            .successPercentage(moduleSuccessPercentage)
                            .methodTestsResults(methodSummaries)
                            .build();
                });
    }
}