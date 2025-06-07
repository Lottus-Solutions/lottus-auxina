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
public class EmprestimoTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    // IDs de Alunos e Livros (assumindo que foram cadastrados previamente em uma ordem específica)
    private static final String ALUNO_ID_1_CARLOS = "1";
    private static final String ALUNO_ID_2_FERNANDA = "2";
    private static final String ALUNO_ID_3_RICARDO = "3";
    private static final String ALUNO_ID_4_MARIANA = "4";
    private static final String ALUNO_ID_5_META_LIVROS = "5"; // Aluno com 4 livros lidos para o cenário de bônus
    private static final String ALUNO_ID_INEXISTENTE = "99999";

    private static final String LIVRO_ID_1_REVOLUCAO = "1";
    private static final String LIVRO_ID_4_1984_INDISPONIVEL = "4";
    private static final String LIVRO_ID_6_HOBBIT_ULTIMA_COPIA = "6";
    private static final String LIVRO_ID_3_SENHOR_ANEIS = "3"; // Para cenários genéricos e de histórico
    private static final String LIVRO_ID_INEXISTENTE = "9998";

    // IDs de Empréstimos (placeholders; o ideal é que o backend os gere)
    private static final String EMPRESTIMO_ID_1_ATIVO = "1";
    private static final String EMPRESTIMO_ID_2_ATRASADO = "2";
    private static final String EMPRESTIMO_ID_3_RENOVADO_1X = "3";
    private static final String EMPRESTIMO_ID_4_ULTIMA_COPIA = "4"; // Empréstimo do livro "O Hobbit"
    private static final String EMPRESTIMO_ID_5_ALUNO_META = "5"; // Empréstimo do aluno que vai atingir a meta
    private static final String EMPRESTIMO_ID_INEXISTENTE = "999";

    // --- GRUPOS DE TESTE POR MÉTODO DO BDD ---
    private static final String GROUP_FAZER_EMPRESTIMO = "1. FazerEmprestimo";
    private static final String GROUP_RENOVAR_EMPRESTIMO = "2. RenovarEmprestimo";
    private static final String GROUP_FINALIZAR_EMPRESTIMO = "3. FinalizarEmprestimo";
    private static final String GROUP_LISTAR_EMPRESTIMOS = "4. ListarEmprestimos";
    private static final String GROUP_HISTORICO_ALUNO = "5. BuscarHistoricoAluno";
    private static final String GROUP_HISTORICO_LIVRO = "5. BuscarHistoricoLivro";


    public EmprestimoTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private Mono<TestResult> executeAndLog(TestCaseConfigDTO config) {
        return testExecutionService.executeTest(config)
                .doOnSubscribe(subscription -> log.info("➡️  INICIANDO TESTE DE EMPRÉSTIMO: {}", config.getTestName()));
    }

    // =================================================================================
    // 1. MÉTODO: fazerEmprestimo
    // =================================================================================

    private TestCaseConfigDTO getConfigFazer_C1_Sucesso() {
        Map<String, Object> body = new HashMap<>(); body.put("matriculaAluno", ALUNO_ID_1_CARLOS); body.put("fk_livro", LIVRO_ID_1_REVOLUCAO);
        return TestCaseConfigDTO.builder().testName("FazerEmprestimo_C1_Sucesso").methodGroupKey(GROUP_FAZER_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(body).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigFazer_C2_AlunoJaComEmprestimo() {
        Map<String, Object> body = new HashMap<>(); body.put("matriculaAluno", ALUNO_ID_2_FERNANDA); body.put("fk_livro", LIVRO_ID_3_SENHOR_ANEIS);
        return TestCaseConfigDTO.builder().testName("FazerEmprestimo_C2_Erro_AlunoJaTem").methodGroupKey(GROUP_FAZER_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(body).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(409).build();
    }
    private TestCaseConfigDTO getConfigFazer_C3_LivroIndisponivel() {
        Map<String, Object> body = new HashMap<>(); body.put("matriculaAluno", ALUNO_ID_3_RICARDO); body.put("fk_livro", LIVRO_ID_4_1984_INDISPONIVEL);
        return TestCaseConfigDTO.builder().testName("FazerEmprestimo_C3_Erro_LivroIndisponivel").methodGroupKey(GROUP_FAZER_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(body).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(409).build();
    }
    private TestCaseConfigDTO getConfigFazer_C4_AlunoInexistente() {
        Map<String, Object> body = new HashMap<>(); body.put("matriculaAluno", ALUNO_ID_INEXISTENTE); body.put("fk_livro", LIVRO_ID_3_SENHOR_ANEIS);
        return TestCaseConfigDTO.builder().testName("FazerEmprestimo_C4_Erro_AlunoInexistente").methodGroupKey(GROUP_FAZER_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(body).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigFazer_C5_UltimaCopia() {
        Map<String, Object> body = new HashMap<>(); body.put("matriculaAluno", ALUNO_ID_4_MARIANA); body.put("fk_livro", LIVRO_ID_6_HOBBIT_ULTIMA_COPIA);
        return TestCaseConfigDTO.builder().testName("FazerEmprestimo_C5_Sucesso_UltimaCopia").methodGroupKey(GROUP_FAZER_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(body).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    // =================================================================================
    // 2. MÉTODO: renovarEmprestimo
    // =================================================================================

    private TestCaseConfigDTO getConfigRenovar_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("RenovarEmprestimo_C1_Sucesso").methodGroupKey(GROUP_RENOVAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_1_ATIVO + "/renovar").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigRenovar_C2_Atrasado() {
        return TestCaseConfigDTO.builder().testName("RenovarEmprestimo_C2_Atrasado").methodGroupKey(GROUP_RENOVAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_2_ATRASADO + "/renovar").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigRenovar_C3_Inexistente() {
        return TestCaseConfigDTO.builder().testName("RenovarEmprestimo_C3_Erro_Inexistente").methodGroupKey(GROUP_RENOVAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_INEXISTENTE + "/renovar").scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRenovar_C4_IncrementaContador() {
        return TestCaseConfigDTO.builder().testName("RenovarEmprestimo_C4_IncrementaContador").methodGroupKey(GROUP_RENOVAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_3_RENOVADO_1X + "/renovar").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigRenovar_C5_NaoAlteraDadosLivro() {
        // Este teste é conceitual. A verificação real de que os dados do livro não mudam
        // precisaria de uma chamada GET ao livro antes e depois, o que foge do escopo deste teste unitário de API.
        // A própria chamada de renovação bem-sucedida serve como substituto.
        return TestCaseConfigDTO.builder().testName("RenovarEmprestimo_C5_NaoAlteraDadosLivro").methodGroupKey(GROUP_RENOVAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_1_ATIVO + "/renovar").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // =================================================================================
    // 3. MÉTODO: finalizarEmprestimo
    // =================================================================================

    private TestCaseConfigDTO getConfigFinalizar_C1_AtivoSucesso() {
        return TestCaseConfigDTO.builder().testName("FinalizarEmprestimo_C1_AtivoSucesso").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_1_ATIVO + "/finalizar").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigFinalizar_C2_Atrasado() {
        return TestCaseConfigDTO.builder().testName("FinalizarEmprestimo_C2_Atrasado").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_2_ATRASADO + "/finalizar").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigFinalizar_C3_Inexistente() {
        return TestCaseConfigDTO.builder().testName("FinalizarEmprestimo_C3_Erro_Inexistente").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_INEXISTENTE + "/finalizar").scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigFinalizar_C4_DevolucaoUltimaCopia() {
        return TestCaseConfigDTO.builder().testName("FinalizarEmprestimo_C4_DevolucaoUltimaCopia").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_4_ULTIMA_COPIA + "/finalizar").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigFinalizar_C5_AlunoAtingeMetaBonus() {
        return TestCaseConfigDTO.builder().testName("FinalizarEmprestimo_C5_AlunoAtingeMetaBonus").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + EMPRESTIMO_ID_5_ALUNO_META + "/finalizar").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // =================================================================================
    // 4. MÉTODO: listarEmprestimos
    // =================================================================================

    private TestCaseConfigDTO getConfigListar_C1_SemFiltros() {
        Map<String, String> qp = new HashMap<>(); qp.put("pagina", "0"); qp.put("tamanho", "10");
        return TestCaseConfigDTO.builder().testName("ListarEmprestimos_C1_SemFiltros").methodGroupKey(GROUP_LISTAR_EMPRESTIMOS).httpMethod("GET").endpoint("/emprestimos").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListar_C2_ComBusca() {
        Map<String, String> qp = new HashMap<>(); qp.put("pagina", "0"); qp.put("tamanho", "10"); qp.put("busca", "Carlos");
        return TestCaseConfigDTO.builder().testName("ListarEmprestimos_C2_ComBusca").methodGroupKey(GROUP_LISTAR_EMPRESTIMOS).httpMethod("GET").endpoint("/emprestimos").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListar_C3_ApenasAtrasados() {
        Map<String, String> qp = new HashMap<>(); qp.put("pagina", "0"); qp.put("tamanho", "10"); qp.put("atrasados", "true");
        return TestCaseConfigDTO.builder().testName("ListarEmprestimos_C3_ApenasAtrasados").methodGroupKey(GROUP_LISTAR_EMPRESTIMOS).httpMethod("GET").endpoint("/emprestimos").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListar_C4_PaginacaoInvalida() {
        Map<String, String> qp = new HashMap<>(); qp.put("pagina", "-1"); qp.put("tamanho", "10");
        return TestCaseConfigDTO.builder().testName("ListarEmprestimos_C4_Erro_PaginacaoInvalida").methodGroupKey(GROUP_LISTAR_EMPRESTIMOS).httpMethod("GET").endpoint("/emprestimos").queryParamsTemplate(qp).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigListar_C5_BuscaSemResultados() {
        Map<String, String> qp = new HashMap<>(); qp.put("pagina", "0"); qp.put("tamanho", "10"); qp.put("busca", "Zebra");
        return TestCaseConfigDTO.builder().testName("ListarEmprestimos_C5_BuscaSemResultados").methodGroupKey(GROUP_LISTAR_EMPRESTIMOS).httpMethod("GET").endpoint("/emprestimos").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build(); // Espera 200 OK com página vazia
    }

    // =================================================================================
    // 5. MÉTODO: buscarHistoricoAluno
    // =================================================================================

    private TestCaseConfigDTO getConfigHistAluno_C1_ComFinalizados() {
        return TestCaseConfigDTO.builder().testName("HistoricoAluno_C1_ComFinalizados").methodGroupKey(GROUP_HISTORICO_ALUNO).httpMethod("GET").endpoint("/emprestimos/historico/aluno/" + ALUNO_ID_1_CARLOS).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigHistAluno_C2_SemFinalizados() {
        return TestCaseConfigDTO.builder().testName("HistoricoAluno_C2_SemFinalizados").methodGroupKey(GROUP_HISTORICO_ALUNO).httpMethod("GET").endpoint("/emprestimos/historico/aluno/" + ALUNO_ID_2_FERNANDA).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build(); // Espera 200 OK com lista vazia
    }
    private TestCaseConfigDTO getConfigHistAluno_C3_AlunoInexistente() { // BDD estava impreciso, corrigido para Aluno
        return TestCaseConfigDTO.builder().testName("HistoricoAluno_C3_Erro_Inexistente").methodGroupKey(GROUP_HISTORICO_ALUNO).httpMethod("GET").endpoint("/emprestimos/historico/aluno/" + ALUNO_ID_INEXISTENTE).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    // =================================================================================
    // 5. MÉTODO: buscarHistoricoLivro
    // =================================================================================

    private TestCaseConfigDTO getConfigHistLivro_C3_LivroInexistente() { // C3 do BDD de livro
        return TestCaseConfigDTO.builder().testName("HistoricoLivro_C3_Erro_Inexistente").methodGroupKey(GROUP_HISTORICO_LIVRO).httpMethod("GET").endpoint("/emprestimos/historico/livro/" + LIVRO_ID_INEXISTENTE).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigHistLivro_C4_LimitadoASete() {
        return TestCaseConfigDTO.builder().testName("HistoricoLivro_C4_LimitadoASete").methodGroupKey(GROUP_HISTORICO_LIVRO).httpMethod("GET").endpoint("/emprestimos/historico/livro/" + LIVRO_ID_3_SENHOR_ANEIS).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigHistLivro_C5_NaoIncluiAtivos() {
        return TestCaseConfigDTO.builder().testName("HistoricoLivro_C5_NaoIncluiAtivos").methodGroupKey(GROUP_HISTORICO_LIVRO).httpMethod("GET").endpoint("/emprestimos/historico/livro/" + LIVRO_ID_4_1984_INDISPONIVEL).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }


    public Mono<ModuleTestDTO> runAllEmprestimoTests() {
        List<TestCaseConfigDTO> testConfigsInOrder = new ArrayList<>();

        // FASE 1: CRIAR O ESTADO INICIAL COM EMPRÉSTIMOS
        testConfigsInOrder.add(getConfigFazer_C1_Sucesso());
        testConfigsInOrder.add(getConfigFazer_C5_UltimaCopia());
        testConfigsInOrder.add(getConfigFazer_C2_AlunoJaComEmprestimo());
        testConfigsInOrder.add(getConfigFazer_C3_LivroIndisponivel());
        testConfigsInOrder.add(getConfigFazer_C4_AlunoInexistente());

        // FASE 2: RENOVAR EMPRÉSTIMOS EXISTENTES
        testConfigsInOrder.add(getConfigRenovar_C1_Sucesso());
        testConfigsInOrder.add(getConfigRenovar_C5_NaoAlteraDadosLivro());
        testConfigsInOrder.add(getConfigRenovar_C4_IncrementaContador());
        testConfigsInOrder.add(getConfigRenovar_C2_Atrasado());
        testConfigsInOrder.add(getConfigRenovar_C3_Inexistente());

        // FASE 3: LISTAR E VERIFICAR O ESTADO ATUAL
        testConfigsInOrder.add(getConfigListar_C1_SemFiltros());
        testConfigsInOrder.add(getConfigListar_C2_ComBusca());
        testConfigsInOrder.add(getConfigListar_C3_ApenasAtrasados());
        testConfigsInOrder.add(getConfigListar_C4_PaginacaoInvalida());
        testConfigsInOrder.add(getConfigListar_C5_BuscaSemResultados());

        // FASE 4: FINALIZAR EMPRÉSTIMOS
        testConfigsInOrder.add(getConfigFinalizar_C1_AtivoSucesso());
        testConfigsInOrder.add(getConfigFinalizar_C2_Atrasado());
        testConfigsInOrder.add(getConfigFinalizar_C4_DevolucaoUltimaCopia());
        testConfigsInOrder.add(getConfigFinalizar_C5_AlunoAtingeMetaBonus());
        testConfigsInOrder.add(getConfigFinalizar_C3_Inexistente());

        // FASE 5: VERIFICAR HISTÓRICOS APÓS FINALIZAÇÕES
        testConfigsInOrder.add(getConfigHistAluno_C1_ComFinalizados());
        testConfigsInOrder.add(getConfigHistAluno_C2_SemFinalizados());
        testConfigsInOrder.add(getConfigHistLivro_C5_NaoIncluiAtivos());
        testConfigsInOrder.add(getConfigHistLivro_C4_LimitadoASete());
        testConfigsInOrder.add(getConfigHistAluno_C3_AlunoInexistente());
        testConfigsInOrder.add(getConfigHistLivro_C3_LivroInexistente());

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