package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import java.time.LocalDate;

public record UpdateIncomeRequest(

        @JsonProperty("clienteId")
        @NotNull(
                message =
                        "El cliente es obligatorio."
        )
        Long clientId,


        @JsonProperty("tipoRegistro")
        @NotBlank(
                message =
                        "El tipo de registro es obligatorio."
        )
        @Pattern(
                regexp =
                        "(?i)^(FACTURA|INGRESO)$",
                message =
                        "El tipo de registro debe ser FACTURA o INGRESO."
        )
        String recordType,


        @JsonProperty("concepto")
        @NotBlank(
                message =
                        "El concepto es obligatorio."
        )
        @Size(
                min = 2,
                max = 300,
                message =
                        "El concepto debe tener entre 2 y 300 caracteres."
        )
        String concept,


        @JsonProperty("subtotal")
        @JsonAlias({
                "monto",
                "importe"
        })
        @NotNull(
                message =
                        "El subtotal es obligatorio."
        )
        @DecimalMin(
                value = "0.01",
                message =
                        "El subtotal debe ser mayor a cero."
        )
        @Digits(
                integer = 12,
                fraction = 2,
                message =
                        "El subtotal debe tener máximo 2 decimales."
        )
        BigDecimal subtotal,


        @JsonProperty("aplicaIva")
        @JsonAlias({
                "conIva",
                "incluyeIva"
        })
        boolean taxApplied,


        @JsonProperty("estado")
        @Pattern(
                regexp =
                        "(?i)^(PENDIENTE|PAGADO|CANCELADO)$",
                message =
                        "El estado debe ser PENDIENTE, PAGADO o CANCELADO."
        )
        String status,


        @JsonProperty("fecha")
        @NotNull(
                message =
                        "La fecha es obligatoria."
        )
        LocalDate recordDate,


        @JsonProperty("fechaPago")
        LocalDate paymentDate,


        @JsonProperty("formaPago")
        @Size(
                max = 80,
                message =
                        "La forma de pago no puede exceder 80 caracteres."
        )
        String paymentMethod

) {
}