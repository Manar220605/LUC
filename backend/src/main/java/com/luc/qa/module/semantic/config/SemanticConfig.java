package com.luc.qa.module.semantic.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmbedderProperties.class)
public class SemanticConfig {
}
