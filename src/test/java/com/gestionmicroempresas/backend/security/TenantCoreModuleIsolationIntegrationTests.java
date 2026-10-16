package com.gestionmicroempresas.backend.security;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import org.springframework.mock.web.MockMultipartFile;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TenantCoreModuleIsolationIntegrationTests {

    private static final String DATABASE_NAME =
            "gestion_microempresas_test";


    private static final String APPLICATION_DATABASE_USER =
            "gestion_app_test";


    private static final String APPLICATION_DATABASE_PASSWORD =
            "gestion_test_password";


    private static final String JWT_TEST_SECRET =
            "integration-test-jwt-secret-must-have-at-least-32-bytes-2026";


    // =====================================================
    // POSTGRESQL
    // =====================================================

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


    // =====================================================
    // TEST CONFIGURATION
    // =====================================================

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {

        // =============================================
        // DATABASE
        // =============================================

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


        // =============================================
        // FLYWAY
        // =============================================

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


        // =============================================
        // JWT
        // =============================================

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


        // =============================================
        // BOOTSTRAP
        // =============================================

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


        // =============================================
        // RATE LIMIT
        // =============================================

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


        // =============================================
        // STORAGE
        // =============================================

        registry.add(
                "app.storage.base-directory",
                () ->
                        "target/test-storage-core"
        );


        // =============================================
        // URLS
        // =============================================

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


    // =====================================================
    // TEST 1
    // INCOMES
    // =====================================================

    @Test
    void incomesMustNotCrossTenantBoundaries()
            throws Exception {

        TenantSession tenantA =
                registerTenant(
                        "Income Company A"
                );


        TenantSession tenantB =
                registerTenant(
                        "Income Company B"
                );


        Long clientAId =
                createClient(
                        tenantA,
                        "Income Client A"
                );


        Long clientBId =
                createClient(
                        tenantB,
                        "Income Client B"
                );


        // =============================================
        // A CANNOT CREATE AN INCOME USING B'S CLIENT
        // =============================================

        mockMvc.perform(

                        post(
                                "/api/ingresos"
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
                                                incomeRequest(
                                                        clientBId,
                                                        "Foreign client attack"
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =============================================
        // B CREATES A VALID INCOME
        // =============================================

        Long incomeBId =
                createIncome(
                        tenantB,
                        clientBId,
                        "Private income B"
                );


        // =============================================
        // A CANNOT UPDATE B'S INCOME
        // =============================================

        mockMvc.perform(

                        put(
                                "/api/ingresos/{id}",
                                incomeBId
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
                                                incomeRequest(
                                                        clientAId,
                                                        "Attempted modification"
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =============================================
        // A CANNOT DELETE B'S INCOME
        // =============================================

        mockMvc.perform(

                        delete(
                                "/api/ingresos/{id}",
                                incomeBId
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


        // =============================================
        // B CAN STILL MODIFY ITS OWN INCOME
        // =============================================

        mockMvc.perform(

                        put(
                                "/api/ingresos/{id}",
                                incomeBId
                        )

                                .with(
                                        csrf()
                                )

                                .cookie(
                                        tenantB.authCookie()
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        json(
                                                incomeRequest(
                                                        clientBId,
                                                        "Income B intact"
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath(
                                "$.id"
                        )
                                .value(
                                        incomeBId
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.concepto"
                        )
                                .value(
                                        "Income B intact"
                                )
                );
    }


    // =====================================================
    // TEST 2
    // MANUAL EXPENSES
    // =====================================================

    @Test
    void expensesMustNotCrossTenantBoundaries()
            throws Exception {

        TenantSession tenantA =
                registerTenant(
                        "Expense Company A"
                );


        TenantSession tenantB =
                registerTenant(
                        "Expense Company B"
                );


        Long expenseBId =
                createExpense(
                        tenantB,
                        "Private expense B"
                );


        // =============================================
        // A CANNOT UPDATE B'S EXPENSE
        // =============================================

        mockMvc.perform(

                        put(
                                "/api/egresos/{id}",
                                expenseBId
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
                                                expenseRequest(
                                                        "Compromised expense"
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =============================================
        // A CANNOT DELETE B'S EXPENSE
        // =============================================

        mockMvc.perform(

                        delete(
                                "/api/egresos/{id}",
                                expenseBId
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


        // =============================================
        // B CAN STILL UPDATE ITS OWN EXPENSE
        // =============================================

        mockMvc.perform(

                        put(
                                "/api/egresos/{id}",
                                expenseBId
                        )

                                .with(
                                        csrf()
                                )

                                .cookie(
                                        tenantB.authCookie()
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        json(
                                                expenseRequest(
                                                        "Expense B intact"
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath(
                                "$.id"
                        )
                                .value(
                                        expenseBId
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.concepto"
                        )
                                .value(
                                        "Expense B intact"
                                )
                );
    }


    // =====================================================
    // TEST 3
    // USERS
    // =====================================================

    @Test
    void administratorsMustNotManageUsersFromAnotherTenant()
            throws Exception {

        TenantSession tenantA =
                registerTenant(
                        "Users Company A"
                );


        TenantSession tenantB =
                registerTenant(
                        "Users Company B"
                );


        // =============================================
        // A TRIES TO MODIFY B'S ADMINISTRATOR
        // =============================================

        Map<String, Object> updateRequest =
                Map.of(
                        "nombre",
                        "Compromised User",

                        "email",
                        "compromised."
                                +
                                uniqueSuffix()
                                +
                                "@example.test",

                        "rol",
                        "empleado"
                );


        mockMvc.perform(

                        put(
                                "/api/usuarios/{id}",
                                tenantB.userId()
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


        // =============================================
        // A TRIES TO DELETE B'S ADMINISTRATOR
        // =============================================

        mockMvc.perform(

                        delete(
                                "/api/usuarios/{id}",
                                tenantB.userId()
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


        // =============================================
        // B'S SESSION MUST STILL WORK
        // =============================================

        mockMvc.perform(

                        get(
                                "/api/auth/me"
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
                                "$.id"
                        )
                                .value(
                                        tenantB.userId()
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.empresa.id"
                        )
                                .value(
                                        tenantB.tenantId()
                                )
                );
    }


    // =====================================================
    // TEST 4
    // TASKS
    // =====================================================

    @Test
    void tasksMustNotCrossTenantBoundaries()
            throws Exception {

        TenantSession tenantA =
                registerTenant(
                        "Tasks Company A"
                );


        TenantSession tenantB =
                registerTenant(
                        "Tasks Company B"
                );


        // =============================================
        // A CANNOT ASSIGN A TASK TO A USER FROM B
        // =============================================

        mockMvc.perform(

                        post(
                                "/api/tareas"
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
                                                taskRequest(
                                                        "Cross tenant assignment",
                                                        tenantB.userId()
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =============================================
        // B CREATES ITS OWN TASK
        // =============================================

        Long taskBId =
                createTask(
                        tenantB,
                        "Private task B"
                );


        // =============================================
        // A CANNOT VIEW B'S TASK
        // =============================================

        mockMvc.perform(

                        get(
                                "/api/tareas/{id}",
                                taskBId
                        )

                                .cookie(
                                        tenantA.authCookie()
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =============================================
        // A CANNOT UPDATE B'S TASK
        // =============================================

        mockMvc.perform(

                        put(
                                "/api/tareas/{id}",
                                taskBId
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
                                                taskRequest(
                                                        "Compromised task",
                                                        tenantA.userId()
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =============================================
        // A CANNOT DELETE B'S TASK
        // =============================================

        mockMvc.perform(

                        delete(
                                "/api/tareas/{id}",
                                taskBId
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


        // =============================================
        // B CAN STILL READ ITS OWN TASK
        // =============================================

        mockMvc.perform(

                        get(
                                "/api/tareas/{id}",
                                taskBId
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
                                "$.id"
                        )
                                .value(
                                        taskBId
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.titulo"
                        )
                                .value(
                                        "Private task B"
                                )
                );
    }


    // =====================================================
    // TEST 5
    // INVENTORY
    // =====================================================

    @Test
    void inventoryMustNotCrossTenantBoundaries()
            throws Exception {

        TenantSession tenantA =
                registerTenant(
                        "Inventory Company A"
                );


        TenantSession tenantB =
                registerTenant(
                        "Inventory Company B"
                );


        ProductData productB =
                createProduct(
                        tenantB,
                        "Private product B"
                );


        // =============================================
        // A CANNOT FIND B'S PRODUCT BY QR
        // =============================================

        mockMvc.perform(

                        get(
                                "/api/inventario/qr"
                        )

                                .param(
                                        "codigo",
                                        productB.qrCode()
                                )

                                .cookie(
                                        tenantA.authCookie()
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =============================================
        // A CANNOT DUPLICATE B'S PRODUCT
        // =============================================

        mockMvc.perform(

                        post(
                                "/api/inventario/{id}/duplicar",
                                productB.id()
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


        // =============================================
        // A CANNOT ADJUST B'S STOCK
        // =============================================

        mockMvc.perform(

                        post(
                                "/api/inventario/{id}/ajustes",
                                productB.id()
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
                                                Map.of(
                                                        "tipo",
                                                        "ENTRADA",

                                                        "cantidad",
                                                        5,

                                                        "motivo",
                                                        "Cross tenant attack"
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =============================================
        // A CANNOT UPDATE B'S PRODUCT
        // =============================================

        MockMultipartFile updateData =
                jsonPart(
                        "datos",
                        inventoryUpdateRequest(
                                "Compromised product"
                        )
                );


        MockMultipartHttpServletRequestBuilder updateRequest =
                multipart(
                        "/api/inventario/{id}",
                        productB.id()
                );


        updateRequest.with(
                request -> {

                    request.setMethod(
                            "PUT"
                    );

                    return request;
                }
        );


        mockMvc.perform(

                        updateRequest

                                .file(
                                        updateData
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


        // =============================================
        // A CANNOT DELETE B'S PRODUCT
        // =============================================

        mockMvc.perform(

                        delete(
                                "/api/inventario/{id}",
                                productB.id()
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


        // =============================================
        // B CAN STILL FIND ITS OWN PRODUCT
        // =============================================

        mockMvc.perform(

                        get(
                                "/api/inventario/qr"
                        )

                                .param(
                                        "codigo",
                                        productB.qrCode()
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
                                "$.id"
                        )
                                .value(
                                        productB.id()
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.descripcion"
                        )
                                .value(
                                        "Private product B"
                                )
                );
    }


    // =====================================================
    // TENANT REGISTRATION
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

                        .andReturn();


        JsonNode response =
                objectMapper.readTree(

                        result
                                .getResponse()
                                .getContentAsString()
                );


        Long userId =
                response
                        .path(
                                "id"
                        )
                        .asLong();


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
                userId,
                authCookie,
                email
        );
    }


    // =====================================================
    // CLIENT
    // =====================================================

    private Long createClient(
            TenantSession tenant,
            String businessName
    )
            throws Exception {

        String suffix =
                uniqueSuffix();


        Map<String, Object> request =
                Map.of(
                        "razonSocial",
                        businessName,

                        "rfc",
                        rfcFromSuffix(
                                suffix
                        ),

                        "telefonos",
                        List.of(
                                phoneFromSuffix(
                                        suffix
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


        return readId(
                result
        );
    }


    // =====================================================
    // INCOME
    // =====================================================

    private Long createIncome(
            TenantSession tenant,
            Long clientId,
            String concept
    )
            throws Exception {

        MvcResult result =
                mockMvc.perform(

                                post(
                                        "/api/ingresos"
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
                                                        incomeRequest(
                                                                clientId,
                                                                concept
                                                        )
                                                )
                                        )
                        )

                        .andExpect(
                                status().isCreated()
                        )

                        .andReturn();


        return readId(
                result
        );
    }


    private Map<String, Object> incomeRequest(
            Long clientId,
            String concept
    ) {

        return Map.of(
                "clienteId",
                clientId,

                "tipoRegistro",
                "INGRESO",

                "concepto",
                concept,

                "subtotal",
                1250.50,

                "aplicaIva",
                false,

                "estado",
                "PAGADO",

                "fecha",
                "2026-08-28",

                "fechaPago",
                "2026-08-28",

                "formaPago",
                "TRANSFERENCIA"
        );
    }


    // =====================================================
    // EXPENSE
    // =====================================================

    private Long createExpense(
            TenantSession tenant,
            String concept
    )
            throws Exception {

        MvcResult result =
                mockMvc.perform(

                                post(
                                        "/api/egresos"
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
                                                        expenseRequest(
                                                                concept
                                                        )
                                                )
                                        )
                        )

                        .andExpect(
                                status().isCreated()
                        )

                        .andReturn();


        return readId(
                result
        );
    }


    private Map<String, Object> expenseRequest(
            String concept
    ) {

        return Map.of(
                "concepto",
                concept,

                "categoria",
                "OPERACION",

                "monto",
                450.75,

                "fecha",
                "2026-08-28",

                "descripcion",
                "Integration test expense"
        );
    }


    // =====================================================
    // TASK
    // =====================================================

    private Long createTask(
            TenantSession tenant,
            String title
    )
            throws Exception {

        MvcResult result =
                mockMvc.perform(

                                post(
                                        "/api/tareas"
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
                                                        taskRequest(
                                                                title,
                                                                tenant.userId()
                                                        )
                                                )
                                        )
                        )

                        .andExpect(
                                status().isCreated()
                        )

                        .andReturn();


        return readId(
                result
        );
    }


    private Map<String, Object> taskRequest(
            String title,
            Long assigneeId
    ) {

        return Map.of(
                "titulo",
                title,

                "descripcion",
                "Integration test task",

                "fechaLimite",
                "2026-09-30T18:00:00Z",

                "prioridad",
                "IMPORTANTE_NO_URGENTE",

                "asignadosIds",
                List.of(
                        assigneeId
                )
        );
    }


    // =====================================================
    // INVENTORY
    // =====================================================

    private ProductData createProduct(
            TenantSession tenant,
            String description
    )
            throws Exception {

        MockMultipartFile data =
                jsonPart(
                        "datos",
                        inventoryCreateRequest(
                                description
                        )
                );


        MvcResult result =
                mockMvc.perform(

                                multipart(
                                        "/api/inventario"
                                )

                                        .file(
                                                data
                                        )

                                        .with(
                                                csrf()
                                        )

                                        .cookie(
                                                tenant.authCookie()
                                        )
                        )

                        .andExpect(
                                status().isCreated()
                        )

                        .andReturn();


        JsonNode response =
                readJson(
                        result
                );


        return new ProductData(

                response
                        .path(
                                "id"
                        )
                        .asLong(),

                response
                        .path(
                                "codigoQr"
                        )
                        .asText()
        );
    }


    private Map<String, Object> inventoryCreateRequest(
            String description
    ) {

        return Map.of(
                "descripcion",
                description,

                "modelo",
                "MODEL-TEST",

                "fila",
                "A-01",

                "precioUnitario",
                100.00,

                "stockMinimo",
                1,

                "cantidadInicial",
                10
        );
    }


    private Map<String, Object> inventoryUpdateRequest(
            String description
    ) {

        return Map.of(
                "descripcion",
                description,

                "modelo",
                "MODEL-HACK",

                "fila",
                "X-99",

                "precioUnitario",
                1.00,

                "stockMinimo",
                0
        );
    }


    // =====================================================
    // JSON MULTIPART
    // =====================================================

    private MockMultipartFile jsonPart(
            String name,
            Object value
    )
            throws Exception {

        return new MockMultipartFile(

                name,

                name
                        +
                        ".json",

                MediaType.APPLICATION_JSON_VALUE,

                json(
                        value
                )
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
        );
    }


    // =====================================================
    // RESPONSE
    // =====================================================

    private Long readId(
            MvcResult result
    )
            throws Exception {

        return readJson(
                result
        )
                .path(
                        "id"
                )
                .asLong();
    }


    private JsonNode readJson(
            MvcResult result
    )
            throws Exception {

        return objectMapper.readTree(

                result
                        .getResponse()
                        .getContentAsString()
        );
    }


    // =====================================================
    // AUTH COOKIE
    // =====================================================

    private Cookie extractAuthenticationCookie(
            MvcResult result
    ) {

        List<String> headers =
                result
                        .getResponse()
                        .getHeaders(
                                HttpHeaders.SET_COOKIE
                        );


        String authenticationHeader =
                headers
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
                                                "The authentication cookie was not created."
                                        )
                        );


        String pair =
                authenticationHeader
                        .split(
                                ";",
                                2
                        )[0];


        int separator =
                pair.indexOf(
                        '='
                );


        String value =
                pair.substring(
                        separator
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
    // TEST DATA
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


    private String phoneFromSuffix(
            String suffix
    ) {

        String numeric =
                suffix
                        .chars()

                        .map(
                                character ->
                                        Character.isDigit(
                                                character
                                        )
                                                ?
                                                character
                                                :
                                                '1'
                        )

                        .collect(

                                StringBuilder::new,

                                StringBuilder::appendCodePoint,

                                StringBuilder::append
                        )

                        .toString();


        return "55"
                +
                numeric.substring(
                        0,
                        8
                );
    }


    private String rfcFromSuffix(
            String suffix
    ) {

        String normalized =
                suffix
                        .toUpperCase()
                        .replaceAll(
                                "[^A-Z0-9]",
                                "A"
                        );


        return "TST010101"
                +
                normalized.substring(
                        0,
                        3
                );
    }


    // =====================================================
    // DATA RECORDS
    // =====================================================

    private record TenantSession(

            Long tenantId,

            Long userId,

            Cookie authCookie,

            String email

    ) {
    }


    private record ProductData(

            Long id,

            String qrCode

    ) {
    }

    // =====================================================
// TEST 6
// TRAVELS + RECEIPTS
// =====================================================

    @Test
    void travelsAndReceiptsMustNotCrossTenantBoundaries()
            throws Exception {

        TenantSession tenantA =
                registerTenant(
                        "Travel Company A"
                );


        TenantSession tenantB =
                registerTenant(
                        "Travel Company B"
                );


        // =================================================
        // A CANNOT CREATE A TRAVEL WITH USER B
        // =================================================

        mockMvc.perform(

                        post(
                                "/api/viaticos"
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
                                                travelRequest(
                                                        "Invalid cross tenant travel",
                                                        tenantB.userId()
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =================================================
        // B CREATES ITS OWN TRAVEL
        // =================================================

        Long travelBId =
                createTravelAdvanced(
                        tenantB,
                        "Private travel B"
                );


        // =================================================
        // A CANNOT READ B'S TRAVEL
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/viaticos/{id}",
                                travelBId
                        )

                                .cookie(
                                        tenantA.authCookie()
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =================================================
        // B UPLOADS AN EXPENSE + RECEIPT
        // =================================================

        MvcResult expenseResult =
                registerTravelExpenseWithReceipt(
                        tenantB,
                        travelBId
                );


        JsonNode expenseResponse =
                readJson(
                        expenseResult
                );


        JsonNode expenses =
                expenseResponse
                        .path(
                                "gastos"
                        );


        org.junit.jupiter.api.Assertions
                .assertFalse(
                        expenses.isEmpty(),
                        "El viaje debe contener el gasto registrado."
                );


        JsonNode expense =
                expenses.get(
                        0
                );


        String receiptUrl =
                expense
                        .path(
                                "comprobanteUrl"
                        )
                        .asText();


        org.junit.jupiter.api.Assertions
                .assertFalse(
                        receiptUrl.isBlank(),
                        "El gasto debe contener la URL del comprobante."
                );


        String receiptFileName =
                fileNameFromUrl(
                        receiptUrl
                );


        // =================================================
        // A CANNOT DOWNLOAD B'S RECEIPT
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/archivos/viaticos/{fileName}",
                                receiptFileName
                        )

                                .cookie(
                                        tenantA.authCookie()
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =================================================
        // B CAN DOWNLOAD ITS OWN RECEIPT
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/archivos/viaticos/{fileName}",
                                receiptFileName
                        )

                                .cookie(
                                        tenantB.authCookie()
                                )
                )

                .andExpect(
                        status().isOk()
                );


        // =================================================
        // A CANNOT DELETE B'S TRAVEL
        // =================================================

        mockMvc.perform(

                        delete(
                                "/api/viaticos/{id}",
                                travelBId
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
        // B'S TRAVEL STILL EXISTS
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/viaticos/{id}",
                                travelBId
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
                                "$.id"
                        )
                                .value(
                                        travelBId
                                )
                );
    }


// =====================================================
// TEST 7
// TASK EVIDENCE FILES
// =====================================================

    @Test
    void taskEvidenceFilesMustNotCrossTenantBoundaries()
            throws Exception {

        TenantSession tenantA =
                registerTenant(
                        "Evidence Company A"
                );


        TenantSession tenantB =
                registerTenant(
                        "Evidence Company B"
                );


        Long taskBId =
                createTask(
                        tenantB,
                        "Private evidence task B"
                );


        MvcResult evidenceResult =
                uploadTaskEvidenceAdvanced(
                        tenantB,
                        taskBId
                );


        JsonNode evidenceArray =
                readJson(
                        evidenceResult
                );


        org.junit.jupiter.api.Assertions
                .assertFalse(
                        evidenceArray.isEmpty(),
                        "Debe haberse creado al menos una evidencia."
                );


        JsonNode evidence =
                evidenceArray.get(
                        0
                );


        Long evidenceId =
                evidence
                        .path(
                                "id"
                        )
                        .asLong();


        String evidenceUrl =
                evidence
                        .path(
                                "url"
                        )
                        .asText();


        org.junit.jupiter.api.Assertions
                .assertFalse(
                        evidenceUrl.isBlank(),
                        "La evidencia debe contener una URL."
                );


        String evidenceFileName =
                fileNameFromUrl(
                        evidenceUrl
                );


        // =================================================
        // A CANNOT DOWNLOAD B'S EVIDENCE
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/archivos/tareas/{fileName}",
                                evidenceFileName
                        )

                                .cookie(
                                        tenantA.authCookie()
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =================================================
        // B CAN DOWNLOAD ITS OWN EVIDENCE
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/archivos/tareas/{fileName}",
                                evidenceFileName
                        )

                                .cookie(
                                        tenantB.authCookie()
                                )
                )

                .andExpect(
                        status().isOk()
                );


        // =================================================
        // A CANNOT DELETE B'S EVIDENCE
        // =================================================

        mockMvc.perform(

                        delete(
                                "/api/tareas/{id}/evidencias/{evidenciaId}",
                                taskBId,
                                evidenceId
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
        // EVIDENCE STILL EXISTS FOR B
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/tareas/{id}/evidencias",
                                taskBId
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
                                "$[0].id"
                        )
                                .value(
                                        evidenceId
                                )
                );
    }


// =====================================================
// TEST 8
// INVENTORY PHOTO FILES
// =====================================================

    @Test
    void inventoryPhotoFilesMustNotCrossTenantBoundaries()
            throws Exception {

        TenantSession tenantA =
                registerTenant(
                        "Photo Company A"
                );


        TenantSession tenantB =
                registerTenant(
                        "Photo Company B"
                );


        ProductPhotoData productB =
                createProductWithPhotoAdvanced(
                        tenantB,
                        "Private photographed product B"
                );


        org.junit.jupiter.api.Assertions
                .assertFalse(
                        productB.photoUrl().isBlank(),
                        "El producto debe tener una foto."
                );


        String photoFileName =
                fileNameFromUrl(
                        productB.photoUrl()
                );


        // =================================================
        // A CANNOT DOWNLOAD B'S PHOTO
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/archivos/inventario/{fileName}",
                                photoFileName
                        )

                                .cookie(
                                        tenantA.authCookie()
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =================================================
        // B CAN DOWNLOAD ITS OWN PHOTO
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/archivos/inventario/{fileName}",
                                photoFileName
                        )

                                .cookie(
                                        tenantB.authCookie()
                                )
                )

                .andExpect(
                        status().isOk()
                );


        // =================================================
        // A CANNOT DUPLICATE B'S PRODUCT
        // =================================================

        mockMvc.perform(

                        post(
                                "/api/inventario/{id}/duplicar",
                                productB.id()
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
    }


// =====================================================
// TEST 9
// KITS / BOM
// =====================================================

    @Test
    void kitsMustNotUseProductsFromAnotherTenant()
            throws Exception {

        TenantSession tenantA =
                registerTenant(
                        "Kit Company A"
                );


        TenantSession tenantB =
                registerTenant(
                        "Kit Company B"
                );


        ProductData productB =
                createProduct(
                        tenantB,
                        "Private component B"
                );


        // =================================================
        // A TRIES TO CREATE A KIT USING PRODUCT B
        // =================================================

        mockMvc.perform(

                        multipart(
                                "/api/configuracion/kits"
                        )

                                .file(
                                        jsonPart(
                                                "datos",
                                                kitRequestAdvanced(
                                                        "Illegal kit A",
                                                        productB.id()
                                                )
                                        )
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
        // B CREATES A VALID KIT
        // =================================================

        Long kitBId =
                createKitAdvanced(
                        tenantB,
                        productB.id(),
                        "Private kit B"
                );


        // =================================================
        // A CANNOT ASSEMBLE B'S KIT
        // =================================================

        mockMvc.perform(

                        post(
                                "/api/inventario/{id}/armar",
                                kitBId
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
                                                Map.of(
                                                        "cantidad",
                                                        1
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        // =================================================
        // A CANNOT DELETE B'S KIT
        // =================================================

        mockMvc.perform(

                        delete(
                                "/api/configuracion/kits/{id}",
                                kitBId
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
        // B CAN ASSEMBLE ITS OWN KIT
        // =================================================

        mockMvc.perform(

                        post(
                                "/api/inventario/{id}/armar",
                                kitBId
                        )

                                .with(
                                        csrf()
                                )

                                .cookie(
                                        tenantB.authCookie()
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        json(
                                                Map.of(
                                                        "cantidad",
                                                        1
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isNoContent()
                );


        // =================================================
        // B CAN DISASSEMBLE IT
        // =================================================

        mockMvc.perform(

                        post(
                                "/api/inventario/{id}/desarmar",
                                kitBId
                        )

                                .with(
                                        csrf()
                                )

                                .cookie(
                                        tenantB.authCookie()
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        json(
                                                Map.of(
                                                        "cantidad",
                                                        1
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isNoContent()
                );
    }


// =====================================================
// TEST 10
// ROLE MATRIX
// =====================================================

// =====================================================
// TEST 10
// ROLE MATRIX
// =====================================================

    @Test
    void employeeAndEditorPermissionsMustBeEnforced()
            throws Exception {

        TenantSession tenant =
                registerTenant(
                        "Roles Company"
                );


        UserSession employee =
                createAndLoginUserAdvanced(
                        tenant,
                        "empleado"
                );


        UserSession editor =
                createAndLoginUserAdvanced(
                        tenant,
                        "editor"
                );


        // =================================================
        // EMPLOYEE CANNOT ACCESS USER ADMINISTRATION
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/usuarios"
                        )

                                .cookie(
                                        employee.authCookie()
                                )
                )

                .andExpect(
                        status().isForbidden()
                );


        // =================================================
        // EMPLOYEE CANNOT DOWNLOAD SYSTEM BACKUP
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/configuracion/respaldo"
                        )

                                .cookie(
                                        employee.authCookie()
                                )
                )

                .andExpect(
                        status().isForbidden()
                );


        // =================================================
        // EMPLOYEE CANNOT VIEW FINANCIAL DASHBOARD
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/dashboard/financiero/mensual"
                        )

                                .param(
                                        "anio",
                                        "2026"
                                )

                                .cookie(
                                        employee.authCookie()
                                )
                )

                .andExpect(
                        status().isForbidden()
                );


        // =================================================
        // EMPLOYEE CANNOT CREATE TRAVELS
        // =================================================

        mockMvc.perform(

                        post(
                                "/api/viaticos"
                        )

                                .with(
                                        csrf()
                                )

                                .cookie(
                                        employee.authCookie()
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        json(
                                                travelRequest(
                                                        "Forbidden employee travel",
                                                        employee.userId()
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isForbidden()
                );


        // =================================================
        // EDITOR CAN VIEW FINANCIAL DASHBOARD
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/dashboard/financiero/mensual"
                        )

                                .param(
                                        "anio",
                                        "2026"
                                )

                                .cookie(
                                        editor.authCookie()
                                )
                )

                .andExpect(
                        status().isOk()
                );


        // =================================================
        // EDITOR CANNOT ADMINISTER USERS
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/usuarios"
                        )

                                .cookie(
                                        editor.authCookie()
                                )
                )

                .andExpect(
                        status().isForbidden()
                );


        // =================================================
        // ADMIN CAN ADMINISTER USERS
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/usuarios"
                        )

                                .cookie(
                                        tenant.authCookie()
                                )
                )

                .andExpect(
                        status().isOk()
                );


        // =================================================
        // ADMIN CAN VIEW FINANCIAL DASHBOARD
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/dashboard/financiero/mensual"
                        )

                                .param(
                                        "anio",
                                        "2026"
                                )

                                .cookie(
                                        tenant.authCookie()
                                )
                )

                .andExpect(
                        status().isOk()
                );


        // =================================================
        // ADMIN CAN DOWNLOAD SYSTEM BACKUP
        // =================================================

        mockMvc.perform(

                        get(
                                "/api/configuracion/respaldo"
                        )

                                .cookie(
                                        tenant.authCookie()
                                )
                )

                .andExpect(
                        status().isOk()
                );
    }

// =====================================================
// ADVANCED HELPER
// TRAVEL REQUEST
// =====================================================
private byte[] validWebpTestBytes() {

    return new byte[]{

            'R',
            'I',
            'F',
            'F',

            8,
            0,
            0,
            0,

            'W',
            'E',
            'B',
            'P',

            'V',
            'P',
            '8',
            ' '
    };
}
    private Map<String, Object> travelRequest(
            String name,
            Long userId
    ) {

        return Map.of(

                "nombre",
                name,

                "destino",
                "Ciudad de México",

                "fechaInicio",
                "2026-08-28",

                "fechaFin",
                "2026-08-30",

                "usuariosIds",
                List.of(
                        userId
                ),

                "presupuesto",
                Map.of(
                        "alimentos",
                        1000.00,

                        "gasolina",
                        800.00,

                        "casetas",
                        300.00,

                        "hospedaje",
                        1500.00,

                        "material",
                        500.00,

                        "otros",
                        200.00
                )
        );
    }


// =====================================================
// ADVANCED HELPER
// CREATE TRAVEL
// =====================================================

    private Long createTravelAdvanced(
            TenantSession tenant,
            String name
    )
            throws Exception {

        MvcResult result =
                mockMvc.perform(

                                post(
                                        "/api/viaticos"
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
                                                        travelRequest(
                                                                name,
                                                                tenant.userId()
                                                        )
                                                )
                                        )
                        )

                        .andExpect(
                                status().isCreated()
                        )

                        .andReturn();


        return readId(
                result
        );
    }


// =====================================================
// ADVANCED HELPER
// TRAVEL RECEIPT
// =====================================================

    private MvcResult registerTravelExpenseWithReceipt(
            TenantSession tenant,
            Long travelId
    )
            throws Exception {

        MockMultipartFile data =
                new MockMultipartFile(

                        "datos",

                        "datos.json",

                        MediaType.APPLICATION_JSON_VALUE,

                        json(
                                List.of(

                                        Map.of(
                                                "categoria",
                                                "ALIMENTOS",

                                                "descripcion",
                                                "Comida de integración",

                                                "monto",
                                                250.50,

                                                "fecha",
                                                "2026-08-29",

                                                "comprobanteIndice",
                                                0
                                        )
                                )
                        )
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );


        /*
         * The current backend validates image/*.
         * The bytes do not need to represent a real image
         * for this storage/isolation integration test.
         */
        MockMultipartFile receipt =
                new MockMultipartFile(

                        "comprobantes",

                        "receipt.webp",

                        "image/webp",

                        validWebpTestBytes()
                );

        return mockMvc.perform(

                        multipart(
                                "/api/viaticos/{id}/gastos/lote",
                                travelId
                        )

                                .file(
                                        data
                                )

                                .file(
                                        receipt
                                )

                                .with(
                                        csrf()
                                )

                                .cookie(
                                        tenant.authCookie()
                                )
                )

                .andExpect(
                        status().isCreated()
                )

                .andReturn();
    }


// =====================================================
// ADVANCED HELPER
// TASK EVIDENCE
// =====================================================

    private MvcResult uploadTaskEvidenceAdvanced(
            TenantSession tenant,
            Long taskId
    )
            throws Exception {

        MockMultipartFile evidence =
                new MockMultipartFile(

                        "archivos",

                        "evidence.webp",

                        "image/webp",

                        validWebpTestBytes()
                );


        return mockMvc.perform(

                        multipart(
                                "/api/tareas/{id}/evidencias",
                                taskId
                        )

                                .file(
                                        evidence
                                )

                                .with(
                                        csrf()
                                )

                                .cookie(
                                        tenant.authCookie()
                                )
                )

                .andExpect(
                        status().isCreated()
                )

                .andReturn();
    }


// =====================================================
// ADVANCED HELPER
// INVENTORY PRODUCT WITH PHOTO
// =====================================================

    private ProductPhotoData createProductWithPhotoAdvanced(
            TenantSession tenant,
            String description
    )
            throws Exception {

        MockMultipartFile data =
                jsonPart(
                        "datos",
                        inventoryCreateRequest(
                                description
                        )
                );


        MockMultipartFile photo =
                new MockMultipartFile(

                        "foto",

                        "product.webp",

                        "image/webp",

                        validWebpTestBytes()
                );


        MvcResult result =
                mockMvc.perform(

                                multipart(
                                        "/api/inventario"
                                )

                                        .file(
                                                data
                                        )

                                        .file(
                                                photo
                                        )

                                        .with(
                                                csrf()
                                        )

                                        .cookie(
                                                tenant.authCookie()
                                        )
                        )

                        .andExpect(
                                status().isCreated()
                        )

                        .andReturn();


        JsonNode response =
                readJson(
                        result
                );


        return new ProductPhotoData(

                response
                        .path(
                                "id"
                        )
                        .asLong(),

                response
                        .path(
                                "fotoUrl"
                        )
                        .asText()
        );
    }


// =====================================================
// ADVANCED HELPER
// KIT REQUEST
// =====================================================

    private Map<String, Object> kitRequestAdvanced(
            String description,
            Long productId
    ) {

        return Map.of(

                "descripcion",
                description,

                "fila",
                "KIT-A1",

                "precioUnitario",
                350.00,

                "componentes",
                List.of(

                        Map.of(
                                "productoId",
                                productId,

                                "cantidadNecesaria",
                                1
                        )
                )
        );
    }


// =====================================================
// ADVANCED HELPER
// CREATE KIT
// =====================================================

    private Long createKitAdvanced(
            TenantSession tenant,
            Long productId,
            String description
    )
            throws Exception {

        MockMultipartFile data =
                jsonPart(

                        "datos",

                        kitRequestAdvanced(
                                description,
                                productId
                        )
                );


        MvcResult result =
                mockMvc.perform(

                                multipart(
                                        "/api/configuracion/kits"
                                )

                                        .file(
                                                data
                                        )

                                        .with(
                                                csrf()
                                        )

                                        .cookie(
                                                tenant.authCookie()
                                        )
                        )

                        .andExpect(
                                status().isCreated()
                        )

                        .andReturn();


        return readId(
                result
        );
    }


// =====================================================
// ADVANCED HELPER
// CREATE + LOGIN USER
// =====================================================

    private UserSession createAndLoginUserAdvanced(
            TenantSession tenant,
            String role
    )
            throws Exception {

        String suffix =
                uniqueSuffix();


        String email =
                role
                        +
                        "."
                        +
                        suffix
                        +
                        "@example.test";


        String password =
                "Password123!";


        MvcResult creationResult =
                mockMvc.perform(

                                post(
                                        "/api/usuarios"
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
                                                        Map.of(
                                                                "nombre",
                                                                "Test "
                                                                        +
                                                                        role,

                                                                "email",
                                                                email,

                                                                "rol",
                                                                role,

                                                                "password",
                                                                password
                                                        )
                                                )
                                        )
                        )

                        .andExpect(
                                status().isCreated()
                        )

                        .andReturn();


        Long userId =
                readId(
                        creationResult
                );


        MvcResult loginResult =
                mockMvc.perform(

                                post(
                                        "/api/auth/login"
                                )

                                        .with(
                                                csrf()
                                        )

                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )

                                        .content(
                                                json(
                                                        Map.of(
                                                                "email",
                                                                email,

                                                                "password",
                                                                password
                                                        )
                                                )
                                        )
                        )

                        .andExpect(
                                status().isOk()
                        )

                        .andReturn();


        Cookie authenticationCookie =
                extractAuthenticationCookie(
                        loginResult
                );


        return new UserSession(
                userId,
                authenticationCookie
        );
    }


// =====================================================
// ADVANCED HELPER
// FILE NAME
// =====================================================

    private String fileNameFromUrl(
            String url
    ) {

        if (
                url == null
                        ||
                        url.isBlank()
        ) {

            throw new AssertionError(
                    "The file URL is empty."
            );
        }


        int queryIndex =
                url.indexOf(
                        '?'
                );


        String withoutQuery =
                queryIndex >= 0
                        ?
                        url.substring(
                                0,
                                queryIndex
                        )
                        :
                        url;


        int separator =
                withoutQuery.lastIndexOf(
                        '/'
                );


        if (
                separator < 0
                        ||
                        separator
                                ==
                                withoutQuery.length()
                                        -
                                        1
        ) {

            throw new AssertionError(
                    "Could not extract the file name from URL: "
                            +
                            url
            );
        }


        return withoutQuery.substring(
                separator
                        +
                        1
        );
    }


// =====================================================
// ADVANCED TEST DATA
// =====================================================

    private record ProductPhotoData(

            Long id,

            String photoUrl

    ) {
    }


    private record UserSession(

            Long userId,

            Cookie authCookie

    ) {
    }
}