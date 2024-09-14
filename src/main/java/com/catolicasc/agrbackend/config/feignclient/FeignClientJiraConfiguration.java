package com.catolicasc.agrbackend.config.feignclient;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientJiraConfiguration {

    @Value("${JIRA_API_USER}")
    private String jiraUser;

    @Value("${JIRA_API_SECRET}")
    private String jiraSecret;

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
}
