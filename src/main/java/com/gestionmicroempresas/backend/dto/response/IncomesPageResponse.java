package com.gestionmicroempresas.backend.dto.response;

import java.util.List;

public record IncomesPageResponse(

        List<IncomeResponse> content,

        int number,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last

) {
}