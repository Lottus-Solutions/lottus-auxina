package br.com.lottus.auxina.config;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Value("${library.service.base-url}")
    private String libraryBaseUrl;

    @Value("${library.service.auth.token}")
    private String libraryServiceAuthToken;

    @Bean
    public WebClient libraryServiceClient(WebClient.Builder builder){
        WebClient.Builder clientBuilder = builder
                .baseUrl(libraryBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);


        if(StringUtils.hasText(libraryServiceAuthToken)) {
            clientBuilder.defaultHeaders(headers -> headers.setBearerAuth(libraryServiceAuthToken));
        }



        return clientBuilder.build();
    }

    @Bean
    public Faker faker(){
        return new Faker();
    }
}
