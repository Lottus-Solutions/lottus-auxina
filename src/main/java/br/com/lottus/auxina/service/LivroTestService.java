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

    private static final String CATEGORIA_ID_1 = "1";
    private static final String CATEGORIA_ID_2 = "2";
    private static final String CATEGORIA_ID_3 = "3";
    private static final String CATEGORIA_ID_INEXISTENTE = "99999";

    private static final String LIVRO_ID_1_REVOLUCAO = "1";
    private static final String LIVRO_ID_2_DOM_CASMURRO = "2";
    private static final String LIVRO_ID_3_SENHOR_DOS_ANEIS = "3";
    private static final String LIVRO_ID_4_1984 = "4";
    private static final String LIVRO_ID_5_PEQUENO_PRINCIPE = "5";
    private static final String LIVRO_ID_6_O_HOBBIT = "6";
    private static final String LIVRO_ID_INEXISTENTE = "99999";


    public LivroTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private static final String GROUP_CADASTRAR_LIVRO = "CadastrarLivro";
    private static final String GROUP_EDITAR_LIVRO = "EditarLivro";
    private static final String GROUP_REMOVER_LIVRO = "RemoverLivro";
    private static final String GROUP_BUSCAR_FILTRAR_LIVROS = "BuscarFiltrarLivros"; // Grupo unificado

    private Map<String, Object> getBaseLivroBody(String nome, String autor, int quantidade, String categoriaId, String descricao) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        body.put("autor", autor);
        body.put("quantidade", quantidade);
        body.put("categoriaId", categoriaId);
        body.put("descricao", descricao);
        return body;
    }

    private Map<String, Object> getBaseCadastroLivroBody(String categoriaId) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Faker::Book.title");
        body.put("autor", "Faker::Name.fullName");
        body.put("quantidade", "Faker::Number.numberBetween(1,100)");
        body.put("categoriaId", categoriaId);
        body.put("descricao", "Faker::Lorem.sentence(5,10)");
        return body;
    }

    // --- Cadastrar Livro (POST /livros) ---
    private TestCaseConfigDTO getConfigCadastrar_C1_RevolucaoBichos() {
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_C1_RevolucaoBichos").methodGroupKey(GROUP_CADASTRAR_LIVRO)
                .httpMethod("POST").endpoint("/livros")
                .requestBodyTemplate(getBaseLivroBody("A Revolução dos Bichos", "George Orwell", 10, CATEGORIA_ID_1, "Uma sátira política."))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C2_CategoriaInexistente() {
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_C2_Erro_CategoriaInexistente").methodGroupKey(GROUP_CADASTRAR_LIVRO)
                .httpMethod("POST").endpoint("/livros")
                .requestBodyTemplate(getBaseLivroBody("Livro com Categoria Ruim", "Autor Desconhecido", 5, CATEGORIA_ID_INEXISTENTE, "Descrição"))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C3_NomeEmBranco() {
        Map<String, Object> body = getBaseLivroBody("Faker::Book.title", "Autor Válido", 5, CATEGORIA_ID_2, "Descrição");
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_C3_Erro_NomeEmBranco").methodGroupKey(GROUP_CADASTRAR_LIVRO)
                .httpMethod("POST").endpoint("/livros").requestBodyTemplate(body)
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C4_Livro1984_QtdZero() {
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_C4_1984_QtdZero").methodGroupKey(GROUP_CADASTRAR_LIVRO)
                .httpMethod("POST").endpoint("/livros")
                .requestBodyTemplate(getBaseLivroBody("1984", "George Orwell", 0, CATEGORIA_ID_2, "Distopia clássica."))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigCadastrar_C5_OHobbit_QtdUm() {
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_C5_OHobbit_QtdUm").methodGroupKey(GROUP_CADASTRAR_LIVRO)
                .httpMethod("POST").endpoint("/livros")
                .requestBodyTemplate(getBaseLivroBody("O Hobbit", "J.R.R. Tolkien", 1, CATEGORIA_ID_3, "Aventura na Terra Média."))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    // Cadastros adicionais para outros cenários (setup implícito)
    private TestCaseConfigDTO getConfigSetupCadastrarDomCasmurro() {
        return TestCaseConfigDTO.builder().testName("Livro_Setup_DomCasmurro").methodGroupKey(GROUP_CADASTRAR_LIVRO)
                .httpMethod("POST").endpoint("/livros").requestBodyTemplate(getBaseLivroBody("Dom Casmurro", "Machado de Assis", 7, CATEGORIA_ID_1, "Clássico brasileiro."))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetupCadastrarSenhorDosAneis() {
        return TestCaseConfigDTO.builder().testName("Livro_Setup_SenhorDosAneis").methodGroupKey(GROUP_CADASTRAR_LIVRO)
                .httpMethod("POST").endpoint("/livros").requestBodyTemplate(getBaseLivroBody("O Senhor dos Anéis", "J.R.R. Tolkien", 10, CATEGORIA_ID_2, "Trilogia épica."))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetupCadastrarPequenoPrincipe() {
        return TestCaseConfigDTO.builder().testName("Livro_Setup_PequenoPrincipe").methodGroupKey(GROUP_CADASTRAR_LIVRO)
                .httpMethod("POST").endpoint("/livros").requestBodyTemplate(getBaseLivroBody("O Pequeno Príncipe", "Antoine de Saint-Exupéry", 3, CATEGORIA_ID_3, "Encantador."))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    // Livros para testes de histórico de empréstimos
    private TestCaseConfigDTO getConfigCadastrarLivroFahrenheit451() {
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_Fahrenheit451").methodGroupKey(GROUP_CADASTRAR_LIVRO)
                .httpMethod("POST").endpoint("/livros").requestBodyTemplate(getBaseCadastroLivroBody(CATEGORIA_ID_1))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigCadastrarLivroAdmiravelMundoNovo() {
        return TestCaseConfigDTO.builder().testName("Livro_Cadastrar_AdmiravelMundoNovo").methodGroupKey(GROUP_CADASTRAR_LIVRO)
                .httpMethod("POST").endpoint("/livros").requestBodyTemplate(getBaseCadastroLivroBody(CATEGORIA_ID_1))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    // --- Editar Livro (PUT /livros/{id}) ---
    private TestCaseConfigDTO getConfigEditar_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("Livro_Editar_C1_Sucesso").methodGroupKey(GROUP_EDITAR_LIVRO)
                .httpMethod("PUT").endpoint("/livros/" + LIVRO_ID_1_REVOLUCAO)
                .requestBodyTemplate(getBaseLivroBody("A Revolução dos Bichos (Edição Revisada)", "G. Orwell Revisado", 10, CATEGORIA_ID_1, "Nova descrição."))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditar_C2_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Livro_Editar_C2_Erro_NaoExiste").methodGroupKey(GROUP_EDITAR_LIVRO)
                .httpMethod("PUT").endpoint("/livros/" + LIVRO_ID_INEXISTENTE)
                .requestBodyTemplate(getBaseLivroBody("Nome Qualquer", "Autor Qualquer", 5, CATEGORIA_ID_1, "Desc"))
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigEditar_C3_CategoriaInexistente() {
        return TestCaseConfigDTO.builder().testName("Livro_Editar_C3_Erro_CategoriaInexistente").methodGroupKey(GROUP_EDITAR_LIVRO)
                .httpMethod("PUT").endpoint("/livros/" + LIVRO_ID_2_DOM_CASMURRO)
                .requestBodyTemplate(getBaseLivroBody("Dom Casmurro", "Machado de Assis", 7, CATEGORIA_ID_INEXISTENTE, "Clássico."))
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    private TestCaseConfigDTO getConfigEditar_C4_AtualizarQuantidade() {
        Map<String, Object> body = getBaseLivroBody("O Senhor dos Anéis", "J.R.R. Tolkien", 15, CATEGORIA_ID_2, "Trilogia épica.");
        return TestCaseConfigDTO.builder().testName("Livro_Editar_C4_AtualizarQuantidade").methodGroupKey(GROUP_EDITAR_LIVRO)
                .httpMethod("PUT").endpoint("/livros/" + LIVRO_ID_3_SENHOR_DOS_ANEIS).requestBodyTemplate(body)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditar_C5_ApenasDescricao() {
        Map<String, Object> body = new HashMap<>(); body.put("descricao", "Uma jornada filosófica e poética.");
        return TestCaseConfigDTO.builder().testName("Livro_Editar_C5_ApenasDescricao").methodGroupKey(GROUP_EDITAR_LIVRO)
                .httpMethod("PUT").endpoint("/livros/" + LIVRO_ID_5_PEQUENO_PRINCIPE).requestBodyTemplate(body)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // --- Remover Livro (DELETE /livros/{id}) ---
    private TestCaseConfigDTO getConfigRemover_C1_Sucesso() {
        return TestCaseConfigDTO.builder().testName("Livro_Remover_C1_Sucesso").methodGroupKey(GROUP_REMOVER_LIVRO)
                .httpMethod("DELETE").endpoint("/livros/" + LIVRO_ID_2_DOM_CASMURRO)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build(); // Ajustado para 200 OK
    }
    private TestCaseConfigDTO getConfigRemover_C2_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Livro_Remover_C2_Erro_NaoExiste").methodGroupKey(GROUP_REMOVER_LIVRO)
                .httpMethod("DELETE").endpoint("/livros/" + LIVRO_ID_INEXISTENTE)
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRemover_C3_JaRemovido() {
        return TestCaseConfigDTO.builder().testName("Livro_Remover_C3_Erro_JaRemovido").methodGroupKey(GROUP_REMOVER_LIVRO)
                .httpMethod("DELETE").endpoint("/livros/" + LIVRO_ID_2_DOM_CASMURRO)
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRemover_C4_ComEmprestimosAtivos() {
        return TestCaseConfigDTO.builder().testName("Livro_Remover_C4_Erro_ComEmprestimos").methodGroupKey(GROUP_REMOVER_LIVRO)
                .httpMethod("DELETE").endpoint("/livros/" + LIVRO_ID_1_REVOLUCAO)
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build(); // Ou 409
    }

    // --- Buscar/Filtrar Livros (GET /livros?...) ---
    private TestCaseConfigDTO getConfigBuscar_C1_SemFiltroPaginado() {
        Map<String, String> qp = new HashMap<>(); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_Buscar_C1_SemFiltroPaginado").methodGroupKey(GROUP_BUSCAR_FILTRAR_LIVROS)
                .httpMethod("GET").endpoint("/livros").queryParamsTemplate(qp)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigBuscar_C2_PorTermoNome() {
        Map<String, String> qp = new HashMap<>(); qp.put("valor", "Revolução"); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_Buscar_C2_PorTermoNome").methodGroupKey(GROUP_BUSCAR_FILTRAR_LIVROS)
                .httpMethod("GET").endpoint("/livros").queryParamsTemplate(qp)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigBuscar_C3_PorCategoria() {
        Map<String, String> qp = new HashMap<>(); qp.put("categoriaId", CATEGORIA_ID_1); qp.put("pagina", "0"); qp.put("tamanho", "10");
        return TestCaseConfigDTO.builder().testName("Livro_Buscar_C3_PorCategoria").methodGroupKey(GROUP_BUSCAR_FILTRAR_LIVROS)
                .httpMethod("GET").endpoint("/livros").queryParamsTemplate(qp)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigBuscar_C4_PorStatusReservado() {
        Map<String, String> qp = new HashMap<>(); qp.put("status", "RESERVADO"); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_Buscar_C4_PorStatusReservado").methodGroupKey(GROUP_BUSCAR_FILTRAR_LIVROS)
                .httpMethod("GET").endpoint("/livros").queryParamsTemplate(qp)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigBuscar_C5_TermoECategoria() {
        Map<String, String> qp = new HashMap<>(); qp.put("valor", "Senhor"); qp.put("categoriaId", CATEGORIA_ID_2); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_Buscar_C5_TermoECategoria").methodGroupKey(GROUP_BUSCAR_FILTRAR_LIVROS)
                .httpMethod("GET").endpoint("/livros").queryParamsTemplate(qp)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigBuscar_AposRemocaoDomCasmurro() { // Para RemoverLivro C5
        Map<String, String> qp = new HashMap<>(); qp.put("valor", "Casmurro"); qp.put("pagina", "0"); qp.put("tamanho", "5");
        return TestCaseConfigDTO.builder().testName("Livro_Buscar_AposRemocaoDomCasmurro").methodGroupKey(GROUP_BUSCAR_FILTRAR_LIVROS)
                .httpMethod("GET").endpoint("/livros").queryParamsTemplate(qp)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build(); // Espera lista vazia
    }

    public Mono<ModuleTestDTO> runAllLivroTests() {
        List<Mono<TestResult>> testMonos = new ArrayList<>();

        // Cadastrar Livro
        testMonos.add(testExecutionService.executeTest(getConfigCadastrar_C1_RevolucaoBichos()));
        testMonos.add(testExecutionService.executeTest(getConfigSetupCadastrarDomCasmurro()));
        testMonos.add(testExecutionService.executeTest(getConfigSetupCadastrarSenhorDosAneis()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrar_C4_Livro1984_QtdZero()));
        testMonos.add(testExecutionService.executeTest(getConfigSetupCadastrarPequenoPrincipe()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrar_C5_OHobbit_QtdUm()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarLivroFahrenheit451()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrarLivroAdmiravelMundoNovo()));

        testMonos.add(testExecutionService.executeTest(getConfigCadastrar_C2_CategoriaInexistente()));
        testMonos.add(testExecutionService.executeTest(getConfigCadastrar_C3_NomeEmBranco()));

        // Atualizar Livro
        testMonos.add(testExecutionService.executeTest(getConfigEditar_C1_Sucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigEditar_C2_NaoExiste()));
        testMonos.add(testExecutionService.executeTest(getConfigEditar_C3_CategoriaInexistente()));
        testMonos.add(testExecutionService.executeTest(getConfigEditar_C4_AtualizarQuantidade()));
        testMonos.add(testExecutionService.executeTest(getConfigEditar_C5_ApenasDescricao()));

        // Buscar/Filtrar Livros
        testMonos.add(testExecutionService.executeTest(getConfigBuscar_C1_SemFiltroPaginado()));
        testMonos.add(testExecutionService.executeTest(getConfigBuscar_C2_PorTermoNome()));
        testMonos.add(testExecutionService.executeTest(getConfigBuscar_C3_PorCategoria()));
        testMonos.add(testExecutionService.executeTest(getConfigBuscar_C4_PorStatusReservado()));
        testMonos.add(testExecutionService.executeTest(getConfigBuscar_C5_TermoECategoria()));

//        // Remover Livro (A ordem importa)
//        testMonos.add(testExecutionService.executeTest(getConfigRemover_C1_Sucesso()));
//        testMonos.add(testExecutionService.executeTest(getConfigBuscar_AposRemocaoDomCasmurro())); // Verifica busca após remoção
//        testMonos.add(testExecutionService.executeTest(getConfigRemover_C2_NaoExiste()));
//        testMonos.add(testExecutionService.executeTest(getConfigRemover_C3_JaRemovido()));
//        testMonos.add(testExecutionService.executeTest(getConfigRemover_C4_ComEmprestimosAtivos()));


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