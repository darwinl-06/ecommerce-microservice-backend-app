package com.selimhorri.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.selimhorri.app.config.featureToggle.FeatureToggleConfig;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.service.CartFeatureService;

@SpringBootTest
class CartFeatureToggleTest {

    @Autowired
    private CartFeatureService cartFeatureService;

    @SpyBean
    private FeatureToggleConfig featureToggleConfig;

    @Test
    void whenBulkOperationsEnabled_thenProcessSuccessfully() {
        // Arrange
        CartDto cart = new CartDto();
        when(featureToggleConfig.isEnableBulkOperations()).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> cartFeatureService.processBulkCartOperations(cart));
    }

    @Test
    void whenBulkOperationsDisabled_thenThrowException() {
        // Arrange
        CartDto cart = new CartDto();
        when(featureToggleConfig.isEnableBulkOperations()).thenReturn(false);

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, 
            () -> cartFeatureService.processBulkCartOperations(cart));
    }

    @Test
    void whenPriceValidationEnabled_thenValidationOccurs() {
        // Arrange
        CartDto cart = new CartDto();
        when(featureToggleConfig.isEnablePriceValidation()).thenReturn(true);

        // Act
        cartFeatureService.validateCartPrices(cart);

        // Assert
        verify(featureToggleConfig, times(1)).isEnablePriceValidation();
    }

    @Test
    void whenPriceValidationDisabled_thenSkipValidation() {
        // Arrange
        CartDto cart = new CartDto();
        when(featureToggleConfig.isEnablePriceValidation()).thenReturn(false);

        // Act
        cartFeatureService.validateCartPrices(cart);

        // Assert
        verify(featureToggleConfig, times(1)).isEnablePriceValidation();
        // No validation should occur when feature is disabled
    }
}
