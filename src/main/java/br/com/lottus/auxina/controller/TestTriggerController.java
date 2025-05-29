package br.com.lottus.auxina.controller;

import br.com.lottus.auxina.dto.ModuleTestDTO;
import br.com.lottus.auxina.dto.TestResult;
import br.com.lottus.auxina.service.CategoriaTestService;
import br.com.lottus.auxina.service.LivroTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux; // Para executar múltiplos testes
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trigger-tests")
public class TestTriggerController {

    private final LivroTestService livroTestService;
    private final CategoriaTestService categoriaTestService; // Adicione @Autowired ou no construtor se for usar

    public TestTriggerController(LivroTestService livroTestService, CategoriaTestService categoriaTestService) {
        this.livroTestService = livroTestService;
        this.categoriaTestService = categoriaTestService;
    }

    @PostMapping("/module/livros")
    public Mono<ResponseEntity<ModuleTestDTO>> triggerAllLivroModuleTests() {
        return livroTestService.runAllLivroTests()
                .map(ResponseEntity::ok);
    }

    @PostMapping("/module/categorias")
    public Mono<ResponseEntity<ModuleTestDTO>> triggerAllCategoriaModuleTests() {
        // Implementar CategoriaTestService.runAllCategoriaTests()
        if (categoriaTestService != null && categoriaTestService.getClass().getDeclaredMethods().length > 1) { // Check to avoid NPE if not fully implemented
            return categoriaTestService.runAllCategoriaTests()
                    .map(ResponseEntity::ok);
        }
        return Mono.just(ResponseEntity.ok(ModuleTestDTO.builder().moduleName("Categorias (Não Implementado Completamente)").build()));
    }

    @PostMapping("/all-modules")
    public Flux<ModuleTestDTO> triggerAllDefinedModuleTests() {
        List<Mono<ModuleTestDTO>> allModuleMonos = new ArrayList<>();

        allModuleMonos.add(livroTestService.runAllLivroTests());

        if (categoriaTestService != null && categoriaTestService.getClass().getDeclaredMethods().length > 1) { // Check to avoid NPE if not fully implemented
            // allModuleMonos.add(categoriaTestService.runAllCategoriaTests()); // Descomente quando CategoriaTestService estiver pronto
        }
        // Adicione outros módulos aqui

        if (allModuleMonos.isEmpty()){
            return Flux.just(ModuleTestDTO.builder().moduleName("Nenhum módulo de teste encontrado/implementado").build());
        }
        return Flux.merge(allModuleMonos);
    }
}