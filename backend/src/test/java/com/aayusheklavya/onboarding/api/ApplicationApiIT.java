package com.aayusheklavya.onboarding.api;

import com.aayusheklavya.onboarding.domain.BusinessApplicationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationApiIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired BusinessApplicationRepository repo;

    @BeforeEach
    void clean() { repo.deleteAll(); }

    @Test
    void createAddRepresentativeSubmitAndApprove() throws Exception {
        String body = mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName":"Acme Ltd","legalStructure":"LLC","naicsCode":"541511","annualRevenue":250000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/applications/")))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.allowedTransitions", contains("SUBMITTED")))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(body).get("id").asText();

        mvc.perform(post("/api/applications/" + id + "/representatives").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"jane@acme.com","role":"OWNER","ownershipPercent":100,"authorisedSigner":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jane@acme.com"));

        mvc.perform(post("/api/applications/" + id + "/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
        mvc.perform(post("/api/applications/" + id + "/review")).andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
        mvc.perform(post("/api/applications/" + id + "/approve").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"verified\"}"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewNote").value("verified"));

        mvc.perform(get("/api/applications/summary"))
                .andExpect(jsonPath("$.APPROVED").value(1))
                .andExpect(jsonPath("$.DRAFT").value(0));
        mvc.perform(get("/api/applications").param("status", "APPROVED"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].representativeCount").value(1));
    }

    @Test
    void validationErrorsComeBackAsProblemDetailsWithFieldMap() throws Exception {
        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessName\":\"\",\"legalStructure\":\"GUILD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.businessName").exists())
                .andExpect(jsonPath("$.errors.legalStructure").exists());
    }

    @Test
    void businessRuleViolationsUseConflictAndUnprocessable() throws Exception {
        String body = mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessName\":\"Acme\",\"legalStructure\":\"LLC\"}"))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(body);
        String id = node.get("id").asText();

        mvc.perform(post("/api/applications/" + id + "/submit"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("representative")));
        mvc.perform(post("/api/applications/" + id + "/approve"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/applications/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void naicsSearchEndpoint() throws Exception {
        mvc.perform(get("/api/naics").param("q", "bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", hasItems("522110", "523150")));
    }
}
