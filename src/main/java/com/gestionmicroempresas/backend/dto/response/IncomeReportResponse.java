package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

import java.util.List;

public record IncomeReportResponse(

        @JsonProperty("registros")
        List<IncomeResponse> records,

        @JsonProperty("resumen")
        IncomeSummaryResponse summary,

        @JsonProperty("generadoEn")
        OffsetDateTime generatedAt

) {
}