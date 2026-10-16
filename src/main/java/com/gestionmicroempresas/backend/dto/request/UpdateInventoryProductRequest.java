package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateInventoryProductRequest(

        @JsonProperty("descripcion")
        @NotBlank(
                message =
                        "La descripción es obligatoria."
        )
        @Size(
                max = 200,
                message =
                        "La descripción no puede superar 200 caracteres."
        )
        String description,


        @JsonProperty("modelo")
        @Size(
                max = 100,
                message =
                        "El modelo no puede superar 100 caracteres."
        )
        String model,


        @JsonProperty("categoriaId")
        Long categoryId,


        @JsonProperty("marcaId")
        Long brandId,


        @JsonProperty("unidadMedidaId")
        Long unitMeasureId,


        @JsonProperty("proveedorId")
        Long supplierId,


        @JsonProperty("medidaId")
        Long measureId,


        @JsonProperty("almacenId")
        Long warehouseId,


        @JsonProperty("condicionId")
        Long conditionId,


        @JsonProperty("fila")
        @Size(
                max = 50,
                message =
                        "La ubicación/fila es demasiado larga."
        )
        String rowLocation,


        @JsonProperty("precioUnitario")
        @NotNull(
                message =
                        "El precio unitario es obligatorio."
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "El precio unitario no puede ser negativo."
        )
        @Digits(
                integer = 12,
                fraction = 2,
                message =
                        "El precio unitario debe tener máximo 2 decimales."
        )
        BigDecimal unitPrice,


        @JsonProperty("stockMinimo")
        @NotNull(
                message =
                        "El stock mínimo es obligatorio."
        )
        @PositiveOrZero(
                message =
                        "El stock mínimo no puede ser negativo."
        )
        Integer minimumStock

) {
}