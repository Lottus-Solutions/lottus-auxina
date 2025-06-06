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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TurmaTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    private static final String TURMA_ID_1_CADASTRADA = "1"; // "1 Ano A - Matutino"
    private static final String TURMA_ID_2_CADASTRADA = "2"; // "2 Ano B - Vespertino"
    private static final String TURMA_ID_3_PARA_DELETAR_E_TESTAR = "3"; // "Turma Temporária Para Deletar"
    private static final String TURMA_ID_INEXISTENTE = "99999";
    private static final String TURMA_ID_FORMATO_INVALIDO = "abc";

    public TurmaTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private static final String GROUP_CADASTRAR_TURMA = "CadastrarTurma";
    private static final String GROUP_LISTAR_TURMAS = "ListarTodasTurmas";
    private static final String GROUP_EDITAR_TURMA = "EditarTurma";
    private static final String GROUP_DELETAR_TURMA = "DeletarTurma";

    private Map<String, Object> getTurmaBody(String serie) {
        Map<String, Object> body = new HashMap<>();
        body.put("serie", serie);
        return body;
    }

    // Métodos de Setup (Cadastros Iniciais)
    private TestCaseConfigDTO getConfigSetupCadastrarTurma1AnoA() {
        return TestCaseConfigDTO.builder().testName("Turma_Setup_1_1AnoA").methodGroupKey(GROUP_CADASTRAR_TURMA)
                .httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody("1 Ano A - Matutino"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetupCadastrarTurma2AnoB() {
        return TestCaseConfigDTO.builder().testName("Turma_Setup_2_2AnoB").methodGroupKey(GROUP_CADASTRAR_TURMA)
                .httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody("2 Ano B - Vespertino"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetupCadastrarTurmaParaDeletar() {
        return TestCaseConfigDTO.builder().testName("Turma_Setup_3_ParaDeletar").methodGroupKey(GROUP_CADASTRAR_TURMA)
                .httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody("Turma Temporária Para Deletar"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    private TestCaseConfigDTO getConfigCadastrarTurmaSerieEmBranco() {
        return TestCaseConfigDTO.builder().testName("Turma_Cadastrar_Erro_SerieEmBranco").methodGroupKey(GROUP_CADASTRAR_TURMA)
                .httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody("Faker::Lorem.word()"))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // Listar Turmas - Cenários BDD
    private TestCaseConfigDTO getConfigListarTurmas_C1_AposCadastros() {
        return TestCaseConfigDTO.builder().testName("Turma_Listar_C1_AposCadastros").methodGroupKey(GROUP_LISTAR_TURMAS)
                .httpMethod("GET").endpoint("/turmas").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListarTurmas_C2_VerificarID1() {
        return TestCaseConfigDTO.builder().testName("Turma_Listar_C2_VerificarID1").methodGroupKey(GROUP_LISTAR_TURMAS)
                .httpMethod("GET").endpoint("/turmas").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListarTurmas_C3_VerificarID2() {
        return TestCaseConfigDTO.builder().testName("Turma_Listar_C3_VerificarID2").methodGroupKey(GROUP_LISTAR_TURMAS)
                .httpMethod("GET").endpoint("/turmas").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListarTurmas_C4_AposRemocaoID3() {
        return TestCaseConfigDTO.builder().testName("Turma_Listar_C4_AposRemocaoID3").methodGroupKey(GROUP_LISTAR_TURMAS)
                .httpMethod("GET").endpoint("/turmas").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListarTurmas_C5_NaoRetornarInexistentes() {
        return TestCaseConfigDTO.builder().testName("Turma_Listar_C5_NaoRetornarInexistentes").methodGroupKey(GROUP_LISTAR_TURMAS)
                .httpMethod("GET").endpoint("/turmas").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // Editar Turma - Cenários BDD
    private TestCaseConfigDTO getConfigEditarTurma_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("Turma_Editar_C1_Sucesso").methodGroupKey(GROUP_EDITAR_TURMA)
                .httpMethod("PUT").endpoint("/turmas/" + TURMA_ID_1_CADASTRADA)
                .requestBodyTemplate(getTurmaBody("1 Ano A - Integral"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditarTurma_C2_IdNaoExistente() {
        return TestCaseConfigDTO.builder().testName("Turma_Editar_C2_Erro_IdNaoExistente").methodGroupKey(GROUP_EDITAR_TURMA)
                .httpMethod("PUT").endpoint("/turmas/" + TURMA_ID_INEXISTENTE)
                .requestBodyTemplate(getTurmaBody("Qualquer Serie"))
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigEditarTurma_C3_SerieJaExistente() {
        return TestCaseConfigDTO.builder().testName("Turma_Editar_C3_Erro_SerieJaExistente").methodGroupKey(GROUP_EDITAR_TURMA)
                .httpMethod("PUT").endpoint("/turmas/" + TURMA_ID_1_CADASTRADA)
                .requestBodyTemplate(getTurmaBody("2 Ano B - Vespertino"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(409).build();
    }
    private TestCaseConfigDTO getConfigEditarTurma_C4_IdFormatoInvalido() {
        return TestCaseConfigDTO.builder().testName("Turma_Editar_C4_Erro_IdFormatoInvalido").methodGroupKey(GROUP_EDITAR_TURMA)
                .httpMethod("PUT").endpoint("/turmas/" + TURMA_ID_FORMATO_INVALIDO)
                .requestBodyTemplate(getTurmaBody("Qualquer Serie"))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigEditarTurma_C5_SerieEmBranco() {
        return TestCaseConfigDTO.builder().testName("Turma_Editar_C5_Erro_SerieEmBranco").methodGroupKey(GROUP_EDITAR_TURMA)
                .httpMethod("PUT").endpoint("/turmas/" + TURMA_ID_1_CADASTRADA)
                .requestBodyTemplate(getTurmaBody("Faker::Lorem.word()"))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // Remover Turma - Cenários BDD
    private TestCaseConfigDTO getConfigRemoverTurma_C1_SemAlunosSucesso() {
        return TestCaseConfigDTO.builder().testName("Turma_Remover_C1_SemAlunosSucesso").methodGroupKey(GROUP_DELETAR_TURMA)
                .httpMethod("DELETE").endpoint("/turmas/" + TURMA_ID_3_PARA_DELETAR_E_TESTAR)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(204).build();
    }
    private TestCaseConfigDTO getConfigRemoverTurma_C2_ComAlunos() {
        return TestCaseConfigDTO.builder().testName("Turma_Remover_C2_Erro_ComAlunos").methodGroupKey(GROUP_DELETAR_TURMA)
                .httpMethod("DELETE").endpoint("/turmas/" + TURMA_ID_1_CADASTRADA)
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigRemoverTurma_C3_IdNaoExistente() {
        return TestCaseConfigDTO.builder().testName("Turma_Remover_C3_Erro_IdNaoExistente").methodGroupKey(GROUP_DELETAR_TURMA)
                .httpMethod("DELETE").endpoint("/turmas/" + TURMA_ID_INEXISTENTE)
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRemoverTurma_C4_JaDeletada() {
        return TestCaseConfigDTO.builder().testName("Turma_Remover_C4_Erro_JaDeletada").methodGroupKey(GROUP_DELETAR_TURMA)
                .httpMethod("DELETE").endpoint("/turmas/" + TURMA_ID_3_PARA_DELETAR_E_TESTAR)
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRemoverTurma_C5_IdFormatoInvalido() {
        return TestCaseConfigDTO.builder().testName("Turma_Remover_C5_Erro_IdFormatoInvalido").methodGroupKey(GROUP_DELETAR_TURMA)
                .httpMethod("DELETE").endpoint("/turmas/" + TURMA_ID_FORMATO_INVALIDO)
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // Outros testes de Cadastro (não diretamente dos BDDs, mas importantes para cobertura)
    private TestCaseConfigDTO getConfigCadastrarTurmaSucessoFaker() {
        String serie = faker.educator().course() + " " + faker.letterify("?").toUpperCase() + " " + faker.random().hex(4);
        return TestCaseConfigDTO.builder().testName("Turma_Cadastrar_OutroSucesso_Faker").methodGroupKey(GROUP_CADASTRAR_TURMA)
                .httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody(serie))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigCadastrarTurmaSerieMuitoLonga() {
        String serieLonga = faker.lorem().fixedString(256);
        return TestCaseConfigDTO.builder().testName("Turma_Cadastrar_Erro_SerieMuitoLonga").methodGroupKey(GROUP_CADASTRAR_TURMA)
                .httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getTurmaBody(serieLonga))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigCadastrarTurmaCorpoVazio() {
        return TestCaseConfigDTO.builder().testName("Turma_Cadastrar_Erro_CorpoVazio").methodGroupKey(GROUP_CADASTRAR_TURMA)
                .httpMethod("POST").endpoint("/turmas").requestBodyTemplate(new HashMap<>())
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }


    public Mono<ModuleTestDTO> runAllTurmaTests() {
        List<Mono<TestResult>> testMonos = new ArrayList<>();

        // Setup: Cadastrar turmas iniciais
        testMonos.add(testExecutionService.executeTest(getConfigSetupCadastrarTurma1AnoA()));
        testMonos.add(testExecutionService.executeTest(getConfigSetupCadastrarTurma2AnoB()));
        testMonos.add(testExecutionService.executeTest(getConfigSetupCadastrarTurmaParaDeletar()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarTurmaSucessoFaker()));

        // Testes de Listar Turmas (BDD)
        testMonos.add(testExecutionService.executeTest(getConfigListarTurmas_C1_AposCadastros()));
        testMonos.add(testExecutionService.executeTest(getConfigListarTurmas_C2_VerificarID1()));
        testMonos.add(testExecutionService.executeTest(getConfigListarTurmas_C3_VerificarID2()));
        testMonos.add(testExecutionService.executeTest(getConfigListarTurmas_C5_NaoRetornarInexistentes()));

        // Testes de Editar Turma (BDD)
        testMonos.add(testExecutionService.executeTest(getConfigEditarTurma_C1_Sucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarTurma_C2_IdNaoExistente()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarTurma_C3_SerieJaExistente()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarTurma_C4_IdFormatoInvalido()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarTurma_C5_SerieEmBranco()));

        // Testes Adicionais de Cadastrar Turma (não BDD, mas cobertura)
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarTurmaSerieEmBranco()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarTurmaSerieMuitoLonga()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarTurmaCorpoVazio()));

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