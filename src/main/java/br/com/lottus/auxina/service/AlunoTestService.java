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
public class AlunoTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;
    private static final String DEFAULT_EXISTING_ALUNO_MATRICULA = "1"; // Assumir que existe para PUT/DELETE/GET
    private static final String DEFAULT_EXISTING_TURMA_ID = "1";       // Assumir que existe

    public AlunoTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    // --- Grupo: Listar Todos Alunos ---
    private static final String GROUP_LISTAR_ALUNOS = "ListarTodosAlunos";

    private TestCaseConfigDTO getConfigListarAlunosSucesso() {
        return TestCaseConfigDTO.builder().testName("Aluno_ListarTodos_Sucesso").methodGroupKey(GROUP_LISTAR_ALUNOS).httpMethod("GET").endpoint("/alunos").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Para GET /alunos, como não é paginado e não tem parâmetros, cenários de erro seriam mais relacionados ao servidor (5xx)
    // ou talvez autenticação/autorização (401/403), que não são o foco aqui.
    // Podemos adicionar um teste para verificar se a resposta não é vazia se esperamos dados.

    // --- Grupo: Buscar Aluno por Nome em Turma ---
    private static final String GROUP_BUSCAR_NOME_TURMA = "BuscarAlunoNomeTurma";

    private TestCaseConfigDTO getConfigBuscarNomeTurmaSucesso() {
        String nome = "Faker::Name.firstName";
        return TestCaseConfigDTO.builder().testName("Aluno_BuscarNomeTurma_Sucesso").methodGroupKey(GROUP_BUSCAR_NOME_TURMA).httpMethod("GET").endpoint("/alunos/buscar-aluno-nome-turma/" + DEFAULT_EXISTING_TURMA_ID + "/" + nome).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigBuscarNomeTurmaNaoEncontrado() {
        String nome = "AlunoInexistente" + faker.random().hex(5);
        return TestCaseConfigDTO.builder().testName("Aluno_BuscarNomeTurma_NaoEncontrado").methodGroupKey(GROUP_BUSCAR_NOME_TURMA).httpMethod("GET").endpoint("/alunos/buscar-aluno-nome-turma/" + DEFAULT_EXISTING_TURMA_ID + "/" + nome).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    private TestCaseConfigDTO getConfigBuscarNomeTurmaIdTurmaInvalida() {
        String nome = "Faker::Name.firstName";
        return TestCaseConfigDTO.builder().testName("Aluno_BuscarNomeTurma_Erro_TurmaInvalida").methodGroupKey(GROUP_BUSCAR_NOME_TURMA).httpMethod("GET").endpoint("/alunos/buscar-aluno-nome-turma/99999/" + nome).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    // Adicionar mais 2: nome vazio, turmaId formato inválido

    // --- Grupo: Cadastrar Aluno ---
    private static final String GROUP_CADASTRAR_ALUNO = "CadastrarAluno";

    private Map<String, Object> getBaseCadastroAlunoBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Faker::Name.fullName");
        body.put("qtd_bonus", "Faker::Number.numberBetween(0,100)");
        body.put("turma_id", DEFAULT_EXISTING_TURMA_ID);
        body.put("qtd_livros_lidos", "Faker::Number.numberBetween(0,50)");
        return body;
    }

    private TestCaseConfigDTO getConfigCadastrarAlunoSucesso() {
        return TestCaseConfigDTO.builder().testName("Aluno_Cadastrar_Sucesso").methodGroupKey(GROUP_CADASTRAR_ALUNO).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getBaseCadastroAlunoBody()).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    private TestCaseConfigDTO getConfigCadastrarAlunoNomeEmBranco() {
        return TestCaseConfigDTO.builder().testName("Aluno_Cadastrar_Erro_NomeEmBranco").methodGroupKey(GROUP_CADASTRAR_ALUNO).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getBaseCadastroAlunoBody()).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigCadastrarAlunoTurmaInexistente() {
        Map<String, Object> body = getBaseCadastroAlunoBody(); body.put("turma_id", "99999");
        return TestCaseConfigDTO.builder().testName("Aluno_Cadastrar_Erro_TurmaInexistente").methodGroupKey(GROUP_CADASTRAR_ALUNO).httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(body).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    // Adicionar mais 2: qtd_bonus negativo, qtd_livros_lidos negativo

    // --- Grupo: Editar Aluno ---
    private static final String GROUP_EDITAR_ALUNO = "EditarAluno";

    private Map<String, Object> getBaseEditarAlunoBody() {
        Map<String, Object> body = getBaseCadastroAlunoBody(); // Reutiliza e adiciona matrícula
        body.put("matricula", DEFAULT_EXISTING_ALUNO_MATRICULA); // Matricula não deve ser editável via corpo, mas pode ser parte do DTO
        return body;
    }

    private TestCaseConfigDTO getConfigEditarAlunoSucesso() {
        Map<String, Object> body = getBaseEditarAlunoBody(); body.put("nome", "Aluno Editado " + "Faker::Name.lastName");
        return TestCaseConfigDTO.builder().testName("Aluno_Editar_Sucesso").methodGroupKey(GROUP_EDITAR_ALUNO).httpMethod("PUT").endpoint("/alunos/editar/" + DEFAULT_EXISTING_ALUNO_MATRICULA).requestBodyTemplate(body).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Adicionar mais 4: matrícula inexistente, corpo inválido (nome em branco), etc.

    // --- Grupo: Listar Turmas ---
    private static final String GROUP_LISTAR_TURMAS = "ListarTurmas";

    private TestCaseConfigDTO getConfigListarTurmasSucesso() {
        return TestCaseConfigDTO.builder().testName("Aluno_ListarTurmas_Sucesso").methodGroupKey(GROUP_LISTAR_TURMAS).httpMethod("GET").endpoint("/alunos/listar-turmas").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Similar ao Listar Alunos, poucos cenários de erro de input aqui.

    // --- Grupo: Buscar Alunos por Nome ---
    private static final String GROUP_BUSCAR_POR_NOME = "BuscarAlunosPorNome";

    private TestCaseConfigDTO getConfigBuscarPorNomeSucesso() {
        String nome = "Faker::Name.firstName";
        return TestCaseConfigDTO.builder().testName("Aluno_BuscarPorNome_Sucesso").methodGroupKey(GROUP_BUSCAR_POR_NOME).httpMethod("GET").endpoint("/alunos/nome/" + nome).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Adicionar mais 4: nome não encontrado (ainda 200 com lista vazia), nome com caracteres especiais, nome muito longo, nome vazio (400 ou 404).

    // --- Grupo: Remover Aluno ---
    private static final String GROUP_REMOVER_ALUNO = "RemoverAluno";

    private TestCaseConfigDTO getConfigRemoverAlunoSucesso() {
        String matriculaParaRemover = "2"; // Idealmente, matricula de um aluno criado para este teste
        return TestCaseConfigDTO.builder().testName("Aluno_Remover_Sucesso").methodGroupKey(GROUP_REMOVER_ALUNO).httpMethod("DELETE").endpoint("/alunos/remover/" + matriculaParaRemover).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(204).build();
    }

    private TestCaseConfigDTO getConfigRemoverAlunoMatriculaNaoExistente() {
        return TestCaseConfigDTO.builder().testName("Aluno_Remover_Erro_MatriculaNaoExistente").methodGroupKey(GROUP_REMOVER_ALUNO).httpMethod("DELETE").endpoint("/alunos/remover/999999").scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    // Adicionar mais 3: matricula formato inválido, tentar remover aluno com pendências (se aplicável), etc.

    // --- Grupo: Listar Alunos por Turma ---
    private static final String GROUP_LISTAR_POR_TURMA = "ListarAlunosPorTurma";

    private TestCaseConfigDTO getConfigListarPorTurmaSucesso() {
        return TestCaseConfigDTO.builder().testName("Aluno_ListarPorTurma_Sucesso").methodGroupKey(GROUP_LISTAR_POR_TURMA).httpMethod("GET").endpoint("/alunos/turma/" + DEFAULT_EXISTING_TURMA_ID).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Adicionar mais 4: turmaId inexistente (404), turmaId formato inválido (400), turma sem alunos (200 lista vazia).

    // --- Grupo: Obter Aluno por Matrícula ---
    private static final String GROUP_OBTER_POR_MATRICULA = "ObterAlunoPorMatricula";

    private TestCaseConfigDTO getConfigObterPorMatriculaSucesso() {
        return TestCaseConfigDTO.builder().testName("Aluno_ObterPorMatricula_Sucesso").methodGroupKey(GROUP_OBTER_POR_MATRICULA).httpMethod("GET").endpoint("/alunos/" + DEFAULT_EXISTING_ALUNO_MATRICULA).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigObterPorMatriculaNaoExistente() {
        return TestCaseConfigDTO.builder().testName("Aluno_ObterPorMatricula_NaoExistente").methodGroupKey(GROUP_OBTER_POR_MATRICULA).httpMethod("GET").endpoint("/alunos/999999").scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    // Adicionar mais 3: matricula formato inválido (400).

    public Mono<ModuleTestDTO> runAllAlunoTests() {
        List<Mono<TestResult>> testMonos = new ArrayList<>();

        testMonos.add(testExecutionService.executeTest(getConfigListarAlunosSucesso()));

        testMonos.add(testExecutionService.executeTest(getConfigBuscarNomeTurmaSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigBuscarNomeTurmaNaoEncontrado()));
        testMonos.add(testExecutionService.executeTest(getConfigBuscarNomeTurmaIdTurmaInvalida()));

        testMonos.add(testExecutionService.executeTest(getConfigCadastrarAlunoSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarAlunoNomeEmBranco()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarAlunoTurmaInexistente()));

        testMonos.add(testExecutionService.executeTest(getConfigEditarAlunoSucesso()));

        testMonos.add(testExecutionService.executeTest(getConfigListarTurmasSucesso()));

        testMonos.add(testExecutionService.executeTest(getConfigBuscarPorNomeSucesso()));

        testMonos.add(testExecutionService.executeTest(getConfigRemoverAlunoMatriculaNaoExistente()));
        testMonos.add(testExecutionService.executeTest(getConfigRemoverAlunoSucesso()));

        testMonos.add(testExecutionService.executeTest(getConfigListarPorTurmaSucesso()));

        testMonos.add(testExecutionService.executeTest(getConfigObterPorMatriculaSucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigObterPorMatriculaNaoExistente()));


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