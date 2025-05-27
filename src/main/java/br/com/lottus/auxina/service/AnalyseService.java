package br.com.lottus.auxina.service;

import br.com.lottus.auxina.dto.ActuatorMetricsResponse;
import br.com.lottus.auxina.dto.ScenarioType;
import br.com.lottus.auxina.dto.TestCaseConfigDTO;
import br.com.lottus.auxina.dto.TestResult;
import com.github.javafaker.Faker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AnalyseService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyseService.class);

    private final WebClient libraryServiceClient;
    private final Faker faker;
    private final MeterRegistry meterRegistry;

    public AnalyseService(WebClient libraryServiceClient, Faker faker, MeterRegistry meterRegistry) {
        this.libraryServiceClient = libraryServiceClient;
        this.faker = faker;
        this.meterRegistry = meterRegistry;
    }

    public Mono<TestResult> performanceLibraryServicePaginationTest() {
        final String testName = "LibraryServiceTesteDePaginacao";
        final String targetEndpoint = "/livros";

        int pagina = faker.number().numberBetween(0, 5);
        int tamanho = faker.number().numberBetween(5, 15);

        long startTime = System.nanoTime();
        Timer.Sample sample = Timer.start(meterRegistry);

        return libraryServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(targetEndpoint)
                        .queryParam("pagina", pagina)
                        .queryParam("tamanho", tamanho)
                        .build())
                .exchangeToMono(clientResponse -> {
                    long durationNanos = System.nanoTime() - startTime;
                    boolean success = clientResponse.statusCode().is2xxSuccessful();

                    Timer specificTestTimer = meterRegistry.timer(testName + ".duration", "success", String.valueOf(success));
                    specificTestTimer.record(durationNanos, TimeUnit.NANOSECONDS);

                    TestResult.TestResultBuilder resultBuilder = TestResult.builder()
                            .testName(testName)
                            .targetEndpoint(targetEndpoint + "?pagina=" + pagina + "&tamanho=" + tamanho)
                            .success(success)
                            .durationMillis(TimeUnit.NANOSECONDS.toMillis(durationNanos))
                            .httpStatus(clientResponse.statusCode().value());

                    return clientResponse.releaseBody()
                            .then(Mono.defer(() -> {
                                if (success) {
                                    return getBookServiceMemoryUsage()
                                            .map(memoryMb -> resultBuilder.targetServiceMemoryUsedMB(memoryMb).build())
                                            .defaultIfEmpty(resultBuilder.build());
                                } else {
                                    return Mono.just(resultBuilder.build());
                                }
                            }));
                })
                .onErrorResume(ex -> {
                    long durationNanos = System.nanoTime() - startTime;
                    logger.error("Erro de comunicação ao executar teste {}: {}", testName, ex.getMessage());

                    Timer specificTestTimer = meterRegistry.timer(testName + ".duration", "success", "false");
                    specificTestTimer.record(durationNanos, TimeUnit.NANOSECONDS);

                    return Mono.just(TestResult.builder()
                            .testName(testName)
                            .targetEndpoint(targetEndpoint + "?pagina=" + pagina + "&tamanho=" + tamanho)
                            .success(false)
                            .durationMillis(TimeUnit.NANOSECONDS.toMillis(durationNanos))
                            .httpStatus(0)
                            .build());
                })
                .doFinally(signalType -> {
                    sample.stop(meterRegistry.timer(testName + ".overall.duration"));
                    logger.info("Teste {} concluído com sinal: {}", testName, signalType);
                });
    }

    private Mono<Double> getBookServiceMemoryUsage() {
        final String memoryMetricEndpoint = "/actuator/metrics/jvm.memory.used";
        return libraryServiceClient.get()
                .uri(memoryMetricEndpoint)
                .retrieve()
                .bodyToMono(ActuatorMetricsResponse.class)
                .map(metricResponse -> {
                    if (metricResponse != null && metricResponse.getMeasurements() != null && !metricResponse.getMeasurements().isEmpty()) {
                        double memoryBytes = metricResponse.getMeasurements().get(0).getValue();
                        return memoryBytes / (1024.0 * 1024.0);
                    }
                    logger.warn("Resposta da métrica de memória vazia ou malformada para {}", memoryMetricEndpoint);
                    return null;
                })
                .doOnError(e -> logger.warn("Falha ao obter métrica de memória do serviço alvo ({}): {}", memoryMetricEndpoint, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }




    public Mono<TestResult> executarConfigurableTest(TestCaseConfigDTO config){
        long startTime = System.nanoTime();

        Timer.Sample sample = Timer.start(meterRegistry);

        Object requestBody = generateRequestBody(config);
        Map<String, String> queryParams = generateQueryParams(config);

        WebClient.RequestBodySpec requestBodySpec = null;
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = null;

        switch(config.getHttpMethod().toUpperCase()) {
            case "POST":
                requestBodySpec = libraryServiceClient.post().uri(config.getEndpoint());

                if(requestBody != null){
                        requestHeadersSpec = requestBodySpec.bodyValue(requestBody);
                } else {
                    requestHeadersSpec = requestBodySpec;
                }

                break;

            case "PUT":
                requestBodySpec = libraryServiceClient.put().uri(config.getEndpoint());

                if(requestBody != null){
                    requestHeadersSpec = requestBodySpec.bodyValue(requestBody);
                }else{
                    requestHeadersSpec = requestBodySpec;
                }

            case "GET":
                default:
                    requestHeadersSpec = libraryServiceClient.get().uri(uriBuilder -> {
                        uriBuilder.path(config.getEndpoint());
                        queryParams.forEach(uriBuilder::queryParam);

                        return uriBuilder.build();
                    });
                    break;

        }

        return requestHeadersSpec
                .exchangeToMono(clientResponse -> {
                    long durationNanos = System.nanoTime() - startTime;

                    boolean success = clientResponse.statusCode().value() == config.getExpectedHtppStatus();

                    Timer specificTestTimer = meterRegistry.timer(config.getTestName() + ".duration",
                            "sucess", String.valueOf(success));

                    specificTestTimer.record(durationNanos, TimeUnit.NANOSECONDS);

                    TestResult.TestResultBuilder resultBuilder = TestResult.builder()
                            .testName(config.getTestName())
                            .targetEndpoint(config.getEndpoint() + "params: "+ queryParams + ")")
                            .success(success)
                            .durationMillis(TimeUnit.NANOSECONDS.toMillis(durationNanos))
                            .httpStatus(clientResponse.statusCode().value());

                    return clientResponse.releaseBody()
                            .then(Mono.defer(() -> {
                                if (clientResponse.statusCode().is2xxSuccessful()) { // Apenas busca memória se foi 2xx (mesmo que o esperado fosse erro)
                                    return getBookServiceMemoryUsage()
                                            .map(memoryMb -> resultBuilder.targetServiceMemoryUsedMB(memoryMb).build())
                                            .defaultIfEmpty(resultBuilder.build());
                                } else {
                                    return Mono.just(resultBuilder.build());
                                }
                            }));
                })
                .onErrorResume(ex -> { // Erros de comunicação, não erros HTTP esperados
                    long durationNanos = System.nanoTime() - startTime;
                    logger.error("Erro de comunicação ao executar teste {}: {}", config.getTestName(), ex.getMessage());
                    boolean success = config.getExpectedHtppStatus() == 0; // Considera sucesso se um erro de rede era esperado (raro)

                    Timer specificTestTimer = meterRegistry.timer(config.getTestName() + ".duration", "success", "false"); // Geralmente falha
                    specificTestTimer.record(durationNanos, TimeUnit.NANOSECONDS);

                    return Mono.just(TestResult.builder()
                            .testName(config.getTestName())
                            .targetEndpoint(config.getEndpoint())
                            .success(success)
                            .durationMillis(TimeUnit.NANOSECONDS.toMillis(durationNanos))
                            .httpStatus(0) // Erro de comunicação
                            .build());
                })
                .doFinally(signalType -> {
                    sample.stop(meterRegistry.timer(config.getTestName() + ".overall.duration"));
                    logger.info("Teste {} (cenário: {}) concluído com sinal: {}", config.getTestName(), config.getScenarioType(), signalType);
                });

    }

    // Funções auxiliares para gerar dados (precisam ser implementadas)
    private Object generateRequestBody(TestCaseConfigDTO config) {
        // Lógica para usar Faker e templates para criar o corpo da requisição
        // Ex: se config.scenarioType == INVALID_INPUT_BAD_REQUEST, gerar um email inválido
        if (config.getRequestBodyTemplate() == null) return null;

        Map<String, Object> body = new HashMap<>(config.getRequestBodyTemplate());
        if (ScenarioType.INVALID_INPUT_BAD_REQUEST.equals(config.getScenarioType())) {
            if (body.containsKey("email")) { // Exemplo
                body.put("email", faker.lorem().word() + "@invalid"); // Força um e-mail inválido
            }
            if (body.containsKey("age")) {
                body.put("age", -5); // Idade inválida
            }
        } else if (ScenarioType.HAPPY_PATH.equals(config.getScenarioType())) {
            if (body.containsKey("email")) {
                body.put("email", faker.internet().emailAddress());
            }
            if (body.containsKey("age")) {
                body.put("age", faker.number().numberBetween(18,99));
            }
        }
        // ... outras lógicas para preencher placeholders com Faker ...
        return body;
    }

    private Map<String, String> generateQueryParams(TestCaseConfigDTO config) {
        // Lógica similar para query params
        if (config.getQueryParamsTemplate() == null) return Collections.emptyMap();
        // ... lógica de preenchimento com Faker baseada no cenário ...
        return new HashMap<>(config.getQueryParamsTemplate());
    }

}