package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.CreateClientRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateClientRequest;

import com.gestionmicroempresas.backend.dto.response.ClientOptionResponse;
import com.gestionmicroempresas.backend.dto.response.ClientResponse;
import com.gestionmicroempresas.backend.dto.response.ClientsPageResponse;

import com.gestionmicroempresas.backend.service.ClientService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClientController {

    private final ClientService clientService;


    public ClientController(
            ClientService clientService
    ) {

        this.clientService =
                clientService;
    }


    // =====================================================
    // GET CLIENTS
    // =====================================================

    @GetMapping
    public ResponseEntity<ClientsPageResponse> getClients(

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size,

            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    defaultValue = "razonSocial,asc"
            )
            String sort
    ) {

        return ResponseEntity.ok(

                clientService.getClients(
                        page,
                        size,
                        search,
                        sort
                )
        );
    }


    // =====================================================
    // OPTIONS
    // =====================================================

    @GetMapping("/opciones")
    public ResponseEntity<List<ClientOptionResponse>> getOptions() {

        return ResponseEntity.ok(
                clientService.getOptions()
        );
    }


    // =====================================================
    // CREATE CLIENT
    // =====================================================

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(

            @Valid
            @RequestBody
            CreateClientRequest request
    ) {

        ClientResponse client =
                clientService.createClient(
                        request
                );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        client
                );
    }


    // =====================================================
    // UPDATE CLIENT
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateClientRequest request
    ) {

        return ResponseEntity.ok(

                clientService.updateClient(
                        id,
                        request
                )
        );
    }


    // =====================================================
    // DELETE CLIENT
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(

            @PathVariable
            Long id
    ) {

        clientService.deleteClient(
                id
        );


        return ResponseEntity
                .noContent()
                .build();
    }
}