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
public class LivroTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    // --- IDs de Recursos ---
    private static final String CATEGORIA_ID_1 = "1";
    private static final String CATEGORIA_ID_2 = "2";
    private static final String CATEGORIA_ID_3 = "3";
    private static final String CATEGORIA_ID_INEXISTENTE = "999";

    // IDs de Livros (alguns são usados em EmpréstimoTestService)
    private static final String LIVRO_ID_1_REVOLUCAO = "1"; // Tem empréstimo ativo, não pode ser removido
    private static final String LIVRO_ID_2_DOM_CASMURRO = "2";
    private static final String LIVRO_ID_3_SENHOR_ANEIS = "3";
    private static final String LIVRO_ID_4_1984 = "4";
    private static final String LIVRO_ID_5_PEQUENO_PRINCIPE = "5";
    private static final String LIVRO_ID_6_O_HOBBIT = "6";
    private static final String LIVRO_ID_7_PARA_REMOVER = "7"; // Livro dedicado para remoção
    private static final String LIVRO_ID_INEXISTENTE = "999";

    // --- GRUPOS DE TESTE POR MÉTODO ---
    private static final String GROUP_0_SETUP = "0. Setup";
    private static final String GROUP_1_CADASTRAR = "1. Cadastrar Livro";
    private static final String GROUP_2_BUSCAR_FILTRAR = "2. Buscar e Filtrar Livros";
    private static final String GROUP_3_EDITAR = "3. Editar Livro";
    private static final String GROUP_4_REMOVER = "4. Remover Livro";

    public LivroTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private Mono<TestResult> executeAndLog(TestCaseConfigDTO config) {
        return testExecutionService.executeTest(config)
                .doOnSubscribe(subscription -> log.info("➡️  INICIANDO TESTE DE LIVRO: {}", config.getTestName()));
    }

    private Map<String, Object> getLivroBody(String nome, String autor, int quantidade, String categoriaId, String descricao) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        body.put("autor", autor);
        body.put("quantidade", quantidade);
        body.put("categoriaId", categoriaId);
        body.put("descricao", descricao);
        return body;
    }

    // =================================================================================
    // 0. SETUP - CADASTRO DOS LIVROS BASE
    // =================================================================================
    private TestCaseConfigDTO getConfigSetup_RevolucaoBichos() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_RevolucaoBichos").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getLivroBody("A Revolução dos Bichos", "George Orwell", 10, CATEGORIA_ID_1, "Uma sátira política.")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_DomCasmurro() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_DomCasmurro").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getLivroBody("Dom Casmurro", "Machado de Assis", 7, CATEGORIA_ID_1, "Clássico brasileiro.")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_SenhorDosAneis() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_SenhorDosAneis").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getLivroBody("O Senhor dos Anéis", "J.R.R. Tolkien", 10, CATEGORIA_ID_2, "Trilogia épica.")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_1984() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_1984_QtdZero").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getLivroBody("1984", "George Orwell", 0, CATEGORIA_ID_2, "Distopia clássica.")).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigSetup_PequenoPrincipe() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_PequenoPrincipe").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getLivroBody("O Pequeno Príncipe", "Antoine de Saint-Exupéry", 3, CATEGORIA_ID_2, "Encantador.")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_OHobbit() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_OHobbit_QtdUm").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getLivroBody("O Hobbit", "J.R.R. Tolkien", 1, CATEGORIA_ID_2, "Aventura na Terra Média.")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetup_LivroParaRemover() {
        return TestCaseConfigDTO.builder().testName("Setup_Cadastrar_LivroParaRemover").methodGroupKey(GROUP_0_SETUP).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getLivroBody("A Droga da Obediência", "Pedro Bandeira", 1, CATEGORIA_ID_1, "Série Os Karas.")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    // =================================================================================
    // 1. CADASTRAR LIVRO (Cenários de Erro)
    // =================================================================================
    private TestCaseConfigDTO getConfigCadastrar_C1_Erro_CategoriaInexistente() {
        return TestCaseConfigDTO.builder().testName("Cadastrar_C1_Erro_CategoriaInexistente").methodGroupKey(GROUP_1_CADASTRAR).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getLivroBody("Livro com Categoria Ruim", "Autor Desconhecido", 5, CATEGORIA_ID_INEXISTENTE, "Descrição")).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C2_Erro_NomeEmBranco() {
        return TestCaseConfigDTO.builder().testName("Cadastrar_C2_Erro_NomeEmBranco").methodGroupKey(GROUP_1_CADASTRAR).httpMethod("POST").endpoint("/livros").requestBodyTemplate(getLivroBody("", "Autor Válido", 5, CATEGORIA_ID_2, "Descrição")).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // =================================================================================
// 2. BUSCAR E FILTRAR LIVROS (VERSÃO CORRIGIDA)
// =================================================================================

    private TestCaseConfigDTO getConfigBuscar_C1_SemFiltroPaginado() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pagina", "0");
        queryParams.put("tamanho", "5");

        return TestCaseConfigDTO.builder()
                .testName("Buscar_C1_SemFiltroPaginado")
                .methodGroupKey(GROUP_2_BUSCAR_FILTRAR)
                .httpMethod("GET")
                .endpoint("/livros") // Apenas o caminho base
                .queryParamsTemplate(queryParams) // Parâmetros separados
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(200)
                .build();
    }

    private TestCaseConfigDTO getConfigBuscar_C2_PorTermoNome() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("valor", "Revolução");

        return TestCaseConfigDTO.builder()
                .testName("Buscar_C2_PorTermoNome_Revolucao")
                .methodGroupKey(GROUP_2_BUSCAR_FILTRAR)
                .httpMethod("GET")
                .endpoint("/livros") // Apenas o caminho base
                .queryParamsTemplate(queryParams) // Parâmetros separados
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(200)
                .build();
    }

    private TestCaseConfigDTO getConfigBuscar_C3_PorCategoria() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("categoriaId", CATEGORIA_ID_3);

        return TestCaseConfigDTO.builder()
                .testName("Buscar_C3_PorCategoria")
                .methodGroupKey(GROUP_2_BUSCAR_FILTRAR)
                .httpMethod("GET")
                .endpoint("/livros") // Apenas o caminho base
                .queryParamsTemplate(queryParams) // Parâmetros separados
                .scenarioType(ScenarioType.HAPPY_PATH)
                .expectedHtppStatus(200)
                .build();
    }

    // =================================================================================
    // 3. EDITAR LIVRO
    // =================================================================================
    private TestCaseConfigDTO getConfigEditar_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("Editar_C1_Sucesso_TodosCampos").methodGroupKey(GROUP_3_EDITAR).httpMethod("PUT").endpoint("/livros/" + LIVRO_ID_3_SENHOR_ANEIS).requestBodyTemplate(getLivroBody("O Senhor dos Anéis (Ed. Revisada)", "J.R.R. Tolkien", 12, CATEGORIA_ID_2, "Nova descrição épica.")).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditar_C2_Erro_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Editar_C2_Erro_NaoExiste").methodGroupKey(GROUP_3_EDITAR).httpMethod("PUT").endpoint("/livros/" + LIVRO_ID_INEXISTENTE).requestBodyTemplate(getLivroBody("Nome Qualquer", "Autor Qualquer", 5, CATEGORIA_ID_1, "Desc")).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigEditar_C3_Erro_CategoriaInexistente() {
        return TestCaseConfigDTO.builder().testName("Editar_C3_Erro_CategoriaInexistente").methodGroupKey(GROUP_3_EDITAR).httpMethod("PUT").endpoint("/livros/" + LIVRO_ID_2_DOM_CASMURRO).requestBodyTemplate(getLivroBody("Dom Casmurro", "Machado de Assis", 7, CATEGORIA_ID_INEXISTENTE, "Clássico.")).scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }

    // =================================================================================
    // 4. REMOVER LIVRO
    // =================================================================================
    private TestCaseConfigDTO getConfigRemover_C1_Sucesso_SemEmprestimo() {
        return TestCaseConfigDTO.builder().testName("Remover_C1_Sucesso_SemEmprestimo").methodGroupKey(GROUP_4_REMOVER).httpMethod("DELETE").endpoint("/livros/" + LIVRO_ID_6_O_HOBBIT).scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(204).build();
    }

    private TestCaseConfigDTO getConfigRemover_C3_Erro_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Remover_C3_Erro_NaoExiste").methodGroupKey(GROUP_4_REMOVER).httpMethod("DELETE").endpoint("/livros/" + LIVRO_ID_INEXISTENTE).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRemover_C4_Erro_JaRemovido() {
        return TestCaseConfigDTO.builder().testName("Remover_C4_Erro_JaRemovido").methodGroupKey(GROUP_4_REMOVER).httpMethod("DELETE").endpoint("/livros/" + LIVRO_ID_7_PARA_REMOVER).scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }

    public Mono<ModuleTestDTO> runAllLivroTests() {
        List<TestCaseConfigDTO> testConfigsInOrder = new ArrayList<>();

        // FASE 0: SETUP
        testConfigsInOrder.add(getConfigSetup_RevolucaoBichos());
        testConfigsInOrder.add(getConfigSetup_DomCasmurro());
        testConfigsInOrder.add(getConfigSetup_SenhorDosAneis());
        testConfigsInOrder.add(getConfigSetup_1984());
        testConfigsInOrder.add(getConfigSetup_PequenoPrincipe());
        testConfigsInOrder.add(getConfigSetup_OHobbit());
        testConfigsInOrder.add(getConfigSetup_LivroParaRemover());

        // FASE 1: CADASTRAR (CENÁRIOS DE ERRO)
        testConfigsInOrder.add(getConfigCadastrar_C1_Erro_CategoriaInexistente());
        testConfigsInOrder.add(getConfigCadastrar_C2_Erro_NomeEmBranco());

        // FASE 2: BUSCAR E FILTRAR
        testConfigsInOrder.add(getConfigBuscar_C1_SemFiltroPaginado());
        testConfigsInOrder.add(getConfigBuscar_C2_PorTermoNome());
        testConfigsInOrder.add(getConfigBuscar_C3_PorCategoria());

        // FASE 3: EDITAR
        testConfigsInOrder.add(getConfigEditar_C1_Sucesso());
        testConfigsInOrder.add(getConfigEditar_C2_Erro_NaoExiste());
        testConfigsInOrder.add(getConfigEditar_C3_Erro_CategoriaInexistente());

        // FASE 4: REMOVER E VERIFICAR
        testConfigsInOrder.add(getConfigRemover_C1_Sucesso_SemEmprestimo());
        testConfigsInOrder.add(getConfigRemover_C4_Erro_JaRemovido());
        testConfigsInOrder.add(getConfigRemover_C3_Erro_NaoExiste());

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