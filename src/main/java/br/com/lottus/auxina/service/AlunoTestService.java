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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlunoTestService {

    // 1. ADICIONAR UM LOGGER À CLASSE
    private static final Logger logger = LoggerFactory.getLogger(AlunoTestService.class);

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    private static final String TURMA_ID_1 = "1";
    private static final String TURMA_ID_2 = "2";
    private static final String TURMA_ID_INEXISTENTE = "99999";

    private static final String DEFAULT_EXISTING_TURMA_ID_PARA_TESTE_ALUNO = "1";
    private static final String MATRICULA_ALUNO_HIST_COM_ATIVO_PARA_REMOVER = "6";

    private static final String MATRICULA_CARLOS_ANDRADE = "1";
    private static final String MATRICULA_FERNANDA_LIMA = "2";
    private static final String MATRICULA_RICARDO_PEREIRA = "3";
    private static final String MATRICULA_MARIANA_COSTA = "4";
    private static final String MATRICULA_ALUNO_HIST_VARIOS = "5";
    private static final String MATRICULA_ALUNO_PARA_REMOVER_SEM_DEPENDENCIAS = "8";
    private static final String MATRICULA_INEXISTENTE = "99999";
    private static final String MATRICULA_FORMATO_INVALIDO = "abc";


    public AlunoTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private static final String GROUP_CADASTRAR_ALUNO = "CadastrarAluno";
    private static final String GROUP_EDITAR_ALUNO = "EditarAluno";
    private static final String GROUP_REMOVER_ALUNO = "RemoverAluno";
    private static final String GROUP_BUSCAR_LISTAR_ALUNOS = "BuscarListarAlunos";
    private static final String GROUP_PERFIL_ALUNO = "ConstruirPerfilAluno";
    private static final String GROUP_LISTAR_TURMAS_NO_ALUNO_CTRL = "ListarTurmasNoAlunoCtrl";


    private Map<String, Object> getAlunoBody(String nome, String turmaId, int qtdBonus, int qtdLivrosLidos) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        body.put("qtdBonus", qtdBonus);
        body.put("turmaId", turmaId);
        body.put("qtdLivrosLidos", qtdLivrosLidos);
        return body;
    }

    // --- Cadastrar Aluno (BDDs e Setup) ---
    private TestCaseConfigDTO getConfigCadastrar_C1_CarlosAndrade() {
        return TestCaseConfigDTO.builder().testName("Aluno_Cadastrar_C1_CarlosAndrade").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Carlos Andrade", TURMA_ID_1, 0, 0))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetupCadastrarFernandaLima() {
        return TestCaseConfigDTO.builder().testName("Aluno_Setup_FernandaLima").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Fernanda Lima", TURMA_ID_1, 0, 0))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetupCadastrarRicardoPereira() {
        return TestCaseConfigDTO.builder().testName("Aluno_Setup_RicardoPereira").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Ricardo Pereira", TURMA_ID_1, 0, 0))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetupCadastrarMarianaCosta() {
        return TestCaseConfigDTO.builder().testName("Aluno_Setup_MarianaCosta").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Mariana Costa", TURMA_ID_1, 0, 0))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetupCadastrarAlunoHistVarios() {
        return TestCaseConfigDTO.builder().testName("Aluno_Setup_AlunoHistVarios").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Aluno Hist Varios", TURMA_ID_2, 10, 4))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    private TestCaseConfigDTO getConfigCadastrarAluno_ParaHistoricoComAtivo() { // Esperado ter matrícula "8"
        return TestCaseConfigDTO.builder().testName("Aluno_Cadastrar_ParaHistoricoComAtivo").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar")
                .requestBodyTemplate(getAlunoBody("Aluno Hist Com Ativo", DEFAULT_EXISTING_TURMA_ID_PARA_TESTE_ALUNO, 5, 3))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    private TestCaseConfigDTO getConfigSetupCadastrarAlunoParaRemoverSemDependencias() {
        return TestCaseConfigDTO.builder().testName("Aluno_Setup_ParaRemoverSemDependencias").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Aluno Sem Emprestimo Para Remover", TURMA_ID_2, 0, 0))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C2_TurmaInexistente() {
        return TestCaseConfigDTO.builder().testName("Aluno_Cadastrar_C2_Erro_TurmaInexistente").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Aluno Turma Ruim", TURMA_ID_INEXISTENTE, 0, 0))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C3_NomeEmBranco() {
        return TestCaseConfigDTO.builder().testName("Aluno_Cadastrar_C3_Erro_NomeEmBranco").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Faker::Name.fullName", TURMA_ID_1, 0,0))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C4_LivrosLidosNegativo() {
        return TestCaseConfigDTO.builder().testName("Aluno_Cadastrar_C4_Erro_LivrosLidosNegativo").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Aluno Livros Neg", TURMA_ID_1, 0, -5))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C5_BonusNegativo() {
        return TestCaseConfigDTO.builder().testName("Aluno_Cadastrar_C5_Erro_BonusNegativo").methodGroupKey(GROUP_CADASTRAR_ALUNO)
                .httpMethod("POST").endpoint("/alunos/cadastrar").requestBodyTemplate(getAlunoBody("Aluno Bonus Neg", TURMA_ID_1, -2, 0))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // --- Editar Aluno (BDDs) ---
    private TestCaseConfigDTO getConfigEditar_C1_TodosDadosSucesso() {
        Map<String, Object> body = getAlunoBody("Carlos Andrade Editado", TURMA_ID_2, 10, 5);
        body.put("matricula", MATRICULA_CARLOS_ANDRADE);
        return TestCaseConfigDTO.builder().testName("Aluno_Editar_C1_TodosDadosSucesso").methodGroupKey(GROUP_EDITAR_ALUNO)
                .httpMethod("PUT").endpoint("/alunos/editar/" + MATRICULA_CARLOS_ANDRADE).requestBodyTemplate(body)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditar_C2_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Aluno_Editar_C2_Erro_NaoExiste").methodGroupKey(GROUP_EDITAR_ALUNO)
                .httpMethod("PUT").endpoint("/alunos/editar/" + MATRICULA_INEXISTENTE)
                .requestBodyTemplate(getAlunoBody("Nome Qualquer", TURMA_ID_1, 0,0))
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigEditar_C3_TurmaInexistente() {
        Map<String, Object> body = getAlunoBody("Fernanda Lima Editada", TURMA_ID_INEXISTENTE, 0, 0);
        body.put("matricula", MATRICULA_FERNANDA_LIMA);
        return TestCaseConfigDTO.builder().testName("Aluno_Editar_C3_Erro_TurmaInexistente").methodGroupKey(GROUP_EDITAR_ALUNO)
                .httpMethod("PUT").endpoint("/alunos/editar/" + MATRICULA_FERNANDA_LIMA).requestBodyTemplate(body)
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigEditar_C4_ApenasNome() {
        Map<String, Object> body = new HashMap<>(); body.put("nome", "Ricardo P. Silva");
        body.put("matricula", MATRICULA_RICARDO_PEREIRA); // O DTO de edição pode precisar da matricula no corpo
        body.put("turma_id", TURMA_ID_1); // E outros campos obrigatórios ou que não podem ser nulos
        body.put("qtd_bonus", 0);
        body.put("qtd_livros_lidos", 0);
        return TestCaseConfigDTO.builder().testName("Aluno_Editar_C4_ApenasNome").methodGroupKey(GROUP_EDITAR_ALUNO)
                .httpMethod("PUT").endpoint("/alunos/editar/" + MATRICULA_RICARDO_PEREIRA).requestBodyTemplate(body)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditar_C5_ApenasBonus() {
        Map<String, Object> body = getAlunoBody("Mariana Costa", TURMA_ID_1, 1, 0); // qtdBonus é int, BDD fala de 1.5, ajustar DTO se necessário
        body.put("matricula", MATRICULA_MARIANA_COSTA);
        return TestCaseConfigDTO.builder().testName("Aluno_Editar_C5_ApenasBonus").methodGroupKey(GROUP_EDITAR_ALUNO)
                .httpMethod("PUT").endpoint("/alunos/editar/" + MATRICULA_MARIANA_COSTA).requestBodyTemplate(body)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // --- Remover Aluno (BDDs) ---

    private TestCaseConfigDTO getConfigRemover_C1_SemDependenciasSucesso() {
        return TestCaseConfigDTO.builder().testName("Aluno_Remover_C1_SemDependenciasSucesso").methodGroupKey(GROUP_REMOVER_ALUNO)
                .httpMethod("DELETE").endpoint("/alunos/remover/" + MATRICULA_ALUNO_HIST_COM_ATIVO_PARA_REMOVER) // Matricula "8"
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build(); // Controller retorna 200 OK com corpo
    }
    private TestCaseConfigDTO getConfigRemover_C2_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Aluno_Remover_C2_Erro_NaoExiste").methodGroupKey(GROUP_REMOVER_ALUNO)
                .httpMethod("DELETE").endpoint("/alunos/remover/" + MATRICULA_INEXISTENTE)
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRemover_C3_ComEmprestimosAtivos() {
        return TestCaseConfigDTO.builder().testName("Aluno_Remover_C3_Erro_ComEmprestimosAtivos").methodGroupKey(GROUP_REMOVER_ALUNO)
                .httpMethod("DELETE").endpoint("/alunos/remover/" + MATRICULA_CARLOS_ANDRADE)
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build(); // Ou 409, DataIntegrity
    }
    private TestCaseConfigDTO getConfigRemover_C4_JaRemovido() {
        return TestCaseConfigDTO.builder().testName("Aluno_Remover_C4_Erro_JaRemovido").methodGroupKey(GROUP_REMOVER_ALUNO)
                .httpMethod("DELETE").endpoint("/alunos/remover/" + MATRICULA_ALUNO_HIST_COM_ATIVO_PARA_REMOVER)
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    // C5 (Verificar lista após remoção) é um teste do grupo BUSCAR_LISTAR_ALUNOS

    // --- Buscar/Listar Alunos (BDDs) ---
    private TestCaseConfigDTO getConfigListar_C1_AlunosDaTurma1() {
        return TestCaseConfigDTO.builder().testName("Aluno_BuscarListar_C1_AlunosDaTurma1").methodGroupKey(GROUP_BUSCAR_LISTAR_ALUNOS)
                .httpMethod("GET").endpoint("/alunos/turma/" + TURMA_ID_1)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigBuscar_C2_PorMatriculaCarlos() {
        return TestCaseConfigDTO.builder().testName("Aluno_BuscarListar_C2_PorMatriculaCarlos").methodGroupKey(GROUP_BUSCAR_LISTAR_ALUNOS)
                .httpMethod("GET").endpoint("/alunos/" + MATRICULA_CARLOS_ANDRADE)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigBuscar_C3_PorParteNomeCar() {
        return TestCaseConfigDTO.builder().testName("Aluno_BuscarListar_C3_PorParteNomeCar").methodGroupKey(GROUP_BUSCAR_LISTAR_ALUNOS)
                .httpMethod("GET").endpoint("/alunos/nome/car") // Ajustar se o endpoint for /buscar com query param
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListar_C4_AlunosTurmaVazia() { // BDD diz que lança NenhumAlunoEncontrado, o que pode ser 404 ou 200 com lista vazia. Ajustar conforme backend.
        return TestCaseConfigDTO.builder().testName("Aluno_BuscarListar_C4_AlunosTurmaVazia").methodGroupKey(GROUP_BUSCAR_LISTAR_ALUNOS)
                .httpMethod("GET").endpoint("/alunos/turma/" + TURMA_ID_2) // Assumindo que Turma 2 ainda não tem alunos ou após setup
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build(); // Ajustar se for 200 com lista vazia
    }
    private TestCaseConfigDTO getConfigBuscar_C5_AlunoNomeTurmaCarlos() {
        return TestCaseConfigDTO.builder().testName("Aluno_BuscarListar_C5_AlunoNomeTurmaCarlos").methodGroupKey(GROUP_BUSCAR_LISTAR_ALUNOS)
                .httpMethod("GET").endpoint("/alunos/buscar-aluno-nome-turma/" + TURMA_ID_1 + "/Carlos")
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListarAlunosAposRemocaoMatricula8() { // Para Remover C5
        return TestCaseConfigDTO.builder().testName("Aluno_BuscarListar_AposRemocaoMatricula8").methodGroupKey(GROUP_BUSCAR_LISTAR_ALUNOS)
                .httpMethod("GET").endpoint("/alunos/turma/" + TURMA_ID_2) // Turma do aluno removido
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // --- Construir Perfil Aluno (BDDs) ---
    private TestCaseConfigDTO getConfigPerfil_C1_RicardoSemEmprestimos() {
        return TestCaseConfigDTO.builder().testName("Aluno_Perfil_C1_RicardoSemEmprestimos").methodGroupKey(GROUP_PERFIL_ALUNO)
                .httpMethod("GET").endpoint("/alunos/perfil/" + MATRICULA_RICARDO_PEREIRA)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigPerfil_C2_CarlosComEmprestimoAtivo() {
        return TestCaseConfigDTO.builder().testName("Aluno_Perfil_C2_CarlosComEmprestimoAtivo").methodGroupKey(GROUP_PERFIL_ALUNO)
                .httpMethod("GET").endpoint("/alunos/perfil/" + MATRICULA_CARLOS_ANDRADE)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigPerfil_C3_FernandaComEmprestimoAtrasado() {
        return TestCaseConfigDTO.builder().testName("Aluno_Perfil_C3_FernandaComEmprestimoAtrasado").methodGroupKey(GROUP_PERFIL_ALUNO)
                .httpMethod("GET").endpoint("/alunos/perfil/" + MATRICULA_FERNANDA_LIMA)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigPerfil_C4_AlunoInexistente() {
        return TestCaseConfigDTO.builder().testName("Aluno_Perfil_C4_Erro_AlunoInexistente").methodGroupKey(GROUP_PERFIL_ALUNO)
                .httpMethod("GET").endpoint("/alunos/perfil/" + MATRICULA_INEXISTENTE)
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigPerfil_C5_VerificarLivrosLidos() {
        return TestCaseConfigDTO.builder().testName("Aluno_Perfil_C5_VerificarLivrosLidos").methodGroupKey(GROUP_PERFIL_ALUNO)
                .httpMethod("GET").endpoint("/alunos/perfil/" + MATRICULA_ALUNO_HIST_VARIOS)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // Outros testes que estavam no seu AlunoTestService
    private TestCaseConfigDTO getConfigListarTodosAlunosGeral() { // GET /alunos
        return TestCaseConfigDTO.builder().testName("Aluno_ListarTodos_Geral").methodGroupKey(GROUP_BUSCAR_LISTAR_ALUNOS).httpMethod("GET").endpoint("/alunos").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListarTurmasViaAlunoCtrl() { // GET /alunos/listar-turmas
        return TestCaseConfigDTO.builder().testName("Aluno_ListarTurmas_ViaAlunoCtrl").methodGroupKey(GROUP_LISTAR_TURMAS_NO_ALUNO_CTRL).httpMethod("GET").endpoint("/alunos/listar-turmas").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private Mono<TestResult> executeAndLog(TestCaseConfigDTO config) {
        return testExecutionService.executeTest(config)
                .doOnSubscribe(subscription ->
                        logger.info("➡️  INICIANDO EXECUÇÃO DO TESTE: {}", config.getTestName())
                );
    }

    public Mono<ModuleTestDTO> runAllAlunoTests() {
        List<Mono<TestResult>> testMonos = new ArrayList<>();

        // Setup: Cadastrar alunos na ordem esperada para os testes de Empréstimo e para os BDDs
        testMonos.add(executeAndLog(getConfigCadastrar_C1_CarlosAndrade()));
        testMonos.add(executeAndLog(getConfigSetupCadastrarFernandaLima()));
        testMonos.add(executeAndLog(getConfigSetupCadastrarRicardoPereira()));
        testMonos.add(executeAndLog(getConfigSetupCadastrarMarianaCosta()));
        testMonos.add(executeAndLog(getConfigSetupCadastrarAlunoHistVarios()));
        testMonos.add(executeAndLog(getConfigSetupCadastrarAlunoParaRemoverSemDependencias()));

        // Cadastrar Aluno (BDDs de erro)
        testMonos.add(executeAndLog(getConfigCadastrar_C2_TurmaInexistente()));
        testMonos.add(executeAndLog(getConfigCadastrar_C3_NomeEmBranco()));
        testMonos.add(executeAndLog(getConfigCadastrar_C4_LivrosLidosNegativo()));
        testMonos.add(executeAndLog(getConfigCadastrar_C5_BonusNegativo()));

        // Editar Aluno (BDDs)
        testMonos.add(executeAndLog(getConfigEditar_C1_TodosDadosSucesso()));
        testMonos.add(executeAndLog(getConfigEditar_C2_NaoExiste()));
        testMonos.add(executeAndLog(getConfigEditar_C3_TurmaInexistente()));
        testMonos.add(executeAndLog(getConfigEditar_C4_ApenasNome()));
        testMonos.add(executeAndLog(getConfigEditar_C5_ApenasBonus()));

        // Buscar/Listar Alunos (BDDs)
        testMonos.add(executeAndLog(getConfigListar_C1_AlunosDaTurma1()));
        testMonos.add(executeAndLog(getConfigBuscar_C2_PorMatriculaCarlos()));
        testMonos.add(executeAndLog(getConfigBuscar_C3_PorParteNomeCar()));
        testMonos.add(executeAndLog(getConfigListar_C4_AlunosTurmaVazia()));
        testMonos.add(executeAndLog(getConfigBuscar_C5_AlunoNomeTurmaCarlos()));
        testMonos.add(executeAndLog(getConfigListarTodosAlunosGeral()));
        testMonos.add(executeAndLog(getConfigListarTurmasViaAlunoCtrl()));

        // Construir Perfil Aluno (BDDs)
        testMonos.add(executeAndLog(getConfigPerfil_C1_RicardoSemEmprestimos()));
        testMonos.add(executeAndLog(getConfigPerfil_C2_CarlosComEmprestimoAtivo()));
        testMonos.add(executeAndLog(getConfigPerfil_C3_FernandaComEmprestimoAtrasado()));
        testMonos.add(executeAndLog(getConfigPerfil_C4_AlunoInexistente()));
        testMonos.add(executeAndLog(getConfigPerfil_C5_VerificarLivrosLidos()));

        // Remover Aluno (BDDs) - A ordem é importante
        testMonos.add(executeAndLog(getConfigRemover_C1_SemDependenciasSucesso()));
        testMonos.add(executeAndLog(getConfigListarAlunosAposRemocaoMatricula8()));
        testMonos.add(executeAndLog(getConfigRemover_C2_NaoExiste()));
        testMonos.add(executeAndLog(getConfigRemover_C3_ComEmprestimosAtivos()));
        testMonos.add(executeAndLog(getConfigRemover_C4_JaRemovido()));


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