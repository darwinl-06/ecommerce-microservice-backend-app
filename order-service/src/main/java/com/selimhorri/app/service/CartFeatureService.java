package com.selimhorri.app.service;

import org.springframework.stereotype.Service;

import com.selimhorri.app.config.featureToggle.FeatureToggleConfig;
import com.selimhorri.app.dto.CartDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartFeatureService {
    private final FeatureToggleConfig featureToggleConfig;
    
    public void processBulkCartOperations(CartDto cart) {
        if (!featureToggleConfig.isEnableBulkOperations()) {
            throw new UnsupportedOperationException("Bulk operations are not enabled");
        }
        // Process bulk operations...
    }
    
    public void validateCartPrices(CartDto cart) {
        if (!featureToggleConfig.isEnablePriceValidation()) {
            return; // Skip validation if feature is disabled
        }
        // Validate prices...
    }
}
