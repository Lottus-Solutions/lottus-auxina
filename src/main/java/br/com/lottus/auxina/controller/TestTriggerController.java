package br.com.lottus.auxina.controller;

import br.com.lottus.auxina.dto.ModuleTestDTO;
import br.com.lottus.auxina.dto.TestResult;
import br.com.lottus.auxina.service.*;
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
    private final CategoriaTestService categoriaTestService;
    private final AlunoTestService alunoTestService;
    private final EmprestimoTestService emprestimoTestService;
    private final TurmaTestService turmaTestService;

    public TestTriggerController(LivroTestService livroTestService, CategoriaTestService categoriaTestService,
                                 AlunoTestService alunoTestService, EmprestimoTestService emprestimoTestService,
                                 TurmaTestService turmaTestService) {
        this.livroTestService = livroTestService;
        this.categoriaTestService = categoriaTestService;
        this.alunoTestService = alunoTestService;
        this.emprestimoTestService = emprestimoTestService;
        this.turmaTestService = turmaTestService;
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

    @PostMapping("module/alunos")
    public Mono<ResponseEntity<ModuleTestDTO>> triggerAllAlunosModuleTests(){

        if(alunoTestService != null && alunoTestService.getClass().getDeclaredMethods().length > 1){
            return alunoTestService.runAllAlunoTests()
                    .map(ResponseEntity::ok);
        }

        return Mono.just(ResponseEntity.ok(ModuleTestDTO.builder().moduleName("Alunos (Não implementado Completamente)").build()));
    }

    @PostMapping("module/emprestimos")
    public Mono<ResponseEntity<ModuleTestDTO>> triggerAllEmprestimosModuleTests(){

        if(emprestimoTestService != null && emprestimoTestService.getClass().getDeclaredMethods().length > 1){
            return emprestimoTestService.runAllEmprestimoTests()
                    .map(ResponseEntity::ok);
        }

        return Mono.just(ResponseEntity.ok(ModuleTestDTO.builder().moduleName("Alunos (Não implementado Completamente)").build()));
    }

    @PostMapping("module/turmas")
    public Mono<ResponseEntity<ModuleTestDTO>> triggerAllTurmasModuleTests() {
        if (turmaTestService != null && turmaTestService.getClass().getDeclaredMethods().length > 1) {
            return turmaTestService.runAllTurmaTests()
                    .map(ResponseEntity::ok);
        }
        return Mono.just(ResponseEntity.ok(ModuleTestDTO.builder().moduleName("Turmas (Não implementado Completamente)").build()));
    }

}