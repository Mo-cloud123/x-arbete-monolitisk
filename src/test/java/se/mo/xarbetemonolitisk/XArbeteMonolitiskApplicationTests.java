package se.mo.xarbetemonolitisk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class XArbeteMonolitiskApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("monolith_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void orderFlowShouldCreateAndFetchOrder() throws Exception {
        String createdUserJson = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice",
                                  "email": "alice@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = objectMapper.readTree(createdUserJson).get("id").asLong();

        String createdProductJson = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Laptop",
                                  "price": 1000.00,
                                  "stock": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long productId = objectMapper.readTree(createdProductJson).get("id").asLong();

        String createdOrderJson = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "items": [
                                    {
                                      "productId": %d,
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """.formatted(userId, productId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdOrder = objectMapper.readTree(createdOrderJson);
        long orderId = createdOrder.get("id").asLong();
        assertThat(createdOrder.get("totalPrice").decimalValue()).isEqualByComparingTo("2000.00");

        String fetchedOrderJson = mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode fetchedOrder = objectMapper.readTree(fetchedOrderJson);
        assertThat(fetchedOrder.get("id").asLong()).isEqualTo(orderId);
        assertThat(fetchedOrder.get("userId").asLong()).isEqualTo(userId);
        assertThat(fetchedOrder.get("items").size()).isEqualTo(1);

        String ordersByUserJson = mockMvc.perform(get("/orders/user/{userId}", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode ordersArray = objectMapper.readTree(ordersByUserJson);
        assertThat(ordersArray.isArray()).isTrue();
        assertThat(ordersArray).hasSize(1);

        String productsJson = mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode products = objectMapper.readTree(productsJson);
        assertThat(products.get(0).get("stock").asInt()).isEqualTo(3);
    }
}
