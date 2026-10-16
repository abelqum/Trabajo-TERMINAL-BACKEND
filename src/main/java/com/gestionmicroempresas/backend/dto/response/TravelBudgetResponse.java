package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.Travel;

import java.math.BigDecimal;

public record TravelBudgetResponse(

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
        BigDecimal other,

        BigDecimal total

) {

    public static TravelBudgetResponse from(
            Travel travel
    ) {

        BigDecimal total =
                travel
                        .getFoodBudget()
                        .add(
                                travel.getGasolineBudget()
                        )
                        .add(
                                travel.getTollsBudget()
                        )
                        .add(
                                travel.getLodgingBudget()
                        )
                        .add(
                                travel.getMaterialBudget()
                        )
                        .add(
                                travel.getOtherBudget()
                        );


        return new TravelBudgetResponse(

                travel.getFoodBudget(),

                travel.getGasolineBudget(),

                travel.getTollsBudget(),

                travel.getLodgingBudget(),

                travel.getMaterialBudget(),

                travel.getOtherBudget(),

                total
        );
    }
}