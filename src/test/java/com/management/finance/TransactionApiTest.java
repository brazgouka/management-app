package com.management.finance;

import com.management.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class TransactionApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void createsTransactionAndNormalizesCategory() throws Exception {
        create("Salary", "3000.00", "INCOME", "  Salary ", "2026-03-01")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/transactions/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.category").value("salary"))
                .andExpect(jsonPath("$.amount").value(3000.00))
                .andExpect(jsonPath("$.occurredOn").value("2026-03-01"));
    }

    @Test
    void rejectsInvalidTransactionWithFieldErrors() throws Exception {
        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content("""
                        {"description": "", "amount": -5, "type": "INCOME", "category": "x", "occurredOn": "2026-03-01"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void filtersByTypeAndDateRange() throws Exception {
        create("Salary", "3000.00", "INCOME", "salary", "2026-03-01");
        create("Rent", "1200.00", "EXPENSE", "housing", "2026-03-02");
        create("Old rent", "1100.00", "EXPENSE", "housing", "2026-01-02");

        mvc.perform(get("/api/transactions").param("type", "EXPENSE").param("from", "2026-03-01").param("to", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].description").value("Rent"));
    }

    @Test
    void summarizesPeriodWithBalanceAndSavingsRate() throws Exception {
        create("Salary", "4000.00", "INCOME", "salary", "2026-03-01");
        create("Rent", "1500.00", "EXPENSE", "housing", "2026-03-02");
        create("Groceries", "100.50", "EXPENSE", "food", "2026-03-05");
        create("Groceries", "99.50", "EXPENSE", "food", "2026-03-15");
        create("Outside the period", "999.00", "EXPENSE", "food", "2026-04-01");

        mvc.perform(get("/api/transactions/summary").param("from", "2026-03-01").param("to", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(4000.00))
                .andExpect(jsonPath("$.totalExpenses").value(1700.00))
                .andExpect(jsonPath("$.balance").value(2300.00))
                .andExpect(jsonPath("$.savingsRate").value(57.5))
                .andExpect(jsonPath("$.categories", hasSize(3)))
                .andExpect(jsonPath("$.categories[?(@.category == 'food')].count").value(2));
    }

    @Test
    void rejectsInvertedDateRange() throws Exception {
        mvc.perform(get("/api/transactions/summary").param("from", "2026-03-31").param("to", "2026-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("'from' must not be after 'to'"));
    }

    @Test
    void updatesAndDeletesTransaction() throws Exception {
        String location = create("Coffee", "3.50", "EXPENSE", "food", "2026-03-01")
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON).content("""
                        {"description": "Coffee beans", "amount": 14.90, "type": "EXPENSE", "category": "food", "occurredOn": "2026-03-01"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Coffee beans"))
                .andExpect(jsonPath("$.amount").value(14.90));

        mvc.perform(delete(location)).andExpect(status().isNoContent());
        mvc.perform(get(location))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void rejectsSortingByUnknownProperty() throws Exception {
        mvc.perform(get("/api/transactions").param("sort", "password"))
                .andExpect(status().isBadRequest());
    }

    private ResultActions create(String description, String amount, String type, String category, String date) throws Exception {
        return mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content("""
                {"description": "%s", "amount": %s, "type": "%s", "category": "%s", "occurredOn": "%s"}
                """.formatted(description, amount, type, category, date)));
    }
}
