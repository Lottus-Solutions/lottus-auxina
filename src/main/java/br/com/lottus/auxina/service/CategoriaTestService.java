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
public class CategoriaTestService {

    private final TestExecutionService testExecutionService;
    private final Faker faker;

    private static final String CATEGORIA_ID_1_AVENTURA = "1";
    private static final String CATEGORIA_ID_2_CIENCIA = "2";
    private static final String CATEGORIA_ID_3_HISTORIA = "3";
    private static final String CATEGORIA_ID_4_FANTASIA_NOVA = "4"; // Assumindo que este será o ID da nova categoria "Fantasia"
    private static final String CATEGORIA_ID_INEXISTENTE = "999";
    private static final String CATEGORIA_COR_PADRAO_PARA_TESTE = "#FFA500"; // Laranja

    public CategoriaTestService(TestExecutionService testExecutionService, Faker faker) {
        this.testExecutionService = testExecutionService;
        this.faker = faker;
    }

    private static final String GROUP_ADICIONAR_CATEGORIA = "AdicionarCategoria";
    private static final String GROUP_LISTAR_CATEGORIAS = "ListarCategorias";
    private static final String GROUP_REMOVER_CATEGORIA = "RemoverCategoria";
    private static final String GROUP_EDITAR_CATEGORIA = "EditarCategoria";

    private Map<String, Object> getCategoriaBody(String nome) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        return body;
    }
    private Map<String, Object> getCategoriaBodyComCor(String nome, String cor) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        if (cor != null) { // Adiciona cor apenas se fornecida
            body.put("cor", cor);
        }
        return body;
    }

    // --- Adicionar Categoria ---
    // Setup: Cadastrar categorias base para os testes de "Adicionar" e outros módulos.
    private TestCaseConfigDTO getConfigSetupCategoriaAventura() {
        return TestCaseConfigDTO.builder().testName("Categoria_Setup_Aventura").methodGroupKey(GROUP_ADICIONAR_CATEGORIA)
                .httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("Aventura"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetupCategoriaCiencia() {
        return TestCaseConfigDTO.builder().testName("Categoria_Setup_Ciencia").methodGroupKey(GROUP_ADICIONAR_CATEGORIA)
                .httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("Ciência"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    private TestCaseConfigDTO getConfigSetupCategoriaHistoria() {
        return TestCaseConfigDTO.builder().testName("Categoria_Setup_Historia").methodGroupKey(GROUP_ADICIONAR_CATEGORIA)
                .httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBodyComCor("História", "#FFD700"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    // Cenário 1: Adicionar "Fantasia" com sucesso
    private TestCaseConfigDTO getConfigAdicionarCategoria_C1_FantasiaSucesso() {
        return TestCaseConfigDTO.builder().testName("Categoria_Adicionar_C1_FantasiaSucesso").methodGroupKey(GROUP_ADICIONAR_CATEGORIA)
                .httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("Fantasia"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }
    // Cenário 2: Tentar adicionar "Aventura" novamente
    private TestCaseConfigDTO getConfigAdicionarCategoria_C2_NomeJaExistente() {
        return TestCaseConfigDTO.builder().testName("Categoria_Adicionar_C2_Erro_NomeJaExistente").methodGroupKey(GROUP_ADICIONAR_CATEGORIA)
                .httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("Aventura"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(409).build(); // CategoriaJaExistenteException
    }
    // Cenário 3: Tentar adicionar com nome em branco
    private TestCaseConfigDTO getConfigAdicionarCategoria_C3_NomeEmBranco() {
        return TestCaseConfigDTO.builder().testName("Categoria_Adicionar_C3_Erro_NomeEmBranco").methodGroupKey(GROUP_ADICIONAR_CATEGORIA)
                .httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("Faker::Lorem.word()")) // Será invalidado
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    // Cenário 4: Tentar adicionar com corpo vazio
    private TestCaseConfigDTO getConfigAdicionarCategoria_C4_CorpoVazio() {
        return TestCaseConfigDTO.builder().testName("Categoria_Adicionar_C4_Erro_CorpoVazio").methodGroupKey(GROUP_ADICIONAR_CATEGORIA)
                .httpMethod("POST").endpoint("/categorias").requestBodyTemplate(new HashMap<>())
                .scenarioType(ScenarioType.INVALID_INPUT_BAD_REQUEST).expectedHtppStatus(400).build();
    }
    // Cenário 5: Adicionar "aventura" (case-insensitive - o BDD diz que o backend permite, então esperamos 201)
    private TestCaseConfigDTO getConfigAdicionarCategoria_C5_CaseInsensitive() {
        return TestCaseConfigDTO.builder().testName("Categoria_Adicionar_C5_Sucesso_CaseInsensitive").methodGroupKey(GROUP_ADICIONAR_CATEGORIA)
                .httpMethod("POST").endpoint("/categorias").requestBodyTemplate(getCategoriaBody("aventura"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(201).build();
    }

    // --- Listar Categorias ---
    private TestCaseConfigDTO getConfigListarCategorias_C1_ConteudoContagem() {
        return TestCaseConfigDTO.builder().testName("Categoria_Listar_C1_ConteudoContagem").methodGroupKey(GROUP_LISTAR_CATEGORIAS)
                .httpMethod("GET").endpoint("/categorias").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListarCategorias_C2_VerificarDTOHistoria() { // (O BDD espera ID 3, cor e qtd)
        return TestCaseConfigDTO.builder().testName("Categoria_Listar_C2_VerificarDTOHistoria").methodGroupKey(GROUP_LISTAR_CATEGORIAS)
                .httpMethod("GET").endpoint("/categorias").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListarCategorias_C3_AposNovaFantasia() { // (Após Fantasia ser adicionada no C1 de Adicionar)
        return TestCaseConfigDTO.builder().testName("Categoria_Listar_C3_AposNovaFantasia").methodGroupKey(GROUP_LISTAR_CATEGORIAS)
                .httpMethod("GET").endpoint("/categorias").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListarCategorias_C4_AposRemocaoHistoria() { // (Este teste depende do teste de remoção de História)
        return TestCaseConfigDTO.builder().testName("Categoria_Listar_C4_AposRemocaoHistoria").methodGroupKey(GROUP_LISTAR_CATEGORIAS)
                .httpMethod("GET").endpoint("/categorias").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigListarCategorias_C5_SemLivros() { // (Assume que o estado do BD não tem livros nas categorias listadas)
        return TestCaseConfigDTO.builder().testName("Categoria_Listar_C5_SemLivros").methodGroupKey(GROUP_LISTAR_CATEGORIAS)
                .httpMethod("GET").endpoint("/categorias").scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }

    // --- Remover Categoria ---
    private TestCaseConfigDTO getConfigRemoverCategoria_C1_Sucesso() { // Remove ID 3 (História)
        return TestCaseConfigDTO.builder().testName("Categoria_Remover_C1_Sucesso").methodGroupKey(GROUP_REMOVER_CATEGORIA)
                .httpMethod("DELETE").endpoint("/categorias/" + CATEGORIA_ID_3_HISTORIA)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(204).build(); // Ou 200
    }
    private TestCaseConfigDTO getConfigRemoverCategoria_C2_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Categoria_Remover_C2_Erro_NaoExiste").methodGroupKey(GROUP_REMOVER_CATEGORIA)
                .httpMethod("DELETE").endpoint("/categorias/" + CATEGORIA_ID_INEXISTENTE)
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigRemoverCategoria_C3_ComLivros() { // Remove ID 1 (Aventura), BDD diz que remove com sucesso
        return TestCaseConfigDTO.builder().testName("Categoria_Remover_C3_ComLivros_Permitido").methodGroupKey(GROUP_REMOVER_CATEGORIA)
                .httpMethod("DELETE").endpoint("/categorias/" + CATEGORIA_ID_1_AVENTURA)
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(204).build();
    }
    private TestCaseConfigDTO getConfigRemoverCategoria_C4_JaDeletada() { // Tenta remover ID 3 (História) novamente
        return TestCaseConfigDTO.builder().testName("Categoria_Remover_C4_Erro_JaDeletada").methodGroupKey(GROUP_REMOVER_CATEGORIA)
                .httpMethod("DELETE").endpoint("/categorias/" + CATEGORIA_ID_3_HISTORIA)
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    // C5 de Remover é mais uma asserção sobre o estado do sistema, difícil de modelar como um único TestCaseConfigDTO aqui.

    // --- Editar Categoria ---
    private TestCaseConfigDTO getConfigEditarCategoria_C1_Sucesso() { // Edita ID 2 (Ciência)
        return TestCaseConfigDTO.builder().testName("Categoria_Editar_C1_Sucesso").methodGroupKey(GROUP_EDITAR_CATEGORIA)
                .httpMethod("PUT").endpoint("/categorias/" + CATEGORIA_ID_2_CIENCIA)
                .requestBodyTemplate(getCategoriaBodyComCor("Ficção Científica", "#0000FF"))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditarCategoria_C2_NaoExiste() {
        return TestCaseConfigDTO.builder().testName("Categoria_Editar_C2_Erro_NaoExiste").methodGroupKey(GROUP_EDITAR_CATEGORIA)
                .httpMethod("PUT").endpoint("/categorias/" + CATEGORIA_ID_INEXISTENTE)
                .requestBodyTemplate(getCategoriaBody("Qualquer Nome"))
                .scenarioType(ScenarioType.RESOURCE_NOT_FOUND).expectedHtppStatus(404).build();
    }
    private TestCaseConfigDTO getConfigEditarCategoria_C3_NomeJaExistente() { // Edita ID 2 (agora Ficção Científica) para "Aventura"
        return TestCaseConfigDTO.builder().testName("Categoria_Editar_C3_Sucesso_NomeJaExistente").methodGroupKey(GROUP_EDITAR_CATEGORIA)
                .httpMethod("PUT").endpoint("/categorias/" + CATEGORIA_ID_2_CIENCIA) // ID da categoria que era "Ciência"
                .requestBodyTemplate(getCategoriaBody("Aventura")) // Nome da categoria ID 1
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build(); // BDD diz que é permitido
    }
    private TestCaseConfigDTO getConfigEditarCategoria_C4_ApenasCor() { // Edita ID 1 (Aventura)
        return TestCaseConfigDTO.builder().testName("Categoria_Editar_C4_ApenasCor").methodGroupKey(GROUP_EDITAR_CATEGORIA)
                .httpMethod("PUT").endpoint("/categorias/" + CATEGORIA_ID_1_AVENTURA)
                .requestBodyTemplate(getCategoriaBodyComCor("Aventura", CATEGORIA_COR_PADRAO_PARA_TESTE))
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build();
    }
    private TestCaseConfigDTO getConfigEditarCategoria_C5_NomeEmBranco() { // Edita ID 2 (que agora é "Aventura" após C3)
        return TestCaseConfigDTO.builder().testName("Categoria_Editar_C5_Sucesso_NomeEmBranco").methodGroupKey(GROUP_EDITAR_CATEGORIA)
                .httpMethod("PUT").endpoint("/categorias/" + CATEGORIA_ID_2_CIENCIA)
                .requestBodyTemplate(getCategoriaBody("Faker::Lorem.word()")) // Será invalidado para ""
                .scenarioType(ScenarioType.HAPPY_PATH).expectedHtppStatus(200).build(); // BDD diz que é permitido
    }


    public Mono<ModuleTestDTO> runAllCategoriaTests() {
        List<Mono<TestResult>> testMonos = new ArrayList<>();

        // Setup inicial
        testMonos.add(testExecutionService.executeTest(getConfigSetupCategoriaAventura()));    // ID 1
        testMonos.add(testExecutionService.executeTest(getConfigSetupCategoriaCiencia()));      // ID 2
        testMonos.add(testExecutionService.executeTest(getConfigSetupCategoriaHistoria()));     // ID 3

        // 1. Adicionar Categoria
        testMonos.add(testExecutionService.executeTest(getConfigAdicionarCategoria_C1_FantasiaSucesso())); // ID 4 (Fantasia)
        testMonos.add(testExecutionService.executeTest(getConfigAdicionarCategoria_C2_NomeJaExistente()));
        testMonos.add(testExecutionService.executeTest(getConfigAdicionarCategoria_C3_NomeEmBranco()));
        testMonos.add(testExecutionService.executeTest(getConfigAdicionarCategoria_C4_CorpoVazio()));
        testMonos.add(testExecutionService.executeTest(getConfigAdicionarCategoria_C5_CaseInsensitive())); // ID 5 (aventura)

        // 2. Listar Categorias
        testMonos.add(testExecutionService.executeTest(getConfigListarCategorias_C1_ConteudoContagem()));
        testMonos.add(testExecutionService.executeTest(getConfigListarCategorias_C2_VerificarDTOHistoria()));
        testMonos.add(testExecutionService.executeTest(getConfigListarCategorias_C3_AposNovaFantasia()));
        testMonos.add(testExecutionService.executeTest(getConfigListarCategorias_C5_SemLivros()));

        // 4. Editar Categoria
        testMonos.add(testExecutionService.executeTest(getConfigEditarCategoria_C1_Sucesso()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarCategoria_C2_NaoExiste()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarCategoria_C3_NomeJaExistente()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarCategoria_C4_ApenasCor()));
        testMonos.add(testExecutionService.executeTest(getConfigEditarCategoria_C5_NomeEmBranco()));

        // 3. Remover Categoria (A ordem importa em relação a Listar C4)
//        testMonos.add(testExecutionService.executeTest(getConfigRemoverCategoria_C1_Sucesso())); // Remove ID 3 (História)
//        testMonos.add(testExecutionService.executeTest(getConfigListarCategorias_C4_AposRemocaoHistoria())); // Verifica lista DEPOIS da remoção
//        testMonos.add(testExecutionService.executeTest(getConfigRemoverCategoria_C2_NaoExiste()));
//        testMonos.add(testExecutionService.executeTest(getConfigRemoverCategoria_C3_ComLivros())); // Remove ID 1 (Aventura)
//        testMonos.add(testExecutionService.executeTest(getConfigRemoverCategoria_C4_JaDeletada())); // Tenta remover ID 3 novamente

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
                            .moduleName("Categorias")
                            .totalTests(totalModuleTests)
                            .sucessfulTests((int) successfulModuleTests)
                            .failedTests(totalModuleTests - (int) successfulModuleTests)
                            .successPercentage(moduleSuccessPercentage)
                            .methodTestsResults(methodSummaries)
                            .build();
                });
    }
}