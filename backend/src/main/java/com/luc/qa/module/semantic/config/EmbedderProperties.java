package com.luc.qa.module.semantic.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "luc.embedder")
public class EmbedderProperties {

    private String url = "http://localhost:8088";
    private int connectTimeoutMs = 2000;
    private int readTimeoutMs = 15000;
}
