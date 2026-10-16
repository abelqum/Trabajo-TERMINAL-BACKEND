package com.gestionmicroempresas.backend.dto.response;

import java.util.List;

public record ClientsPageResponse(

        List<ClientResponse> content,

        int number,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last

) {
}