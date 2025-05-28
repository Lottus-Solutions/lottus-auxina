package br.com.lottus.auxina.controller;

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
@RequestMapping("/api/v1/trigger-tests") // Endpoint para disparar os testes
public class TestTriggerController {

    private final LivroTestService livroTestService;
    private final CategoriaTestService categoriaTestService;
    // Injete outros XxxTestService aqui

    public TestTriggerController(LivroTestService livroTestService, CategoriaTestService categoriaTestService) {
        this.livroTestService = livroTestService;
        this.categoriaTestService = categoriaTestService;
    }

    @PostMapping("/livro/cadastrar-sucesso")
    public Mono<ResponseEntity<TestResult>> triggerCadastrarLivroSucesso() {
        return livroTestService.runCadastrarLivroSucesso().map(ResponseEntity::ok);
    }

    @PostMapping("/livro/cadastrar-nome-em-branco")
    public Mono<ResponseEntity<TestResult>> triggerCadastrarLivroNomeEmBranco() {
        return livroTestService.runCadastrarLivroNomeEmBranco().map(ResponseEntity::ok);
    }

    @PostMapping("/livro/buscar-paginado")
    public Mono<ResponseEntity<TestResult>> triggerBuscarLivrosPaginado() {
        return livroTestService.runBuscarLivrosPaginado().map(ResponseEntity::ok);
    }

    @PostMapping("/categoria/cadastrar-sucesso")
    public Mono<ResponseEntity<TestResult>> triggerCadastrarCategoriaSucesso() {
        return categoriaTestService.runCadastrarCategoriaSucesso().map(ResponseEntity::ok);
    }

    @PostMapping("/categoria/cadastrar-nome-em-branco")
    public Mono<ResponseEntity<TestResult>> triggerCadastrarCategoriaNomeEmBranco() {
        return categoriaTestService.runCadastrarCategoriaNomeEmBranco().map(ResponseEntity::ok);
    }

    // Endpoint para rodar um conjunto de testes (ex: todos os testes de livro)
    @PostMapping("/livro/all")
    public Flux<TestResult> triggerAllLivroTests() {
        List<Mono<TestResult>> testMonos = new ArrayList<>();
        testMonos.add(livroTestService.runCadastrarLivroSucesso());
        testMonos.add(livroTestService.runCadastrarLivroNomeEmBranco());
        testMonos.add(livroTestService.runBuscarLivrosPaginado());
        // Adicione outros testes de livro aqui

        // Executa os Monos em paralelo (ou sequencialmente se preferir com concatMap)
        return Flux.merge(testMonos);
    }

    // Endpoint para rodar TODOS os testes definidos
    @PostMapping("/all-defined")
    public Flux<TestResult> triggerAllDefinedTests() {
        List<Mono<TestResult>> allTestMonos = new ArrayList<>();
        // Livro
        allTestMonos.add(livroTestService.runCadastrarLivroSucesso());
        allTestMonos.add(livroTestService.runCadastrarLivroNomeEmBranco());
        allTestMonos.add(livroTestService.runBuscarLivrosPaginado());
        // Categoria
        allTestMonos.add(categoriaTestService.runCadastrarCategoriaSucesso());
        allTestMonos.add(categoriaTestService.runCadastrarCategoriaNomeEmBranco());
        // Adicione chamadas para outros XxxTestService aqui

        return Flux.merge(allTestMonos);
    }
}