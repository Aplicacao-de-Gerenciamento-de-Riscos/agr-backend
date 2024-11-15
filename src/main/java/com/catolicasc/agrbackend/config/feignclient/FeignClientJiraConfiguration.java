package com.catolicasc.agrbackend.config.feignclient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignClientJiraConfiguration {

    @Value("${JIRA_API_USER}")
    private String jiraUser;

    @Value("${JIRA_API_SECRET}")
    private String jiraSecret;

    /**
     * Configuração de interceptador para adicionar o header de autenticação da API do Jira
     * @return
     */
    @Bean
    public RequestInterceptor basicAuthRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String auth = jiraUser + ":" + jiraSecret;
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
                String authHeader = "Basic " + new String(encodedAuth);
                template.header("Authorization", authHeader);
            }
        };
    }

    /**
     * Configura o ObjectMapper para deserializar JSON em objetos Java, com suporte a datas do Java 8+ e tolerância a propriedades desconhecidas.
     * Define um decodificador Feign que usa o ObjectMapper configurado para converter as respostas das APIs em objetos Java.
     * Isso é útil para garantir que o cliente Feign interprete corretamente os dados recebidos, mesmo em cenários complexos, como respostas que contêm campos inesperados ou formatos de data específicos.
     */

    /**
     * Configuração de deserialização de objetos
     * @return
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    /**
     * Configuração de decodificação de objetos
     * @param objectMapper
     * @return
     */
    @Bean
    public Decoder feignDecoder(ObjectMapper objectMapper) {
        HttpMessageConverter<?> jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }
}
