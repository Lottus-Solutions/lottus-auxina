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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmprestimoTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;
    private static final String DEFAULT_EXISTING_ALUNO_MATRICULA = "1";
    private static final String DEFAULT_EXISTING_LIVRO_ID = "1";
    private static final String DEFAULT_EXISTING_EMPRESTIMO_ID = "1"; // Assumir que existe para finalizar/renovar

    public EmprestimoTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private String getFormattedCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // --- Grupo: Listar Empréstimos Paginado ---
    private static final String GROUP_LISTAR_EMPRESTIMOS_PAGINADO = "ListarEmprestimosPaginado";

    private TestCaseConfigDTO getConfigListarEmprestimosPaginadoSucesso() {
        Map<String, String> qp = new HashMap<>();
        qp.put("busca", ""); qp.put("atrasados", "false"); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Emprestimo_ListarPaginado_Sucesso").methodGroupKey(GROUP_LISTAR_EMPRESTIMOS_PAGINADO).httpMethod("GET").endpoint("/emprestimos").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Adicionar mais 4: busca por termo, apenas atrasados, paginação inválida, etc.

    // --- Grupo: Fazer Novo Empréstimo ---
    private static final String GROUP_NOVO_EMPRESTIMO = "NovoEmprestimo";

    private Map<String, Object> getBaseNovoEmprestimoBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("matriculaAluno", DEFAULT_EXISTING_ALUNO_MATRICULA);
        body.put("fk_livro", DEFAULT_EXISTING_LIVRO_ID);
        body.put("dataEmprestimo", getFormattedCurrentDate());
        return body;
    }

    private TestCaseConfigDTO getConfigNovoEmprestimoSucesso() {
        return TestCaseConfigDTO.builder().testName("Emprestimo_Novo_Sucesso").methodGroupKey(GROUP_NOVO_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos").requestBodyTemplate(getBaseNovoEmprestimoBody()).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    // Adicionar mais 4: matrícula aluno inexistente, livro inexistente, livro indisponível, data inválida.

    // --- Grupo: Listar Empréstimos Atrasados ---
    private static final String GROUP_LISTAR_ATRASADOS = "ListarEmprestimosAtrasados";

    private TestCaseConfigDTO getConfigListarAtrasadosSucesso() {
        return TestCaseConfigDTO.builder().testName("Emprestimo_ListarAtrasados_Sucesso").methodGroupKey(GROUP_LISTAR_ATRASADOS).httpMethod("GET").endpoint("/emprestimos/atrasados").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Poucos cenários de erro de input aqui, mais servidor/autorização.

    // --- Grupo: Histórico de Aluno ---
    private static final String GROUP_HISTORICO_ALUNO = "HistoricoAluno";

    private TestCaseConfigDTO getConfigHistoricoAlunoSucesso() {
        return TestCaseConfigDTO.builder().testName("Emprestimo_HistoricoAluno_Sucesso").methodGroupKey(GROUP_HISTORICO_ALUNO).httpMethod("GET").endpoint("/emprestimos/historico/aluno/" + DEFAULT_EXISTING_ALUNO_MATRICULA).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Adicionar mais 4: matrícula inexistente, matrícula formato inválido.

    // --- Grupo: Histórico de Livro ---
    private static final String GROUP_HISTORICO_LIVRO = "HistoricoLivro";

    private TestCaseConfigDTO getConfigHistoricoLivroSucesso() {
        return TestCaseConfigDTO.builder().testName("Emprestimo_HistoricoLivro_Sucesso").methodGroupKey(GROUP_HISTORICO_LIVRO).httpMethod("GET").endpoint("/emprestimos/historico/livro/" + DEFAULT_EXISTING_LIVRO_ID).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Adicionar mais 4: ID livro inexistente, ID formato inválido.

    // --- Grupo: Finalizar Empréstimo ---
    private static final String GROUP_FINALIZAR_EMPRESTIMO = "FinalizarEmprestimo";

    private TestCaseConfigDTO getConfigFinalizarEmprestimoSucesso() {
        return TestCaseConfigDTO.builder().testName("Emprestimo_Finalizar_Sucesso").methodGroupKey(GROUP_FINALIZAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + DEFAULT_EXISTING_EMPRESTIMO_ID + "/finalizar").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build(); // Ou 204
    }
    // Adicionar mais 4: ID empréstimo inexistente, já finalizado, formato ID inválido.

    // --- Grupo: Renovar Empréstimo ---
    private static final String GROUP_RENOVAR_EMPRESTIMO = "RenovarEmprestimo";

    private TestCaseConfigDTO getConfigRenovarEmprestimoSucesso() {
        return TestCaseConfigDTO.builder().testName("Emprestimo_Renovar_Sucesso").methodGroupKey(GROUP_RENOVAR_EMPRESTIMO).httpMethod("POST").endpoint("/emprestimos/" + DEFAULT_EXISTING_EMPRESTIMO_ID + "/renovar").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Adicionar mais 4: ID empréstimo inexistente, limite de renovações atingido, já finalizado, formato ID inválido.


    public Mono<ModuleTestDTO> runAllEmprestimoTests() {
        List<Mono<TestResult>> testMonos = new ArrayList<>();

        testMonos.add(testExecutionService.executeTest(getConfigListarEmprestimosPaginadoSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigNovoEmprestimoSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigListarAtrasadosSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigHistoricoAlunoSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigHistoricoLivroSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigFinalizarEmprestimoSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigRenovarEmprestimoSucesso()));

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