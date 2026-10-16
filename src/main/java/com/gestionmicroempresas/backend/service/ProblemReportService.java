package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.ProblemReportRequest;

import com.gestionmicroempresas.backend.entity.Tenant;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class ProblemReportService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm:ss 'UTC'"
            );


    private final JavaMailSender mailSender;

    private final UserRepository userRepository;

    private final CurrentTenantService currentTenantService;

    private final String destinationEmail;

    private final String senderEmail;


    public ProblemReportService(
            JavaMailSender mailSender,
            UserRepository userRepository,
            CurrentTenantService currentTenantService,

            @Value("${app.problem-report.destination-email:}")
            String destinationEmail,

            @Value("${spring.mail.username:}")
            String senderEmail
    ) {

        this.mailSender =
                mailSender;

        this.userRepository =
                userRepository;

        this.currentTenantService =
                currentTenantService;

        this.destinationEmail =
                destinationEmail == null
                        ?
                        ""
                        :
                        destinationEmail.trim();

        this.senderEmail =
                senderEmail == null
                        ?
                        ""
                        :
                        senderEmail.trim();
    }


    // =====================================================
    // SEND REPORT
    // =====================================================

    @Transactional(readOnly = true)
    public void sendReport(
            Long currentUserId,
            ProblemReportRequest request
    ) {

        validateMailConfiguration();


        Tenant tenant =
                currentTenantService
                        .getCurrentTenant();


        User user =
                userRepository
                        .findByIdAndTenant_Id(
                                currentUserId,
                                tenant.getId()
                        )
                        .orElseThrow(
                                () ->
                                        new AccessDeniedException(
                                                "El usuario de la sesión no pertenece a la empresa."
                                        )
                        );


        String body =
                buildEmailBody(
                        tenant,
                        user,
                        request
                );


        SimpleMailMessage message =
                new SimpleMailMessage();


        message.setFrom(
                senderEmail
        );


        message.setTo(
                destinationEmail
        );


        message.setSubject(
                "[Sistema de Gestión] Nuevo reporte de problema"
        );


        message.setText(
                body
        );


        mailSender.send(
                message
        );
    }


    // =====================================================
    // EMAIL BODY
    // =====================================================

    private String buildEmailBody(
            Tenant tenant,
            User user,
            ProblemReportRequest request
    ) {

        String requestId =
                normalizeOptional(
                        request.requestId()
                );


        if (
                requestId == null
        ) {

            requestId =
                    "No disponible";
        }


        String currentDate =
                OffsetDateTime
                        .now(
                                ZoneOffset.UTC
                        )
                        .format(
                                DATE_FORMAT
                        );


        return """
                Se recibió un nuevo reporte desde el Sistema de Gestión para Microempresas.

                ==================================================
                EMPRESA
                ==================================================

                ID:
                %s

                Nombre:
                %s


                ==================================================
                USUARIO
                ==================================================

                ID:
                %s

                Nombre:
                %s

                Correo:
                %s

                Rol:
                %s


                ==================================================
                PROBLEMA
                ==================================================

                Página:
                %s

                Descripción:
                %s


                ==================================================
                INFORMACIÓN TÉCNICA
                ==================================================

                Request ID:
                %s

                Fecha:
                %s


                El Request ID puede utilizarse para localizar
                la petición correspondiente en los logs del servidor.
                """
                .formatted(

                        tenant.getId(),

                        tenant.getName(),

                        user.getId(),

                        user.getName(),

                        user.getEmail(),

                        user.getRole(),

                        request.page().trim(),

                        request.description().trim(),

                        requestId,

                        currentDate
                );
    }


    // =====================================================
    // CONFIGURATION
    // =====================================================

    private void validateMailConfiguration() {

        if (
                destinationEmail.isBlank()
        ) {

            throw new IllegalStateException(
                    "El correo de destino para reportes no está configurado."
            );
        }


        if (
                senderEmail.isBlank()
        ) {

            throw new IllegalStateException(
                    "El correo remitente no está configurado."
            );
        }
    }


    // =====================================================
    // OPTIONAL TEXT
    // =====================================================

    private String normalizeOptional(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return null;
        }


        return value.trim();
    }
}