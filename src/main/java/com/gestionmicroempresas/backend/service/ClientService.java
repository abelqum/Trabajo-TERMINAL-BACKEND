package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.CreateClientRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateClientRequest;

import com.gestionmicroempresas.backend.dto.response.ClientOptionResponse;
import com.gestionmicroempresas.backend.dto.response.ClientResponse;
import com.gestionmicroempresas.backend.dto.response.ClientsPageResponse;

import com.gestionmicroempresas.backend.entity.Client;

import com.gestionmicroempresas.backend.exception.ConflictException;
import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.repository.ClientRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    private final CurrentTenantService currentTenantService;


    public ClientService(
            ClientRepository clientRepository,
            CurrentTenantService currentTenantService
    ) {

        this.clientRepository =
                clientRepository;

        this.currentTenantService =
                currentTenantService;
    }


    // =====================================================
    // GET CLIENTS
    // =====================================================

    @Transactional(readOnly = true)
    public ClientsPageResponse getClients(
            int page,
            int size,
            String search,
            String sort
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        int safePage =
                Math.max(
                        page,
                        0
                );


        int safeSize =
                Math.min(
                        Math.max(
                                size,
                                1
                        ),
                        100
                );


        String normalizedSearch =
                normalizeSearch(
                        search
                );


        PageRequest pageable =
                PageRequest.of(
                        safePage,
                        safeSize,
                        resolveSort(sort)
                );


        Specification<Client> specification =
                (
                        root,
                        query,
                        criteriaBuilder
                ) ->

                        criteriaBuilder.equal(

                                root
                                        .get("tenant")
                                        .get("id"),

                                tenantId
                        );


        if (normalizedSearch != null) {

            String searchPattern =
                    "%"
                            +
                            normalizedSearch
                                    .toLowerCase(
                                            Locale.ROOT
                                    )
                            +
                            "%";


            specification =
                    specification.and(

                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.or(

                                            criteriaBuilder.like(

                                                    criteriaBuilder.lower(
                                                            root.get(
                                                                    "businessName"
                                                            )
                                                    ),

                                                    searchPattern
                                            ),

                                            criteriaBuilder.like(

                                                    criteriaBuilder.lower(
                                                            root.get(
                                                                    "rfc"
                                                            )
                                                    ),

                                                    searchPattern
                                            )
                                    )
                    );
        }


        Page<Client> result =
                clientRepository.findAll(
                        specification,
                        pageable
                );


        List<ClientResponse> clients =
                result
                        .getContent()
                        .stream()
                        .map(
                                ClientResponse::from
                        )
                        .toList();


        return new ClientsPageResponse(

                clients,

                result.getNumber(),

                result.getSize(),

                result.getTotalElements(),

                result.getTotalPages(),

                result.isFirst(),

                result.isLast()
        );
    }


    // =====================================================
    // CREATE CLIENT
    // =====================================================

    @Transactional
    public ClientResponse createClient(
            CreateClientRequest request
    ) {

        String normalizedRfc =
                normalizeRfc(
                        request.rfc()
                );


        validateRfcAvailability(
                normalizedRfc,
                null
        );


        Client client =
                new Client(

                        currentTenantService
                                .getCurrentTenant(),

                        request
                                .businessName()
                                .trim(),

                        normalizedRfc
                );


        client.replacePhones(
                normalizePhones(
                        request.phones()
                )
        );


        client.replaceEmails(
                normalizeEmails(
                        request.emails()
                )
        );


        Client savedClient =
                clientRepository.saveAndFlush(
                        client
                );


        return ClientResponse.from(
                savedClient
        );
    }


    // =====================================================
    // UPDATE CLIENT
    // =====================================================

    @Transactional
    public ClientResponse updateClient(
            Long clientId,
            UpdateClientRequest request
    ) {

        Client client =
                getClientById(
                        clientId
                );


        String normalizedRfc =
                normalizeRfc(
                        request.rfc()
                );


        validateRfcAvailability(
                normalizedRfc,
                clientId
        );


        client.setBusinessName(
                request
                        .businessName()
                        .trim()
        );


        client.setRfc(
                normalizedRfc
        );


        client.replacePhones(
                normalizePhones(
                        request.phones()
                )
        );


        client.replaceEmails(
                normalizeEmails(
                        request.emails()
                )
        );


        Client updatedClient =
                clientRepository.saveAndFlush(
                        client
                );


        return ClientResponse.from(
                updatedClient
        );
    }


    // =====================================================
    // DELETE CLIENT
    // =====================================================

    @Transactional
    public void deleteClient(
            Long clientId
    ) {

        Client client =
                getClientById(
                        clientId
                );


        clientRepository.delete(
                client
        );


        clientRepository.flush();
    }


    // =====================================================
    // OPTIONS
    // =====================================================

    @Transactional(readOnly = true)
    public List<ClientOptionResponse> getOptions() {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        return clientRepository
                .findAllByTenant_IdOrderByBusinessNameAsc(
                        tenantId
                )
                .stream()
                .map(
                        ClientOptionResponse::from
                )
                .toList();
    }


    // =====================================================
    // GET BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public Client getClientById(
            Long clientId
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        return clientRepository
                .findByIdAndTenant_Id(
                        clientId,
                        tenantId
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El cliente solicitado no existe."
                                )
                );
    }


    // =====================================================
    // RFC
    // =====================================================

    private void validateRfcAvailability(
            String rfc,
            Long currentClientId
    ) {

        if (rfc == null) {
            return;
        }


        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        boolean exists;


        if (currentClientId == null) {

            exists =
                    clientRepository
                            .existsByTenant_IdAndRfcIgnoreCase(
                                    tenantId,
                                    rfc
                            );

        } else {

            exists =
                    clientRepository
                            .existsByTenant_IdAndRfcIgnoreCaseAndIdNot(
                                    tenantId,
                                    rfc,
                                    currentClientId
                            );
        }


        if (exists) {

            throw new ConflictException(
                    "Ya existe un cliente registrado con ese RFC."
            );
        }
    }


    // =====================================================
    // NORMALIZATION
    // =====================================================

    private String normalizeSearch(
            String search
    ) {

        if (
                search == null
                        ||
                        search.isBlank()
        ) {

            return null;
        }


        return search.trim();
    }


    private String normalizeRfc(
            String rfc
    ) {

        if (
                rfc == null
                        ||
                        rfc.isBlank()
        ) {

            return null;
        }


        return rfc
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }


    private Set<String> normalizePhones(
            List<String> phones
    ) {

        LinkedHashSet<String> normalized =
                new LinkedHashSet<>();


        if (phones == null) {
            return normalized;
        }


        for (String phone : phones) {

            if (
                    phone == null
                            ||
                            phone.isBlank()
            ) {

                continue;
            }


            normalized.add(
                    phone.trim()
            );
        }


        return normalized;
    }


    private Set<String> normalizeEmails(
            List<String> emails
    ) {

        LinkedHashSet<String> normalized =
                new LinkedHashSet<>();


        if (emails == null) {
            return normalized;
        }


        for (String email : emails) {

            if (
                    email == null
                            ||
                            email.isBlank()
            ) {

                continue;
            }


            normalized.add(

                    email
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            )
            );
        }


        return normalized;
    }


    // =====================================================
    // SORT
    // =====================================================

    private Sort resolveSort(
            String sort
    ) {

        if (
                sort == null
                        ||
                        sort.isBlank()
        ) {

            return Sort
                    .by("businessName")
                    .ascending();
        }


        String[] parts =
                sort.split(",");


        String property =
                switch (
                        parts[0].trim()
                        ) {

                    case "rfc" ->
                            "rfc";

                    case "razonSocial" ->
                            "businessName";

                    default ->
                            "businessName";
                };


        Sort.Direction direction =
                Sort.Direction.ASC;


        if (
                parts.length > 1
                        &&
                        "desc".equalsIgnoreCase(
                                parts[1].trim()
                        )
        ) {

            direction =
                    Sort.Direction.DESC;
        }


        return Sort.by(
                direction,
                property
        );
    }
}