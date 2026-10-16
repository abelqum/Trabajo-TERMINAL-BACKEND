package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import java.util.List;

public record SystemBackupResponse(

        @JsonProperty("formato")
        String format,


        @JsonProperty("version")
        int version,


        @JsonProperty("generadoEn")
        OffsetDateTime generatedAt,


        @JsonProperty("incluyeArchivos")
        boolean includesFiles,


        @JsonProperty("incluyeCredenciales")
        boolean includesCredentials,


        @JsonProperty("advertencias")
        List<String> warnings,


        @JsonProperty("datos")
        BackupData data

) {

    public record BackupData(

            @JsonProperty("usuarios")
            List<UserData> users,


            @JsonProperty("clientes")
            List<ClientData> clients,


            @JsonProperty("ingresos")
            List<IncomeData> incomes,


            @JsonProperty("egresos")
            List<ExpenseData> expenses,


            @JsonProperty("viajes")
            List<TravelData> travels,


            @JsonProperty("gastosViaticos")
            List<TravelExpenseData> travelExpenses,


            @JsonProperty("tareas")
            List<TaskData> tasks,


            @JsonProperty("evidenciasTareas")
            List<TaskEvidenceData> taskEvidence,


            @JsonProperty("catalogosInventario")
            List<InventoryCatalogData> inventoryCatalogs,


            @JsonProperty("productosInventario")
            List<InventoryProductData> inventoryProducts,


            @JsonProperty("movimientosInventario")
            List<InventoryMovementData> inventoryMovements,


            @JsonProperty("componentesKits")
            List<KitComponentData> kitComponents

    ) {
    }


    public record UserData(

            Long id,


            @JsonProperty("nombre")
            String name,


            String email,


            @JsonProperty("rol")
            String role,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt,


            @JsonProperty("actualizadoEn")
            OffsetDateTime updatedAt

    ) {
    }


    public record ClientData(

            Long id,


            @JsonProperty("razonSocial")
            String businessName,


            String rfc,


            @JsonProperty("telefonos")
            List<String> phones,


            @JsonProperty("correos")
            List<String> emails,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt,


            @JsonProperty("actualizadoEn")
            OffsetDateTime updatedAt

    ) {
    }


    public record IncomeData(

            Long id,


            @JsonProperty("clienteId")
            Long clientId,


            @JsonProperty("tipoRegistro")
            String recordType,


            @JsonProperty("concepto")
            String concept,


            BigDecimal subtotal,


            @JsonProperty("aplicaIva")
            boolean taxApplied,


            @JsonProperty("iva")
            BigDecimal taxAmount,


            BigDecimal total,


            @JsonProperty("estado")
            String status,


            @JsonProperty("fecha")
            LocalDate recordDate,


            @JsonProperty("fechaPago")
            LocalDate paymentDate,


            @JsonProperty("formaPago")
            String paymentMethod,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt,


            @JsonProperty("actualizadoEn")
            OffsetDateTime updatedAt

    ) {
    }


    public record ExpenseData(

            Long id,


            @JsonProperty("concepto")
            String concept,


            @JsonProperty("categoria")
            String category,


            @JsonProperty("monto")
            BigDecimal amount,


            @JsonProperty("fecha")
            LocalDate expenseDate,


            @JsonProperty("descripcion")
            String description,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt,


            @JsonProperty("actualizadoEn")
            OffsetDateTime updatedAt

    ) {
    }


    public record TravelBudgetData(

            @JsonProperty("alimentos")
            BigDecimal food,


            @JsonProperty("gasolina")
            BigDecimal gasoline,


            @JsonProperty("casetas")
            BigDecimal tolls,


            @JsonProperty("hospedaje")
            BigDecimal lodging,


            @JsonProperty("material")
            BigDecimal material,


            @JsonProperty("otros")
            BigDecimal other

    ) {
    }


    public record TravelData(

            Long id,


            @JsonProperty("nombre")
            String name,


            @JsonProperty("destino")
            String destination,


            @JsonProperty("fechaInicio")
            LocalDate startDate,


            @JsonProperty("fechaFin")
            LocalDate endDate,


            @JsonProperty("estatus")
            String status,


            @JsonProperty("presupuesto")
            TravelBudgetData budget,


            @JsonProperty("usuariosIds")
            List<Long> assignedUserIds,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt,


            @JsonProperty("actualizadoEn")
            OffsetDateTime updatedAt

    ) {
    }


    public record TravelExpenseData(

            Long id,


            @JsonProperty("viajeId")
            Long travelId,


            @JsonProperty("categoria")
            String category,


            @JsonProperty("descripcion")
            String description,


            @JsonProperty("monto")
            BigDecimal amount,


            @JsonProperty("fecha")
            LocalDate expenseDate,


            @JsonProperty("estatus")
            String status,


            @JsonProperty("motivoRechazo")
            String rejectionReason,


            @JsonProperty("comprobanteArchivo")
            String receiptFileName,


            @JsonProperty("creadoPorUsuarioId")
            Long createdByUserId,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt,


            @JsonProperty("actualizadoEn")
            OffsetDateTime updatedAt

    ) {
    }


    public record TaskData(

            Long id,


            @JsonProperty("titulo")
            String title,


            @JsonProperty("descripcion")
            String description,


            @JsonProperty("fechaLimite")
            OffsetDateTime dueAt,


            @JsonProperty("prioridad")
            String priority,


            @JsonProperty("estado")
            String status,


            @JsonProperty("creadorUsuarioId")
            Long creatorUserId,


            @JsonProperty("asignadosIds")
            List<Long> assigneeIds,


            @JsonProperty("retroalimentacion")
            String feedback,


            @JsonProperty("fechaRevision")
            OffsetDateTime reviewedAt,


            @JsonProperty("fechaCompletada")
            OffsetDateTime completedAt,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt,


            @JsonProperty("actualizadoEn")
            OffsetDateTime updatedAt

    ) {
    }


    public record TaskEvidenceData(

            Long id,


            @JsonProperty("tareaId")
            Long taskId,


            @JsonProperty("autorUsuarioId")
            Long authorUserId,


            @JsonProperty("archivo")
            String fileName,


            @JsonProperty("nombreOriginal")
            String originalName,


            @JsonProperty("mimeType")
            String mimeType,


            @JsonProperty("tamanoBytes")
            long sizeBytes,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt

    ) {
    }


    public record InventoryCatalogData(

            Long id,


            @JsonProperty("tipo")
            String type,


            @JsonProperty("nombre")
            String name,


            @JsonProperty("enlace")
            String link,


            @JsonProperty("permiteActualizarPrecio")
            boolean allowPriceUpdate,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt,


            @JsonProperty("actualizadoEn")
            OffsetDateTime updatedAt

    ) {
    }


    public record InventoryProductData(

            Long id,


            @JsonProperty("descripcion")
            String description,


            @JsonProperty("modelo")
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
            String rowLocation,


            @JsonProperty("precioUnitario")
            BigDecimal unitPrice,


            @JsonProperty("cantidad")
            int quantity,


            @JsonProperty("stockMinimo")
            int minimumStock,


            @JsonProperty("esKit")
            boolean kit,


            @JsonProperty("fotoArchivo")
            String photoFileName,


            @JsonProperty("codigoQr")
            String qrCode,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt,


            @JsonProperty("actualizadoEn")
            OffsetDateTime updatedAt

    ) {
    }


    public record InventoryMovementData(

            Long id,


            @JsonProperty("productoId")
            Long productId,


            @JsonProperty("usuarioId")
            Long userId,


            @JsonProperty("tipo")
            String movementType,


            @JsonProperty("cambioCantidad")
            int quantityChange,


            @JsonProperty("cantidadResultante")
            int resultingQuantity,


            @JsonProperty("motivo")
            String reason,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt

    ) {
    }


    public record KitComponentData(

            Long id,


            @JsonProperty("kitProductoId")
            Long kitProductId,


            @JsonProperty("componenteProductoId")
            Long componentProductId,


            @JsonProperty("cantidadNecesaria")
            int requiredQuantity,


            @JsonProperty("creadoEn")
            OffsetDateTime createdAt

    ) {
    }
}