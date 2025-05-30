package br.com.lottus.auxina.service;

import com.github.javafaker.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class DataControllerService {

    private static final Logger logger = LoggerFactory.getLogger(DataControllerService.class);

    private final WebClient libraryServiceCLient;

    public DataControllerService(WebClient libraryServiceCLient) {
        this.libraryServiceCLient = libraryServiceCLient;
    }

    public Mono<Boolean> triggerCleanupAllTestData(){
        final String cleanupAllEndpoint = "/admin/db/cleanup/all-data";
        logger.info("Disparando limpeza de todos os dados de teste do service alvo");

        return libraryServiceCLient.post()
                .uri(cleanupAllEndpoint)
                .retrieve()
                .toBodilessEntity()
                .map(responseEntity ->{
                        boolean success = responseEntity.getStatusCode().is2xxSuccessful();
                if(success) {
                      logger.info("Comando de limpeza de todos os dados de testes enviados com sucesso");
                }else{
                    logger.error("Falha ao enviar comando de limpeza de todos os dados de teste para {}. Status: {}. Corpo da resposta (se houver): {}",
                            cleanupAllEndpoint, responseEntity.getStatusCode(), responseEntity.toString());
                }

                return success;
     })
                .onErrorResume(e ->{
                    logger.error("Erro de comunicação ao tentar disparar limpeza de dados");

                    return Mono.just(false);
                });
    }

}
