package com.selimhorri.app.unit.service;

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;
import com.selimhorri.app.unit.util.ProductUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;

@SpringJUnitConfig
@EnableRetry
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDto product;

    @BeforeEach
    void setUp() {
        product = ProductUtil.getSampleProductDto();
    }

    @Test
    void testFindById_ShouldReturnProductDto() {
        when(productRepository.findById(1)).thenReturn(Optional.of(ProductUtil.getSampleProduct()));

        ProductDto result = productService.findById(product.getProductId());

        assertNotNull(result);
        assertEquals(product.getProductId(), result.getProductId());
        assertEquals(product.getProductTitle(), result.getProductTitle());
        assertEquals(product.getImageUrl(), result.getImageUrl());
    }


}

