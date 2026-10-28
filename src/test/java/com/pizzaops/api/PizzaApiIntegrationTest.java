package com.pizzaops.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PizzaApiIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void menuAndHealthAreAccessible() throws Exception {
        mvc.perform(get("/api/v1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sizes[0].code").value("SMALL"))
                .andExpect(jsonPath("$.toppings[0].code").value("PEPPERONI"));
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void createOrderThenReadOrderAndStatistics() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"size":"SMALL","base":"ORIGINAL","sauce":"TOMATO",
                         "cheese":"MOZZARELLA","toppings":["HAM","OLIVES","PEPPER","CHICKEN","BACON"]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/orders/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.extraToppingChargeGbp").value(1));
        mvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toppings.length()").value(5));
        mvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
        mvc.perform(get("/api/v1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCount").value(1))
                .andExpect(jsonPath("$.averageToppings").value(5.0))
                .andExpect(jsonPath("$.totalExtraChargesGbp").value(1));
    }

    @Test
    void rejectsDuplicateToppingsWithoutSaving() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"size":"SMALL","base":"ORIGINAL","sauce":"TOMATO",
                         "cheese":"MOZZARELLA","toppings":["HAM","HAM"]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ORDER"));
        mvc.perform(get("/api/v1/statistics"))
                .andExpect(jsonPath("$.orderCount").value(0));
    }

    @Test
    void rejectsMissingFieldsAndUnknownEnums() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"size":"SMALL"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"size":"NOT_A_SIZE","base":"ORIGINAL","sauce":"TOMATO",
                         "cheese":"MOZZARELLA","toppings":[]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_CATALOG_VALUE"))
                .andExpect(jsonPath("$.field").value("size"))
                .andExpect(jsonPath("$.allowedValues[0]").value("SMALL"))
                .andExpect(jsonPath("$.allowedValues[1]").value("MEDIUM"))
                .andExpect(jsonPath("$.allowedValues[2]").value("LARGE"));
    }

    @Test
    void invalidSizeIsDescriptiveAndDoesNotSaveOrConsumeId() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"size":"EXTRA_GIGANTE","base":"THIN","sauce":"TOMATO",
                         "cheese":"MOZZARELLA","toppings":["HAM"]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_CATALOG_VALUE"))
                .andExpect(jsonPath("$.field").value("size"))
                .andExpect(jsonPath("$.allowedValues.length()").value(3));
        mvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"size":"LARGE","base":"THIN","sauce":"TOMATO",
                         "cheese":"MOZZARELLA","toppings":["HAM"]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void invalidToppingReportsItsPosition() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"size":"LARGE","base":"THIN","sauce":"TOMATO",
                         "cheese":"MOZZARELLA","toppings":["HAM","NOT_A_TOPPING"]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_CATALOG_VALUE"))
                .andExpect(jsonPath("$.field").value("toppings[1]"))
                .andExpect(jsonPath("$.allowedValues[0]").value("PEPPERONI"));
    }

    @Test
    void malformedJsonDoesNotSave() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                .content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_JSON"));
        mvc.perform(get("/api/v1/statistics"))
                .andExpect(jsonPath("$.orderCount").value(0));
    }

    @Test
    void missingOrderReturns404() throws Exception {
        mvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
    }
}
