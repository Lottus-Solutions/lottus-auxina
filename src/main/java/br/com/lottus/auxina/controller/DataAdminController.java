package br.com.lottus.auxina.controller;


import br.com.lottus.auxina.service.DataControllerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/data-admin")
@Tag(
        name = "Administração de Dados",
        description = "Endpoints administrativos para operações como limpeza de dados de teste"
)
public class DataAdminController {
    private final DataControllerService dataControllerService;

    public DataAdminController(DataControllerService dataControllerService) {
        this.dataControllerService = dataControllerService;
    }


    @Operation(
            summary = "Limpa todos os dados de teste",
            description = """
                      Dispara uma solicitação ao serviço de biblioteca para executar a limpeza completa 
                      dos dados de teste. Isso remove entidades temporárias usadas em testes automatizados.
                      """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comando de limpeza executado com sucesso",
                            content = @Content(mediaType = "text/plain")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Falha ao disparar o comando de limpeza",
                            content = @Content(mediaType = "text/plain")
                    )
            }
    )
    @PostMapping("/cleanup/all")
    public Mono<ResponseEntity<String>>cleanupAllTestData(){
        return dataControllerService.triggerCleanupAllTestData()
                .map(success -> success ? ResponseEntity.ok("Comando de limpeza geral enviado com sucesso") :
                        ResponseEntity.status(500).body("Falha ao enviar comando de limpeza geral."));
    }

}
