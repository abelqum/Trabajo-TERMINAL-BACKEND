package com.gestionmicroempresas.backend.config;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.Test;

import org.slf4j.MDC;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RequestIdFilterTests {

    private final RequestIdFilter filter =
            new RequestIdFilter();


    // =====================================================
    // REQUEST ID HEADER
    // =====================================================

    @Test
    void everyRequestMustReceiveRequestId()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/test"
                );


        MockHttpServletResponse response =
                new MockHttpServletResponse();


        filter.doFilter(

                request,

                response,

                emptyChain()
        );


        String requestId =
                response.getHeader(
                        RequestIdFilter.REQUEST_ID_HEADER
                );


        assertNotNull(
                requestId
        );


        assertFalse(
                requestId.isBlank()
        );


        assertDoesNotThrow(
                () ->
                        java.util.UUID.fromString(
                                requestId
                        )
        );
    }


    // =====================================================
    // MDC DURING REQUEST
    // =====================================================

    @Test
    void requestIdMustBeAvailableInsideRequestMdc()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/api/test"
                );


        MockHttpServletResponse response =
                new MockHttpServletResponse();


        AtomicReference<String> observedRequestId =
                new AtomicReference<>();


        FilterChain chain =
                (servletRequest, servletResponse) ->

                        observedRequestId.set(

                                MDC.get(
                                        RequestIdFilter.REQUEST_ID_MDC_KEY
                                )
                        );


        filter.doFilter(
                request,
                response,
                chain
        );


        String responseRequestId =
                response.getHeader(
                        RequestIdFilter.REQUEST_ID_HEADER
                );


        assertNotNull(
                observedRequestId.get()
        );


        assertEquals(

                responseRequestId,

                observedRequestId.get()
        );
    }


    // =====================================================
    // MDC CLEANUP
    // =====================================================

    @Test
    void requestIdMustBeRemovedFromMdcAfterRequest()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/test"
                );


        MockHttpServletResponse response =
                new MockHttpServletResponse();


        filter.doFilter(

                request,

                response,

                emptyChain()
        );


        assertNull(

                MDC.get(
                        RequestIdFilter.REQUEST_ID_MDC_KEY
                )
        );
    }


    // =====================================================
    // UNIQUE IDs
    // =====================================================

    @Test
    void consecutiveRequestsMustReceiveDifferentIds()
            throws Exception {

        MockHttpServletResponse firstResponse =
                executeRequest();


        MockHttpServletResponse secondResponse =
                executeRequest();


        String firstRequestId =
                firstResponse.getHeader(
                        RequestIdFilter.REQUEST_ID_HEADER
                );


        String secondRequestId =
                secondResponse.getHeader(
                        RequestIdFilter.REQUEST_ID_HEADER
                );


        assertNotNull(
                firstRequestId
        );


        assertNotNull(
                secondRequestId
        );


        assertNotEquals(

                firstRequestId,

                secondRequestId
        );
    }


    // =====================================================
    // HELPERS
    // =====================================================

    private MockHttpServletResponse executeRequest()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/test"
                );


        MockHttpServletResponse response =
                new MockHttpServletResponse();


        filter.doFilter(

                request,

                response,

                emptyChain()
        );


        return response;
    }


    private FilterChain emptyChain() {

        return (
                request,
                response
        ) -> {
            // Intentionally empty.
        };
    }
}