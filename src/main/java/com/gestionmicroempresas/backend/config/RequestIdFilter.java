package com.gestionmicroempresas.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.UUID;

import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter
        extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER =
            "X-Request-ID";


    public static final String REQUEST_ID_MDC_KEY =
            "requestId";


    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    RequestIdFilter.class
            );


    // =====================================================
    // FILTER
    // =====================================================

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException,
            IOException {

        String requestId =
                generateRequestId();


        long startedAt =
                System.nanoTime();


        MDC.put(
                REQUEST_ID_MDC_KEY,
                requestId
        );


        response.setHeader(
                REQUEST_ID_HEADER,
                requestId
        );


        try {

            filterChain.doFilter(
                    request,
                    response
            );

        } finally {

            long elapsedNanos =
                    System.nanoTime()
                            -
                            startedAt;


            long elapsedMilliseconds =
                    TimeUnit.NANOSECONDS
                            .toMillis(
                                    elapsedNanos
                            );


            logRequest(

                    request,

                    response,

                    requestId,

                    elapsedMilliseconds
            );


            /*
             * Threads are reused by the application server.
             *
             * Therefore the MDC value MUST be removed after
             * every request or another request could inherit it.
             */
            MDC.remove(
                    REQUEST_ID_MDC_KEY
            );
        }
    }


    // =====================================================
    // REQUEST ID
    // =====================================================

    private String generateRequestId() {

        /*
         * We intentionally generate the ID ourselves instead
         * of trusting an X-Request-ID supplied by the client.
         */
        return UUID
                .randomUUID()
                .toString();
    }


    // =====================================================
    // LOG
    // =====================================================

    private void logRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            String requestId,
            long elapsedMilliseconds
    ) {

        String method =
                request.getMethod();


        String uri =
                request.getRequestURI();


        int status =
                response.getStatus();


        if (
                status >= 500
        ) {

            LOGGER.error(

                    "HTTP request failed: requestId={}, method={}, uri={}, status={}, durationMs={}",

                    requestId,

                    method,

                    uri,

                    status,

                    elapsedMilliseconds
            );

            return;
        }


        if (
                status >= 400
        ) {

            LOGGER.warn(

                    "HTTP request rejected: requestId={}, method={}, uri={}, status={}, durationMs={}",

                    requestId,

                    method,

                    uri,

                    status,

                    elapsedMilliseconds
            );

            return;
        }


        LOGGER.info(

                "HTTP request completed: requestId={}, method={}, uri={}, status={}, durationMs={}",

                requestId,

                method,

                uri,

                status,

                elapsedMilliseconds
        );
    }
}