package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.exception.TooManyRequestsException;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuthRateLimitService {

    private static final long CLEANUP_INTERVAL =
            256;


    private static final long STALE_BUCKET_NANOS =
            Duration
                    .ofHours(2)
                    .toNanos();


    private final Map<String, BucketEntry> buckets =
            new ConcurrentHashMap<>();


    private final AtomicLong operations =
            new AtomicLong();


    private final int loginIpMaxAttempts;

    private final int loginIdentityMaxAttempts;

    private final Duration loginWindow;

    private final int registrationIpMaxAttempts;

    private final Duration registrationWindow;


    public AuthRateLimitService(

            @Value(
                    "${app.security.rate-limit.login.ip.max-attempts}"
            )
            int loginIpMaxAttempts,

            @Value(
                    "${app.security.rate-limit.login.identity.max-attempts}"
            )
            int loginIdentityMaxAttempts,

            @Value(
                    "${app.security.rate-limit.login.window-minutes}"
            )
            long loginWindowMinutes,

            @Value(
                    "${app.security.rate-limit.registration.ip.max-attempts}"
            )
            int registrationIpMaxAttempts,

            @Value(
                    "${app.security.rate-limit.registration.window-minutes}"
            )
            long registrationWindowMinutes
    ) {

        this.loginIpMaxAttempts =
                loginIpMaxAttempts;

        this.loginIdentityMaxAttempts =
                loginIdentityMaxAttempts;

        this.loginWindow =
                Duration.ofMinutes(
                        loginWindowMinutes
                );

        this.registrationIpMaxAttempts =
                registrationIpMaxAttempts;

        this.registrationWindow =
                Duration.ofMinutes(
                        registrationWindowMinutes
                );
    }


    // =====================================================
    // LOGIN
    // =====================================================

    public void checkLogin(
            HttpServletRequest request,
            String email
    ) {

        String clientIp =
                resolveClientIp(
                        request
                );


        String normalizedEmail =
                normalizeEmail(
                        email
                );


        consume(

                "auth:login:ip:"
                        +
                        clientIp,

                loginIpMaxAttempts,

                loginWindow,

                "Demasiados intentos de inicio de sesión. Espera un momento antes de volver a intentarlo."
        );


        consume(

                "auth:login:identity:"
                        +
                        clientIp
                        +
                        ":"
                        +
                        normalizedEmail,

                loginIdentityMaxAttempts,

                loginWindow,

                "Demasiados intentos de inicio de sesión. Espera un momento antes de volver a intentarlo."
        );
    }


    public void registerLoginSuccess(
            HttpServletRequest request,
            String email
    ) {

        String clientIp =
                resolveClientIp(
                        request
                );


        String normalizedEmail =
                normalizeEmail(
                        email
                );


        buckets.remove(

                "auth:login:identity:"
                        +
                        clientIp
                        +
                        ":"
                        +
                        normalizedEmail
        );
    }


    // =====================================================
    // REGISTRATION
    // =====================================================

    public void checkRegistration(
            HttpServletRequest request
    ) {

        String clientIp =
                resolveClientIp(
                        request
                );


        consume(

                "auth:registration:ip:"
                        +
                        clientIp,

                registrationIpMaxAttempts,

                registrationWindow,

                "Se han realizado demasiados registros desde esta conexión. Intenta nuevamente más tarde."
        );
    }


    // =====================================================
    // BUCKET
    // =====================================================

    private void consume(
            String key,
            int capacity,
            Duration refillPeriod,
            String rejectionMessage
    ) {

        long now =
                System.nanoTime();


        BucketEntry entry =
                buckets.compute(

                        key,

                        (
                                ignored,
                                current
                        ) -> {

                            if (current == null) {

                                return new BucketEntry(

                                        createBucket(
                                                capacity,
                                                refillPeriod
                                        ),

                                        now
                                );
                            }


                            current.touch(
                                    now
                            );


                            return current;
                        }
                );


        ConsumptionProbe probe =
                entry
                        .bucket()
                        .tryConsumeAndReturnRemaining(
                                1
                        );


        cleanupIfNecessary(
                now
        );


        if (
                probe.isConsumed()
        ) {

            return;
        }


        long retryAfterSeconds =
                Math.max(

                        1,

                        TimeUnit.NANOSECONDS
                                .toSeconds(
                                        probe.getNanosToWaitForRefill()
                                )
                                +
                                1
                );


        throw new TooManyRequestsException(

                rejectionMessage,

                retryAfterSeconds
        );
    }


    private Bucket createBucket(
            int capacity,
            Duration refillPeriod
    ) {

        if (capacity <= 0) {

            throw new IllegalArgumentException(
                    "La capacidad del rate limit debe ser mayor que cero."
            );
        }


        if (
                refillPeriod.isZero()
                        ||
                        refillPeriod.isNegative()
        ) {

            throw new IllegalArgumentException(
                    "La ventana del rate limit debe ser mayor que cero."
            );
        }


        return Bucket
                .builder()

                .addLimit(

                        limit ->
                                limit

                                        .capacity(
                                                capacity
                                        )

                                        .refillGreedy(
                                                capacity,
                                                refillPeriod
                                        )
                )

                .build();
    }


    // =====================================================
    // CLEANUP
    // =====================================================

    private void cleanupIfNecessary(
            long now
    ) {

        long currentOperation =
                operations.incrementAndGet();


        if (
                currentOperation
                        %
                        CLEANUP_INTERVAL
                        !=
                        0
        ) {

            return;
        }


        long staleBefore =
                now
                        -
                        STALE_BUCKET_NANOS;


        buckets
                .entrySet()
                .removeIf(

                        entry ->
                                entry
                                        .getValue()
                                        .lastAccessNanos()
                                        <
                                        staleBefore
                );
    }


    // =====================================================
    // CLIENT IDENTITY
    // =====================================================

    private String resolveClientIp(
            HttpServletRequest request
    ) {

        String address =
                request.getRemoteAddr();


        if (
                address == null
                        ||
                        address.isBlank()
        ) {

            return "unknown";
        }


        return address.trim();
    }


    private String normalizeEmail(
            String email
    ) {

        if (
                email == null
                        ||
                        email.isBlank()
        ) {

            return "unknown";
        }


        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }


    // =====================================================
    // INTERNAL ENTRY
    // =====================================================

    private static final class BucketEntry {

        private final Bucket bucket;

        private volatile long lastAccessNanos;


        private BucketEntry(
                Bucket bucket,
                long lastAccessNanos
        ) {

            this.bucket =
                    bucket;

            this.lastAccessNanos =
                    lastAccessNanos;
        }


        private Bucket bucket() {
            return bucket;
        }


        private long lastAccessNanos() {
            return lastAccessNanos;
        }


        private void touch(
                long now
        ) {

            this.lastAccessNanos =
                    now;
        }
    }
}