package com.gestionmicroempresas.backend.security;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.testcontainers.containers.PostgreSQLContainer;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TenantIsolationIntegrationTests {

    private static final String DATABASE_NAME =
            "gestion_microempresas_test";


    private static final String APPLICATION_DATABASE_USER =
            "gestion_app_test";


    private static final String APPLICATION_DATABASE_PASSWORD =
            "gestion_test_password";


    private static final String JWT_TEST_SECRET =
            "integration-test-jwt-secret-must-have-at-least-32-bytes-2026";


    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            DATABASE_NAME
                    )
                    .withUsername(
                            "postgres"
                    )
                    .withPassword(
                            "postgres"
                    )
                    .withInitScript(
                            "test-db-init.sql"
                    );


    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {

        // =================================================
        // DATABASE
        // =================================================

        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );


        registry.add(
                "spring.datasource.username",
                () ->
                        APPLICATION_DATABASE_USER
        );


        registry.add(
                "spring.datasource.password",
                () ->
                        APPLICATION_DATABASE_PASSWORD
        );


        registry.add(
                "spring.datasource.driver-class-name",
                () ->
                        "org.postgresql.Driver"
        );


        // =================================================
        // FLYWAY
        // =================================================

        registry.add(
                "spring.flyway.user",
                () ->
                        APPLICATION_DATABASE_USER
        );


        registry.add(
                "spring.flyway.password",
                () ->
                        APPLICATION_DATABASE_PASSWORD
        );


        registry.add(
                "spring.flyway.enabled",
                () ->
                        "true"
        );


        // =================================================
        // SECURITY
        // =================================================

        registry.add(
                "app.jwt.secret",
                () ->
                        JWT_TEST_SECRET
        );


        registry.add(
                "app.jwt.issuer",
                () ->
                        "gestion-microempresas-test"
        );


        registry.add(
                "app.auth.cookie-name",
                () ->
                        "access_token"
        );


        registry.add(
                "app.auth.cookie-secure",
                () ->
                        "false"
        );


        // =================================================
        // BOOTSTRAP
        // =================================================

        registry.add(
                "app.bootstrap-admin.enabled",
                () ->
                        "false"
        );


        registry.add(
                "app.bootstrap-admin.name",
                () ->
                        "Test Administrator"
        );


        registry.add(
                "app.bootstrap-admin.email",
                () ->
                        ""
        );


        registry.add(
                "app.bootstrap-admin.password",
                () ->
                        ""
        );


        // =================================================
        // RATE LIMIT
        // =================================================
        //
        // High values because rate limiting is not the
        // concern of this isolation suite.
        // =================================================

        registry.add(
                "app.security.rate-limit.login.ip.max-attempts",
                () ->
                        "1000"
        );


        registry.add(
                "app.security.rate-limit.login.identity.max-attempts",
                () ->
                        "1000"
        );


        registry.add(
                "app.security.rate-limit.login.window-minutes",
                () ->
                        "15"
        );


        registry.add(
                "app.security.rate-limit.registration.ip.max-attempts",
                () ->
                        "1000"
        );


        registry.add(
                "app.security.rate-limit.registration.window-minutes",
                () ->
                        "60"
        );


        // =================================================
        // STORAGE
        // =================================================

        registry.add(
                "app.storage.base-directory",
                () ->
                        "target/test-storage"
        );


        // =================================================
        // URLS
        // =================================================

        registry.add(
                "app.frontend-url",
                () ->
                        "http://localhost:3000"
        );


        registry.add(
                "app.backend-url",
                () ->
                        "http://localhost:8080"
        );
    }


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Autowired
    private PlatformTransactionManager transactionManager;


    // =====================================================
    // CSRF
    // =====================================================

    @Test
    void registrationWithoutCsrfMustBeRejected()
            throws Exception {

        String suffix =
                uniqueSuffix();


        Map<String, Object> request =
                Map.of(
                        "empresa",
                        "CSRF Test "
                                +
                                suffix,

                        "nombre",
                        "Administrator",

                        "email",
                        "csrf."
                                +
                                suffix
                                +
                                "@example.test",

                        "password",
                        "Password123!",

                        "confirmarPassword",
                        "Password123!"
                );


        mockMvc.perform(

                        post(
                                "/api/auth/registro"
                        )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        json(
                                                request
                                        )
                                )
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    // =====================================================
    // DATABASE ROLE
    // =====================================================

    @Test
    void applicationDatabaseRoleMustNotBypassRls() {

        String currentUser =
                jdbcTemplate.queryForObject(

                        """
                        SELECT current_user
                        """,

                        String.class
                );


        assertEquals(
                APPLICATION_DATABASE_USER,
                currentUser
        );


        Boolean superuser =
                jdbcTemplate.queryForObject(

                        """
                        SELECT rolsuper
                        FROM pg_roles
                        WHERE rolname = current_user
                        """,

                        Boolean.class
                );


        Boolean bypassRls =
                jdbcTemplate.queryForObject(

                        """
                        SELECT rolbypassrls
                        FROM pg_roles
                        WHERE rolname = current_user
                        """,

                        Boolean.class
                );


        assertEquals(
                Boolean.FALSE,
                superuser,
                "The application database role must not be a PostgreSQL superuser."
        );


        assertEquals(
                Boolean.FALSE,
                bypassRls,
                "The application database role must not have BYPASSRLS."
        );
    }


    // =====================================================
    // FAIL CLOSED
    // =====================================================

    @Test
    void rlsMustHideTenantRowsWhenNoTenantContextExists()
            throws Exception {

        TenantSession tenant =
                registerTenant(
                        "No Context"
                );


        createClient(
                tenant,
                "Hidden Client",
                "RLS010101AA1"
        );


        TransactionTemplate transactionTemplate =
                new TransactionTemplate(
                        transactionManager
                );


        Long visibleClients =
                transactionTemplate.execute(

                        status ->

                                jdbcTemplate.queryForObject(

                                        """
                                        SELECT COUNT(*)
                                        FROM clients
                                        """,

                                        Long.class
                                )
                );


        assertEquals(
                0L,
                visibleClients
        );
    }


    // =====================================================
    // TENANT A VS TENANT B
    // =====================================================

    @Test
    void tenantsMustNotSeeModifyOrDeleteEachOthersClients()
            throws Exception {

        TenantSession tenantA =
                registerTenant(
                        "Company A"
                );


        TenantSession tenantB =
                registerTenant(
                        "Company B"
                );


        /*
         * Intentionally use the same RFC.
         *
         * RFC uniqueness must exist inside each tenant,
         * not globally across the entire platform.
         */
        String sharedRfc =
                "ABC010101AA1";


        Long clientAId =
                createClient(
                        tenantA,
                        "Client A",
                        sharedRfc
                );


        Long clientBId =
                createClient(
                        tenantB,
                        "Client B",
                        sharedRfc
                );


        // =================================================
        // A ONLY SEES A
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/clientes"
                        )

                                .param(
                                        "size",
                                        "100"
                                )

                                .cookie(
                                        tenantA.authCookie()
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath(
                                "$.totalElements"
                        )
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.content[0].id"
                        )
                                .value(
                                        clientAId
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.content[0].razonSocial"
                        )
                                .value(
                                        "Client A"
                                )
                );


        // =================================================
        // B ONLY SEES B
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/clientes"
                        )

                                .param(
                                        "size",
                                        "100"
                                )

                                .cookie(
                                        tenantB.authCookie()
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath(
                                "$.totalElements"
                        )
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.content[0].id"
                        )
                                .value(
                                        clientBId
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.content[0].razonSocial"
                        )
                                .value(
                                        "Client B"
                                )
                );


        // =================================================
        // A TRIES TO MODIFY B
        // =================================================

        Map<String, Object> updateRequest =
                Map.of(
                        "razonSocial",
                        "Compromised Client",

                        "rfc",
                        sharedRfc,

                        "telefonos",
                        List.of(
                                "5555555555"
                        ),

                        "correos",
                        List.of(
                                "attacker@example.test"
                        )
                );


        mockMvc.perform(

                        put(
                                "/api/clientes/{id}",
                                clientBId
                        )

                                .with(
                                        csrf()
                                )

                                .cookie(
                                        tenantA.authCookie()
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        json(
                                                updateRequest
                                        )
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =================================================
        // A TRIES TO DELETE B
        // =================================================

        mockMvc.perform(

                        delete(
                                "/api/clientes/{id}",
                                clientBId
                        )

                                .with(
                                        csrf()
                                )

                                .cookie(
                                        tenantA.authCookie()
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =================================================
        // B DATA MUST STILL EXIST
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/clientes"
                        )

                                .param(
                                        "size",
                                        "100"
                                )

                                .cookie(
                                        tenantB.authCookie()
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath(
                                "$.totalElements"
                        )
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.content[0].id"
                        )
                                .value(
                                        clientBId
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.content[0].razonSocial"
                        )
                                .value(
                                        "Client B"
                                )
                );


        // =================================================
        // DATABASE RLS
        // =================================================

        assertEquals(
                1L,
                countClientsVisibleToTenant(
                        tenantA.tenantId()
                )
        );


        assertEquals(
                1L,
                countClientsVisibleToTenant(
                        tenantB.tenantId()
                )
        );
    }


    // =====================================================
    // REGISTER TENANT
    // =====================================================

    private TenantSession registerTenant(
            String companyPrefix
    )
            throws Exception {

        String suffix =
                uniqueSuffix();


        String email =
                "admin."
                        +
                        suffix
                        +
                        "@example.test";


        Map<String, Object> request =
                Map.of(
                        "empresa",
                        companyPrefix
                                +
                                " "
                                +
                                suffix,

                        "nombre",
                        "Administrator "
                                +
                                suffix,

                        "email",
                        email,

                        "password",
                        "Password123!",

                        "confirmarPassword",
                        "Password123!"
                );


        MvcResult result =
                mockMvc.perform(

                                post(
                                        "/api/auth/registro"
                                )

                                        .with(
                                                csrf()
                                        )

                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )

                                        .content(
                                                json(
                                                        request
                                                )
                                        )
                        )

                        .andExpect(
                                status().isCreated()
                        )

                        .andExpect(
                                jsonPath(
                                        "$.id"
                                )
                                        .isNumber()
                        )

                        .andExpect(
                                jsonPath(
                                        "$.empresa.id"
                                )
                                        .isNumber()
                        )

                        .andExpect(
                                jsonPath(
                                        "$.rol"
                                )
                                        .value(
                                                "admin"
                                        )
                        )

                        .andReturn();


        JsonNode response =
                objectMapper.readTree(

                        result
                                .getResponse()
                                .getContentAsString()
                );


        Long tenantId =
                response
                        .path(
                                "empresa"
                        )
                        .path(
                                "id"
                        )
                        .asLong();


        Cookie authCookie =
                extractAuthenticationCookie(
                        result
                );


        return new TenantSession(
                tenantId,
                authCookie,
                email
        );
    }


    // =====================================================
    // CREATE CLIENT
    // =====================================================

    private Long createClient(
            TenantSession tenant,
            String businessName,
            String rfc
    )
            throws Exception {

        String suffix =
                uniqueSuffix();


        Map<String, Object> request =
                Map.of(
                        "razonSocial",
                        businessName,

                        "rfc",
                        rfc,

                        "telefonos",
                        List.of(
                                "55"
                                        +
                                        suffix.substring(
                                                0,
                                                8
                                        )
                        ),

                        "correos",
                        List.of(
                                "client."
                                        +
                                        suffix
                                        +
                                        "@example.test"
                        )
                );


        MvcResult result =
                mockMvc.perform(

                                post(
                                        "/api/clientes"
                                )

                                        .with(
                                                csrf()
                                        )

                                        .cookie(
                                                tenant.authCookie()
                                        )

                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )

                                        .content(
                                                json(
                                                        request
                                                )
                                        )
                        )

                        .andExpect(
                                status().isCreated()
                        )

                        .andReturn();


        JsonNode response =
                objectMapper.readTree(

                        result
                                .getResponse()
                                .getContentAsString()
                );


        return response
                .path(
                        "id"
                )
                .asLong();
    }


    // =====================================================
    // RLS DATABASE QUERY
    // =====================================================

    private Long countClientsVisibleToTenant(
            Long tenantId
    ) {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(
                        transactionManager
                );


        return transactionTemplate.execute(

                status -> {

                    jdbcTemplate.queryForObject(

                            """
                            SELECT set_config(
                                'app.current_tenant',
                                ?,
                                TRUE
                            )
                            """,

                            String.class,

                            tenantId.toString()
                    );


                    return jdbcTemplate.queryForObject(

                            """
                            SELECT COUNT(*)
                            FROM clients
                            """,

                            Long.class
                    );
                }
        );
    }


    // =====================================================
    // COOKIE
    // =====================================================

    private Cookie extractAuthenticationCookie(
            MvcResult result
    ) {

        List<String> setCookieHeaders =
                result
                        .getResponse()
                        .getHeaders(
                                HttpHeaders.SET_COOKIE
                        );


        String authenticationHeader =
                setCookieHeaders
                        .stream()

                        .filter(
                                header ->
                                        header.startsWith(
                                                "access_token="
                                        )
                        )

                        .findFirst()

                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                "The registration response did not create the authentication cookie."
                                        )
                        );


        String cookiePair =
                authenticationHeader
                        .split(
                                ";",
                                2
                        )[0];


        int separatorIndex =
                cookiePair.indexOf(
                        '='
                );


        String value =
                cookiePair.substring(
                        separatorIndex
                                +
                                1
                );


        Cookie cookie =
                new Cookie(
                        "access_token",
                        value
                );


        cookie.setPath(
                "/"
        );


        return cookie;
    }


    // =====================================================
    // JSON
    // =====================================================

    private String json(
            Object value
    )
            throws Exception {

        return objectMapper
                .writeValueAsString(
                        value
                );
    }


    // =====================================================
    // UNIQUE DATA
    // =====================================================

    private String uniqueSuffix() {

        return UUID
                .randomUUID()
                .toString()
                .replace(
                        "-",
                        ""
                )
                .substring(
                        0,
                        10
                );
    }


    // =====================================================
    // SESSION
    // =====================================================

    private record TenantSession(

            Long tenantId,

            Cookie authCookie,

            String email

    ) {
    }
}