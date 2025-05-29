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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TurmaTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;
    private static final String DEFAULT_EXISTING_TURMA_ID = "1";

    public TurmaTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private static final String GROUP_LISTAR_TURMAS = "ListarTodasTurmas";
    private static final String GROUP_CADASTRAR_TURMA = "CadastrarTurma";
    private static final String GROUP_EDITAR_TURMA = "EditarTurma";
    private static final String GROUP_DELETAR_TURMA = "DeletarTurma";

    private TestCaseConfigDTO getConfigListarTurmasSucesso() {
        return TestCaseConfigDTO.builder().testName("Turma_ListarTodas_Sucesso").methodGroupKey(GROUP_LISTAR_TURMAS).httpMethod("GET").endpoint("/turmas").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private Map<String, Object> getBaseCadastroTurmaBody(String serie) {
        Map<String, Object> body = new HashMap<>();
        body.put("serie", serie);
        return body;
    }

    private TestCaseConfigDTO getConfigCadastrarTurmaSucesso() {
        String serie = faker.educator().course() + " " + faker.letterify("?").toUpperCase();
        return TestCaseConfigDTO.builder().testName("Turma_Cadastrar_Sucesso").methodGroupKey(GROUP_CADASTRAR_TURMA).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getBaseCadastroTurmaBody(serie)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    private TestCaseConfigDTO getConfigCadastrarTurmaSerieEmBranco() {
        return TestCaseConfigDTO.builder().testName("Turma_Cadastrar_Erro_SerieEmBranco").methodGroupKey(GROUP_CADASTRAR_TURMA).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getBaseCadastroTurmaBody("Faker::Lorem.word()")).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigCadastrarTurmaSerieMuitoLonga() {
        String serieLonga = faker.lorem().fixedString(256);
        return TestCaseConfigDTO.builder().testName("Turma_Cadastrar_Erro_SerieMuitoLonga").methodGroupKey(GROUP_CADASTRAR_TURMA).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getBaseCadastroTurmaBody(serieLonga)).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigCadastrarTurmaSerieJaExistente() {
        String serieExistente = "1 Ano A";
        return TestCaseConfigDTO.builder().testName("Turma_Cadastrar_Erro_SerieJaExistente").methodGroupKey(GROUP_CADASTRAR_TURMA).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(getBaseCadastroTurmaBody(serieExistente)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(409).build();
    }

    private TestCaseConfigDTO getConfigCadastrarTurmaCorpoVazio() {
        return TestCaseConfigDTO.builder().testName("Turma_Cadastrar_Erro_CorpoVazio").methodGroupKey(GROUP_CADASTRAR_TURMA).httpMethod("POST").endpoint("/turmas").requestBodyTemplate(new HashMap<>()).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigEditarTurmaSucesso() {
        String novaSerie = faker.educator().secondarySchool() + " - Editado";
        return TestCaseConfigDTO.builder().testName("Turma_Editar_Sucesso").methodGroupKey(GROUP_EDITAR_TURMA).httpMethod("PUT").endpoint("/turmas/" + DEFAULT_EXISTING_TURMA_ID).requestBodyTemplate(getBaseCadastroTurmaBody(novaSerie)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigEditarTurmaIdNaoExistente() {
        String novaSerie = "Qualquer Serie";
        return TestCaseConfigDTO.builder().testName("Turma_Editar_Erro_IdNaoExistente").methodGroupKey(GROUP_EDITAR_TURMA).httpMethod("PUT").endpoint("/turmas/99999").requestBodyTemplate(getBaseCadastroTurmaBody(novaSerie)).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    private TestCaseConfigDTO getConfigEditarTurmaSerieEmBranco() {
        return TestCaseConfigDTO.builder().testName("Turma_Editar_Erro_SerieEmBranco").methodGroupKey(GROUP_EDITAR_TURMA).httpMethod("PUT").endpoint("/turmas/" + DEFAULT_EXISTING_TURMA_ID).requestBodyTemplate(getBaseCadastroTurmaBody("Faker::Lorem.word()")).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigEditarTurmaIdFormatoInvalido() {
        String novaSerie = "Qualquer Serie";
        return TestCaseConfigDTO.builder().testName("Turma_Editar_Erro_IdFormatoInvalido").methodGroupKey(GROUP_EDITAR_TURMA).httpMethod("PUT").endpoint("/turmas/abc").requestBodyTemplate(getBaseCadastroTurmaBody(novaSerie)).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigEditarTurmaParaSerieJaExistente() {
        String serieExistente = "1 Ano A"; // Supondo que esta série já existe e pertence a outra turma
        return TestCaseConfigDTO.builder().testName("Turma_Editar_Erro_ParaSerieJaExistente").methodGroupKey(GROUP_EDITAR_TURMA).httpMethod("PUT").endpoint("/turmas/" + DEFAULT_EXISTING_TURMA_ID).requestBodyTemplate(getBaseCadastroTurmaBody(serieExistente)).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(409).build();
    }


    private TestCaseConfigDTO getConfigDeletarTurmaSucesso() {
        String idParaDeletar = "2";
        return TestCaseConfigDTO.builder().testName("Turma_Deletar_Sucesso").methodGroupKey(GROUP_DELETAR_TURMA).httpMethod("DELETE").endpoint("/turmas/" + idParaDeletar).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(204).build();
    }

    private TestCaseConfigDTO getConfigDeletarTurmaIdNaoExistente() {
        return TestCaseConfigDTO.builder().testName("Turma_Deletar_Erro_IdNaoExistente").methodGroupKey(GROUP_DELETAR_TURMA).httpMethod("DELETE").endpoint("/turmas/99999").scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    private TestCaseConfigDTO getConfigDeletarTurmaComAlunos() {
        return TestCaseConfigDTO.builder().testName("Turma_Deletar_Erro_ComAlunos").methodGroupKey(GROUP_DELETAR_TURMA).httpMethod("DELETE").endpoint("/turmas/" + DEFAULT_EXISTING_TURMA_ID).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigDeletarTurmaIdFormatoInvalido() {
        return TestCaseConfigDTO.builder().testName("Turma_Deletar_Erro_IdFormatoInvalido").methodGroupKey(GROUP_DELETAR_TURMA).httpMethod("DELETE").endpoint("/turmas/xyz").scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigDeletarTurmaJaDeletada() {
        String idDeletadoRecentemente = "2"; // Assumindo que o teste Deletar_Sucesso usou este ID
        return TestCaseConfigDTO.builder().testName("Turma_Deletar_Erro_JaDeletada").methodGroupKey(GROUP_DELETAR_TURMA).httpMethod("DELETE").endpoint("/turmas/" + idDeletadoRecentemente).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    public Mono<ModuleTestDTO> runAllTurmaTests() {
        List<Mono<TestResult>> testMonos = new ArrayList<>();

        testMonos.add(testExecutionService.executeTest(getConfigListarTurmasSucesso()));
        // Adicionar mais 4 testes para Listar (se houver cenários de erro relevantes não cobertos)

        testMonos.add(testExecutionService.executeTest(getConfigCadastrarTurmaSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarTurmaSerieEmBranco()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarTurmaSerieMuitoLonga()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarTurmaSerieJaExistente()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarTurmaCorpoVazio()));

        testMonos.add(testExecutionService.executeTest(getConfigEditarTurmaSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarTurmaIdNaoExistente()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarTurmaSerieEmBranco()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarTurmaIdFormatoInvalido()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarTurmaParaSerieJaExistente()));

        testMonos.add(testExecutionService.executeTest(getConfigDeletarTurmaIdNaoExistente()));
        testMonos.add(testExecutionService.executeTest(getConfigDeletarTurmaComAlunos()));
        testMonos.add(testExecutionService.executeTest(getConfigDeletarTurmaIdFormatoInvalido()));
        // O teste de sucesso para deletar e deletar já deletada precisam de uma ordem ou setup
        // Para simplificar sem setup, adicionamos DeletarTurmaSucesso por último, assumindo que um ID "2" pode ser deletável
        testMonos.add(testExecutionService.executeTest(getConfigDeletarTurmaSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigDeletarTurmaJaDeletada()));


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