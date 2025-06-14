package com.selimhorri.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.service.CartService;

@SpringBootTest
class CartServiceCircuitBreakerTest {

    @Autowired
    private CartService cartService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void whenUserServiceFails_thenCircuitBreakerReturns() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(UserDto.class)))
                .thenThrow(new RuntimeException("User Service Down"));

        // Act
        CartDto result = cartService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Unavailable", result.getUserDto().getFirstName());
        assertEquals("unavailable@temp.com", result.getUserDto().getEmail());
    }

    @Test
    void whenUserServiceWorks_thenReturnsNormalResponse() {
        // Arrange
        UserDto mockUser = new UserDto();
        mockUser.setFirstName("John");
        mockUser.setEmail("john@example.com");

        when(restTemplate.getForObject(anyString(), eq(UserDto.class)))
                .thenReturn(mockUser);

        // Act
        CartDto result = cartService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getUserDto().getFirstName());
        assertEquals("john@example.com", result.getUserDto().getEmail());
    }

    @Test
    void whenMultipleFailures_thenCircuitBreakerOpens() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(UserDto.class)))
                .thenThrow(new RuntimeException("User Service Down"));

        // Act & Assert
        // Call multiple times to trigger circuit breaker
        for (int i = 0; i < 5; i++) {
            CartDto result = cartService.findById(1);
            assertNotNull(result);
            assertEquals("Unavailable", result.getUserDto().getFirstName());
        }
    }
}