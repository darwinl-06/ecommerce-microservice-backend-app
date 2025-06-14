package com.selimhorri.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "features.cart")
public class FeatureToggleConfig {
    private boolean enableBulkOperations;
    private boolean enablePriceValidation;
}
