package br.com.lottus.auxina.service.engine; // Exemplo de novo pacote

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
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TestExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(TestExecutionService.class);
    private static final Pattern FAKER_PLACEHOLDER_PATTERN = Pattern.compile("^Faker::(\\w+)\\.(\\w+)(?:\\(([^)]*)\\))?$");

    private final WebClient libraryServiceClient;
    private final Faker faker;
    private final MeterRegistry meterRegistry;

    public TestExecutionService(WebClient libraryServiceClient, Faker faker, MeterRegistry meterRegistry) {
        this.libraryServiceClient = libraryServiceClient;
        this.faker = faker;
        this.meterRegistry = meterRegistry;
    }

    public Mono<TestResult> executeTest(TestCaseConfigDTO config) { // Nomeado de forma mais genérica
        long startTime = System.nanoTime();
        Timer.Sample sample = Timer.start(meterRegistry);

        Object requestBody = generateRequestBody(config);
        Map<String, String> queryParams = generateQueryParams(config);

        WebClient.RequestBodySpec requestBodySpec;
        WebClient.RequestHeadersSpec<?> requestHeadersSpec;

        String httpMethodUpper = config.getHttpMethod().toUpperCase();

        switch (httpMethodUpper) {
            case "POST":
                requestBodySpec = libraryServiceClient.post().uri(config.getEndpoint());
                requestHeadersSpec = (requestBody != null) ? requestBodySpec.bodyValue(requestBody) : requestBodySpec;
                break;
            case "PUT":
                requestBodySpec = libraryServiceClient.put().uri(config.getEndpoint());
                requestHeadersSpec = (requestBody != null) ? requestBodySpec.bodyValue(requestBody) : requestBodySpec;
                break;
            case "DELETE":
                requestHeadersSpec = libraryServiceClient.delete().uri(config.getEndpoint());
                break;
            case "GET":
            default:
                requestHeadersSpec = libraryServiceClient.get()
                        .uri(uriBuilder -> {
                            uriBuilder.path(config.getEndpoint());
                            if (queryParams != null && !queryParams.isEmpty()) {
                                queryParams.forEach(uriBuilder::queryParam);
                            }
                            return uriBuilder.build();
                        });
                break;
        }

        return requestHeadersSpec
                .exchangeToMono(clientResponse -> {
                    long durationNanos = System.nanoTime() - startTime;
                    boolean success = clientResponse.statusCode().value() == config.getExpectedHtppStatus();

                    Timer specificTestTimer = meterRegistry.timer(config.getTestName() + ".duration", "success", String.valueOf(success));
                    specificTestTimer.record(durationNanos, TimeUnit.NANOSECONDS);

                    String endpointDetails = config.getEndpoint();
                    if (queryParams != null && !queryParams.isEmpty()){
                        endpointDetails += "?" + queryParams.entrySet().stream()
                                .map(e -> e.getKey() + "=" + e.getValue())
                                .collect(Collectors.joining("&"));
                    }

                    TestResult.TestResultBuilder resultBuilder = TestResult.builder()
                            .testName(config.getTestName())
                            .targetEndpoint(endpointDetails)
                            .methodGroupKey(config.getMethodGroupKey())
                            .success(success)
                            .durationMillis(TimeUnit.NANOSECONDS.toMillis(durationNanos))
                            .httpStatus(clientResponse.statusCode().value());

                    return clientResponse.releaseBody()
                            .then(Mono.defer(() -> {
                                    return getTargetServiceMemoryUsage() // Renomeado para ser mais genérico
                                            .map(memoryMb -> resultBuilder.targetServiceMemoryUsedMB(memoryMb).build())
                                            .defaultIfEmpty(resultBuilder.build());
                            }));
                })
                .onErrorResume(ex -> {
                    long durationNanos = System.nanoTime() - startTime;
                    logger.error("Erro de comunicação ao executar teste configurável {}: {}", config.getTestName(), ex.getMessage());
                    boolean isExpectedNetworkError = config.getExpectedHtppStatus() == 0;

                    Timer specificTestTimer = meterRegistry.timer(config.getTestName() + ".duration", "success", String.valueOf(isExpectedNetworkError));
                    specificTestTimer.record(durationNanos, TimeUnit.NANOSECONDS);

                    return Mono.just(TestResult.builder()
                            .testName(config.getTestName())
                            .methodGroupKey(config.getMethodGroupKey())
                            .targetEndpoint(config.getEndpoint())
                            .success(isExpectedNetworkError)
                            .durationMillis(TimeUnit.NANOSECONDS.toMillis(durationNanos))
                            .httpStatus(0)
                            .build());
                })
                .doFinally(signalType -> {
                    sample.stop(meterRegistry.timer(config.getTestName() + ".overall.duration"));
                    logger.info("Teste configurável {} (cenário: {}) concluído com sinal: {}", config.getTestName(), config.getScenarioType(), signalType);
                });
    }

    // Métodos generateRequestBody, generateQueryParams, processTemplate, invokeFakerMethod,
    // applyScenarioSpecificModifications permanecem os mesmos aqui, mas são privados.

    private Object generateRequestBody(TestCaseConfigDTO config) {
        if (config.getRequestBodyTemplate() == null) {
            return null;
        }
        Map<String, Object> generatedBody = processTemplate(config.getRequestBodyTemplate());
        applyScenarioSpecificModifications(generatedBody, config.getScenarioType(), config.getRequestBodyTemplate());
        return generatedBody;
    }

    private Map<String, String> generateQueryParams(TestCaseConfigDTO config) {
        if (config.getQueryParamsTemplate() == null || config.getQueryParamsTemplate().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> processedParamsAsObjects = processTemplate(config.getQueryParamsTemplate());
        Map<String, String> generatedParams = processedParamsAsObjects.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));

        Map<String, Object> tempModifiableMap = new HashMap<>(generatedParams);
        applyScenarioSpecificModifications(tempModifiableMap, config.getScenarioType(), config.getQueryParamsTemplate());

        return tempModifiableMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
    }

    private Map<String, Object> processTemplate(Map<String, ?> template) {
        Map<String, Object> result = new HashMap<>();
        if (template == null) return result;

        for (Map.Entry<String, ?> entry : template.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                Matcher matcher = FAKER_PLACEHOLDER_PATTERN.matcher((String) value);
                if (matcher.matches()) {
                    try {
                        Object fakerValue = invokeFakerMethod(matcher.group(1), matcher.group(2), matcher.group(3));
                        result.put(key, fakerValue);
                    } catch (Exception e) {
                        logger.warn("Falha ao invocar método Faker para placeholder '{}': {}. Usando placeholder como valor literal.", value, e.getMessage());
                        result.put(key, value);
                    }
                } else {
                    result.put(key, value);
                }
            } else if (value instanceof Map) {
                result.put(key, processTemplate((Map<String, ?>) value));
            } else if (value instanceof java.util.List) {
                result.put(key, ((java.util.List<?>) value).stream()
                        .map(item -> {
                            if (item instanceof String) {
                                Matcher matcher = FAKER_PLACEHOLDER_PATTERN.matcher((String) item);
                                if (matcher.matches()) {
                                    try {
                                        return invokeFakerMethod(matcher.group(1), matcher.group(2), matcher.group(3));
                                    } catch (Exception e) {
                                        logger.warn("Falha ao invocar método Faker para placeholder em lista '{}': {}. Usando placeholder.", item, e.getMessage());
                                        return item;
                                    }
                                }
                            } else if (item instanceof Map) {
                                return processTemplate((Map<String, ?>) item);
                            }
                            return item;
                        })
                        .collect(Collectors.toList()));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    private Object invokeFakerMethod(String categoryName, String methodName, String argsString) throws Exception {
        Method categoryMethod = Faker.class.getMethod(categoryName.toLowerCase());
        Object categoryInstance = categoryMethod.invoke(faker);

        if (!StringUtils.hasText(argsString)) {
            Method targetMethod = categoryInstance.getClass().getMethod(methodName);
            return targetMethod.invoke(categoryInstance);
        } else {
            String[] argValuesStr = Arrays.stream(argsString.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);

            Method targetMethod = null;
            for (Method m : categoryInstance.getClass().getMethods()) {
                if (m.getName().equals(methodName) && m.getParameterCount() == argValuesStr.length) {
                    targetMethod = m;
                    break;
                }
            }

            if (targetMethod == null) {
                throw new NoSuchMethodException("Método " + methodName + " com " + argValuesStr.length + " argumentos não encontrado em " + categoryInstance.getClass().getName());
            }

            Object[] MappedArgumentValues = new Object[argValuesStr.length];
            Class<?>[] paramTypes = targetMethod.getParameterTypes();
            for (int i = 0; i < argValuesStr.length; i++) {
                if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) {
                    MappedArgumentValues[i] = Integer.parseInt(argValuesStr[i]);
                } else if (paramTypes[i] == long.class || paramTypes[i] == Long.class) {
                    MappedArgumentValues[i] = Long.parseLong(argValuesStr[i]);
                } else if (paramTypes[i] == double.class || paramTypes[i] == Double.class) {
                    MappedArgumentValues[i] = Double.parseDouble(argValuesStr[i]);
                } else if (paramTypes[i] == boolean.class || paramTypes[i] == Boolean.class) {
                    MappedArgumentValues[i] = Boolean.parseBoolean(argValuesStr[i]);
                } else {
                    MappedArgumentValues[i] = argValuesStr[i];
                }
            }
            return targetMethod.invoke(categoryInstance, MappedArgumentValues);
        }
    }

    private void applyScenarioSpecificModifications(Map<String, Object> data,
                                                    ScenarioType scenarioType,
                                                    Map<String, ?> originalTemplate) {
        if (scenarioType == null || originalTemplate == null || data == null) return;

        switch (scenarioType) {
            case INVALID_INPUT_BAD_REQUEST:
            case INVALID_INPUT_UNPROCESSABLE_ENTITY:
                if (originalTemplate.containsKey("email") && data.containsKey("email")) {
                    data.put("email", faker.lorem().word() + "@invalid");
                }
                if (originalTemplate.containsKey("age") && data.containsKey("age")) {
                    data.put("age", -5);
                }
                if (originalTemplate.containsKey("nome") && data.containsKey("nome")) { // Assumindo que 'nome' é um campo comum
                    data.put("nome", "");
                }
                if (originalTemplate.containsKey("quantidade") && data.containsKey("quantidade")) { // Exemplo para livro
                    data.put("quantidade", -1);
                }
                if (originalTemplate.containsKey("requiredField") && data.containsKey("requiredField")) {
                    data.put("requiredField", null);
                }
                break;
            case HAPPY_PATH:
                break;
            case RESOURCE_NOT_FOUND:
                if (data.containsKey("id") && originalTemplate.containsKey("id")) {
                    data.put("id", faker.number().randomNumber(10, true) + 999900000L);
                }
                break;
            default:
                break;
        }
    }

    private Mono<Double> getTargetServiceMemoryUsage() {
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
}