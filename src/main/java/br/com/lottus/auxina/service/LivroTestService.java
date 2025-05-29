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
public class LivroTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;
    private static final String DEFAULT_EXISTING_LIVRO_ID = "1";
    private static final String DEFAULT_EXISTING_CATEGORIA_ID = "1";

    public LivroTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    // --- Grupo: Listar Livros Paginado ---
    private static final String GROUP_LISTAR_PAGINADO = "ListarLivrosPaginado";

    private TestCaseConfigDTO getConfigListarPaginadoSucesso() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pagina", "0");
        queryParams.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_ListarPaginado_Sucesso").methodGroupKey(GROUP_LISTAR_PAGINADO).httpMethod("GET").endpoint("/livros").queryParamsTemplate(queryParams).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigListarPaginadoPaginaZero() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pagina", "0");
        queryParams.put("tamanho", "2");
        return TestCaseConfigDTO.builder().testName("Livro_ListarPaginado_PaginaZero").methodGroupKey(GROUP_LISTAR_PAGINADO).httpMethod("GET").endpoint("/livros").queryParamsTemplate(queryParams).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigListarPaginadoPaginaNegativa() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pagina", "-1");
        queryParams.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_ListarPaginado_Erro_PaginaNegativa").methodGroupKey(GROUP_LISTAR_PAGINADO).httpMethod("GET").endpoint("/livros").queryParamsTemplate(queryParams).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigListarPaginadoTamanhoZero() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pagina", "0");
        queryParams.put("tamanho", "0");
        return TestCaseConfigDTO.builder().testName("Livro_ListarPaginado_Erro_TamanhoZero").methodGroupKey(GROUP_LISTAR_PAGINADO).httpMethod("GET").endpoint("/livros").queryParamsTemplate(queryParams).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigListarPaginadoTamanhoGrande() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pagina", "0");
        queryParams.put("tamanho", "1001");
        return TestCaseConfigDTO.builder().testName("Livro_ListarPaginado_Erro_TamanhoGrande").methodGroupKey(GROUP_LISTAR_PAGINADO).httpMethod("GET").endpoint("/livros").queryParamsTemplate(queryParams).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // --- Grupo: Cadastrar Livro ---
    private static final String GROUP_CADASTRAR_LIVRO = "CadastrarLivro";

    private Map<String, Object> getBaseCadastroLivroBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Faker::Book.title");
        body.put("autor", "Faker::Name.fullName");
        body.put("quantidade", "Faker::Number.numberBetween(1,100)");
        body.put("categoriaId", DEFAULT_EXISTING_CATEGORIA_ID);
        body.put("descricao", "Faker::Lorem.sentence(5,5)");
        return body;
    }

    private TestCaseConfigDTO getConfigCadastrarLivroSucesso() {
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_Sucesso").methodGroupKey(GROUP_CADASTRAR_LIVRO).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getBaseCadastroLivroBody()).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    private TestCaseConfigDTO getConfigCadastrarLivroNomeEmBranco() {
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_Erro_NomeEmBranco").methodGroupKey(GROUP_CADASTRAR_LIVRO).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getBaseCadastroLivroBody()).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigCadastrarLivroAutorEmBranco() {
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_Erro_AutorEmBranco").methodGroupKey(GROUP_CADASTRAR_LIVRO).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getBaseCadastroLivroBody()).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigCadastrarLivroQuantidadeNegativa() {
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_Erro_QuantidadeNegativa").methodGroupKey(GROUP_CADASTRAR_LIVRO).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getBaseCadastroLivroBody()).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigCadastrarLivroCategoriaInexistente() {
        Map<String, Object> body = getBaseCadastroLivroBody();
        body.put("categoriaId", "99999");
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_Erro_CategoriaInexistente").methodGroupKey(GROUP_CADASTRAR_LIVRO).httpMethod("POST").endpoint("/livros").requestBodyTemplate(body).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // --- Grupo: Buscar Livro (por termo) ---
    private static final String GROUP_BUSCAR_LIVRO_TERMO = "BuscarLivroPorTermo";

    private TestCaseConfigDTO getConfigBuscarTermoSucesso() {
        Map<String, String> qp = new HashMap<>(); qp.put("valor", "Potter"); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_BuscarTermo_Sucesso").methodGroupKey(GROUP_BUSCAR_LIVRO_TERMO).httpMethod("GET").endpoint("/livros/buscar").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigBuscarTermoNaoEncontrado() {
        Map<String, String> qp = new HashMap<>(); qp.put("valor", "TermoInexistente" + faker.random().hex(8)); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_BuscarTermo_NaoEncontrado").methodGroupKey(GROUP_BUSCAR_LIVRO_TERMO).httpMethod("GET").endpoint("/livros/buscar").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigBuscarTermoVazio() {
        Map<String, String> qp = new HashMap<>(); qp.put("valor", ""); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_BuscarTermo_Erro_TermoVazio").methodGroupKey(GROUP_BUSCAR_LIVRO_TERMO).httpMethod("GET").endpoint("/livros/buscar").queryParamsTemplate(qp).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigBuscarTermoPaginacaoInvalida() {
        Map<String, String> qp = new HashMap<>(); qp.put("valor", "a"); qp.put("pagina", "-1"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_BuscarTermo_Erro_PaginacaoInvalida").methodGroupKey(GROUP_BUSCAR_LIVRO_TERMO).httpMethod("GET").endpoint("/livros/buscar").queryParamsTemplate(qp).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigBuscarTermoSemParametro() {
        return TestCaseConfigDTO.builder().testName("Livro_BuscarTermo_Erro_SemParametro").methodGroupKey(GROUP_BUSCAR_LIVRO_TERMO).httpMethod("GET").endpoint("/livros/buscar").queryParamsTemplate(new HashMap<>()).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // --- Grupo: Filtrar por Categoria ---
    private static final String GROUP_FILTRAR_CATEGORIA = "FiltrarLivroPorCategoria";

    private TestCaseConfigDTO getConfigFiltrarCategoriaUmaIdSucesso() {
        Map<String, String> qp = new HashMap<>(); qp.put("categoriaIds", DEFAULT_EXISTING_CATEGORIA_ID);
        return TestCaseConfigDTO.builder().testName("Livro_FiltrarCategoria_UmaId_Sucesso").methodGroupKey(GROUP_FILTRAR_CATEGORIA).httpMethod("GET").endpoint("/livros/filtrar-por-categoria").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigFiltrarCategoriaMultiplasIds() {
        Map<String, String> qp = new HashMap<>(); qp.put("categoriaIds", "1,2,3");
        return TestCaseConfigDTO.builder().testName("Livro_FiltrarCategoria_MultiplasIds_Sucesso").methodGroupKey(GROUP_FILTRAR_CATEGORIA).httpMethod("GET").endpoint("/livros/filtrar-por-categoria").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigFiltrarCategoriaIdNaoExistente() {
        Map<String, String> qp = new HashMap<>(); qp.put("categoriaIds", "99999");
        return TestCaseConfigDTO.builder().testName("Livro_FiltrarCategoria_IdNaoExistente").methodGroupKey(GROUP_FILTRAR_CATEGORIA).httpMethod("GET").endpoint("/livros/filtrar-por-categoria").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigFiltrarCategoriaIdInvalida() {
        Map<String, String> qp = new HashMap<>(); qp.put("categoriaIds", "abc");
        return TestCaseConfigDTO.builder().testName("Livro_FiltrarCategoria_Erro_IdInvalida").methodGroupKey(GROUP_FILTRAR_CATEGORIA).httpMethod("GET").endpoint("/livros/filtrar-por-categoria").queryParamsTemplate(qp).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    private TestCaseConfigDTO getConfigFiltrarCategoriaSemParametro() {
        return TestCaseConfigDTO.builder().testName("Livro_FiltrarCategoria_Erro_SemParametro").methodGroupKey(GROUP_FILTRAR_CATEGORIA).httpMethod("GET").endpoint("/livros/filtrar-por-categoria").queryParamsTemplate(new HashMap<>()).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // --- Grupo: Filtrar por Status ---
    private static final String GROUP_FILTRAR_STATUS = "FiltrarLivroPorStatus";

    private TestCaseConfigDTO getConfigFiltrarStatusDisponivel() {
        Map<String, String> qp = new HashMap<>(); qp.put("statusvalue", "DISPONIVEL"); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_FiltrarStatus_Disponivel_Sucesso").methodGroupKey(GROUP_FILTRAR_STATUS).httpMethod("GET").endpoint("/livros/filtrar-por-status").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigFiltrarStatusReservado() {
        Map<String, String> qp = new HashMap<>(); qp.put("statusvalue", "RESERVADO"); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_FiltrarStatus_Reservado_Sucesso").methodGroupKey(GROUP_FILTRAR_STATUS).httpMethod("GET").endpoint("/livros/filtrar-por-status").queryParamsTemplate(qp).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    private TestCaseConfigDTO getConfigFiltrarStatusInvalido() {
        Map<String, String> qp = new HashMap<>(); qp.put("statusvalue", "XYZ"); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_FiltrarStatus_Erro_StatusInvalido").methodGroupKey(GROUP_FILTRAR_STATUS).httpMethod("GET").endpoint("/livros/filtrar-por-status").queryParamsTemplate(qp).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    // Adicionar mais 2 testes aqui (sem status, paginação inválida)

    // --- Grupo: Editar Livro ---
    private static final String GROUP_EDITAR_LIVRO = "EditarLivro";

    private TestCaseConfigDTO getConfigEditarLivroSucesso() {
        Map<String, Object> body = getBaseCadastroLivroBody(); body.put("nome", "Livro Editado - " + "Faker::Lorem.word()");
        return TestCaseConfigDTO.builder().testName("Livro_Editar_Sucesso").methodGroupKey(GROUP_EDITAR_LIVRO).httpMethod("PUT").endpoint("/livros/" + DEFAULT_EXISTING_LIVRO_ID).requestBodyTemplate(body).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    // Adicionar mais 4 testes aqui (ID não existente, corpo inválido, etc.)

    // --- Grupo: Deletar Livro ---
    private static final String GROUP_DELETAR_LIVRO = "DeletarLivro";

    private TestCaseConfigDTO getConfigDeletarLivroSucesso() {
        String idParaDeletar = "2";
        return TestCaseConfigDTO.builder().testName("Livro_Deletar_Sucesso").methodGroupKey(GROUP_DELETAR_LIVRO).httpMethod("DELETE").endpoint("/livros/" + idParaDeletar).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(204).build();
    }

    private TestCaseConfigDTO getConfigDeletarLivroIdNaoExistente() {
        String idNaoExistente = "999999";
        return TestCaseConfigDTO.builder().testName("Livro_Deletar_Erro_IdNaoExistente").methodGroupKey(GROUP_DELETAR_LIVRO).httpMethod("DELETE").endpoint("/livros/" + idNaoExistente).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    // Adicionar mais 3 testes aqui (ID formato inválido, deletar livro com empréstimo ativo, etc.)

    public Mono<ModuleTestDTO> runAllLivroTests() {
        List<Mono<TestResult>> testMonos = new ArrayList<>();

        testMonos.addAll(Arrays.asList(
                testExecutionService.executeTest(getConfigListarPaginadoSucesso()),
                testExecutionService.executeTest(getConfigListarPaginadoPaginaZero()),
                testExecutionService.executeTest(getConfigListarPaginadoPaginaNegativa()),
                testExecutionService.executeTest(getConfigListarPaginadoTamanhoZero()),
                testExecutionService.executeTest(getConfigListarPaginadoTamanhoGrande())
        ));
        testMonos.addAll(Arrays.asList(
                testExecutionService.executeTest(getConfigCadastrarLivroSucesso()),
                testExecutionService.executeTest(getConfigCadastrarLivroNomeEmBranco()),
                testExecutionService.executeTest(getConfigCadastrarLivroAutorEmBranco()),
                testExecutionService.executeTest(getConfigCadastrarLivroQuantidadeNegativa()),
                testExecutionService.executeTest(getConfigCadastrarLivroCategoriaInexistente())
        ));
        testMonos.addAll(Arrays.asList(
                testExecutionService.executeTest(getConfigBuscarTermoSucesso()),
                testExecutionService.executeTest(getConfigBuscarTermoNaoEncontrado()),
                testExecutionService.executeTest(getConfigBuscarTermoVazio()),
                testExecutionService.executeTest(getConfigBuscarTermoPaginacaoInvalida()),
                testExecutionService.executeTest(getConfigBuscarTermoSemParametro())
        ));
        testMonos.addAll(Arrays.asList(
                testExecutionService.executeTest(getConfigFiltrarCategoriaUmaIdSucesso()),
                testExecutionService.executeTest(getConfigFiltrarCategoriaMultiplasIds()),
                testExecutionService.executeTest(getConfigFiltrarCategoriaIdNaoExistente()),
                testExecutionService.executeTest(getConfigFiltrarCategoriaIdInvalida()),
                testExecutionService.executeTest(getConfigFiltrarCategoriaSemParametro())
        ));
        testMonos.addAll(Arrays.asList(
                testExecutionService.executeTest(getConfigFiltrarStatusDisponivel()),
                testExecutionService.executeTest(getConfigFiltrarStatusReservado()),
                testExecutionService.executeTest(getConfigFiltrarStatusInvalido())
        ));
        testMonos.addAll(Arrays.asList(
                testExecutionService.executeTest(getConfigEditarLivroSucesso())
        ));
        testMonos.addAll(Arrays.asList(
                testExecutionService.executeTest(getConfigDeletarLivroIdNaoExistente()),
                testExecutionService.executeTest(getConfigDeletarLivroSucesso())
        ));

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
                            .moduleName("Livros")
                            .totalTests(totalModuleTests)
                            .sucessfulTests((int) successfulModuleTests)
                            .failedTests(totalModuleTests - (int) successfulModuleTests)
                            .successPercentage(moduleSuccessPercentage)
                            .methodTestsResults(methodSummaries)
                            .build();
                });
    }
}