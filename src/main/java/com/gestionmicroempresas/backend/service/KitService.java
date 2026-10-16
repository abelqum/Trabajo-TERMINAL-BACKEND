package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.KitComponentRequest;
import com.gestionmicroempresas.backend.dto.request.KitOperationRequest;
import com.gestionmicroempresas.backend.dto.request.KitRequest;

import com.gestionmicroempresas.backend.dto.response.CatalogItemResponse;
import com.gestionmicroempresas.backend.dto.response.KitAvailableProductResponse;
import com.gestionmicroempresas.backend.dto.response.KitComponentResponse;
import com.gestionmicroempresas.backend.dto.response.KitResponse;

import com.gestionmicroempresas.backend.entity.InventoryCatalogItem;
import com.gestionmicroempresas.backend.entity.InventoryKitComponent;
import com.gestionmicroempresas.backend.entity.InventoryProduct;
import com.gestionmicroempresas.backend.entity.InventoryStockMovement;
import com.gestionmicroempresas.backend.entity.Tenant;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.InventoryCatalogType;
import com.gestionmicroempresas.backend.enums.InventoryMovementType;

import com.gestionmicroempresas.backend.exception.ConflictException;
import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.repository.InventoryCatalogRepository;
import com.gestionmicroempresas.backend.repository.InventoryKitComponentRepository;
import com.gestionmicroempresas.backend.repository.InventoryProductRepository;
import com.gestionmicroempresas.backend.repository.InventoryStockMovementRepository;
import com.gestionmicroempresas.backend.repository.UserRepository;

import com.gestionmicroempresas.backend.storage.InventoryPhotoStorageService;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class KitService {

    private final InventoryProductRepository productRepository;

    private final InventoryKitComponentRepository componentRepository;

    private final InventoryStockMovementRepository movementRepository;

    private final InventoryCatalogRepository catalogRepository;

    private final UserRepository userRepository;

    private final InventoryPhotoStorageService storageService;

    private final CurrentTenantService currentTenantService;


    public KitService(
            InventoryProductRepository productRepository,
            InventoryKitComponentRepository componentRepository,
            InventoryStockMovementRepository movementRepository,
            InventoryCatalogRepository catalogRepository,
            UserRepository userRepository,
            InventoryPhotoStorageService storageService,
            CurrentTenantService currentTenantService
    ) {

        this.productRepository = productRepository;
        this.componentRepository = componentRepository;
        this.movementRepository = movementRepository;
        this.catalogRepository = catalogRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.currentTenantService = currentTenantService;
    }


    // =====================================================
    // GET KITS
    // =====================================================

    @Transactional(readOnly = true)
    public List<KitResponse> getKits() {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        return productRepository
                .findAllByTenant_IdAndKitTrueOrderByDescriptionAsc(
                        tenantId
                )
                .stream()
                .map(
                        this::buildKitResponse
                )
                .toList();
    }


    // =====================================================
    // AVAILABLE PRODUCTS
    // =====================================================

    @Transactional(readOnly = true)
    public List<KitAvailableProductResponse> getAvailableProducts() {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        return productRepository
                .findAllByTenant_IdAndKitFalseOrderByDescriptionAsc(
                        tenantId
                )
                .stream()
                .map(
                        KitAvailableProductResponse::from
                )
                .toList();
    }


    // =====================================================
    // CREATE
    // =====================================================

    @Transactional
    public KitResponse createKit(
            KitRequest request,
            MultipartFile photo
    ) {

        Tenant tenant =
                currentTenantService
                        .getCurrentTenant();


        InventoryCatalogItem category =
                resolveCatalogItem(
                        request.categoryId(),
                        InventoryCatalogType.CATEGORY,
                        "La categoría seleccionada no existe."
                );


        InventoryCatalogItem warehouse =
                resolveCatalogItem(
                        request.warehouseId(),
                        InventoryCatalogType.WAREHOUSE,
                        "El almacén seleccionado no existe."
                );


        List<PreparedComponent> components =
                prepareComponents(
                        request.components()
                );


        String photoFileName = null;


        try {

            if (
                    photo != null
                            &&
                            !photo.isEmpty()
            ) {

                photoFileName =
                        storageService.storePhoto(
                                photo
                        );
            }


            InventoryProduct kit =
                    new InventoryProduct(
                            tenant,

                            request
                                    .description()
                                    .trim(),

                            null,

                            category,

                            null,

                            null,

                            null,

                            null,

                            warehouse,

                            null,

                            normalizeText(
                                    request.rowLocation()
                            ),

                            money(
                                    request.unitPrice()
                            ),

                            0,

                            0
                    );


            kit.setKit(
                    true
            );


            kit.setPhotoFileName(
                    photoFileName
            );


            InventoryProduct savedKit =
                    productRepository.saveAndFlush(
                            kit
                    );


            savedKit.setQrCode(
                    "INV:"
                            +
                            savedKit.getId()
            );


            savedKit =
                    productRepository.saveAndFlush(
                            savedKit
                    );


            componentRepository.saveAll(

                    createComponentEntities(
                            savedKit,
                            components
                    )
            );


            componentRepository.flush();


            return buildKitResponse(
                    savedKit
            );

        } catch (RuntimeException exception) {

            deletePhotoQuietly(
                    photoFileName
            );

            throw exception;
        }
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @Transactional
    public KitResponse updateKit(
            Long kitId,
            KitRequest request,
            MultipartFile photo
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        InventoryProduct kit =
                getKit(
                        kitId
                );


        InventoryCatalogItem category =
                resolveCatalogItem(
                        request.categoryId(),
                        InventoryCatalogType.CATEGORY,
                        "La categoría seleccionada no existe."
                );


        InventoryCatalogItem warehouse =
                resolveCatalogItem(
                        request.warehouseId(),
                        InventoryCatalogType.WAREHOUSE,
                        "El almacén seleccionado no existe."
                );


        List<PreparedComponent> requestedComponents =
                prepareComponents(
                        request.components()
                );


        List<InventoryKitComponent> currentComponents =
                componentRepository
                        .findAllByTenant_IdAndKit_IdOrderByIdAsc(
                                tenantId,
                                kitId
                        );


        boolean componentDefinitionChanged =
                !sameComponentDefinition(
                        currentComponents,
                        requestedComponents
                );


        if (
                componentDefinitionChanged
                        &&
                        kit.getQuantity() > 0
        ) {

            throw new ConflictException(
                    "No puedes modificar los componentes mientras existan kits armados. Desarma primero todas las existencias del kit."
            );
        }


        String newPhotoFileName = null;


        try {

            if (
                    photo != null
                            &&
                            !photo.isEmpty()
            ) {

                newPhotoFileName =
                        storageService.storePhoto(
                                photo
                        );
            }


            String previousPhoto =
                    kit.getPhotoFileName();


            kit.setDescription(
                    request
                            .description()
                            .trim()
            );


            kit.setCategory(
                    category
            );


            kit.setWarehouse(
                    warehouse
            );


            kit.setRowLocation(
                    normalizeText(
                            request.rowLocation()
                    )
            );


            kit.setUnitPrice(
                    money(
                            request.unitPrice()
                    )
            );


            if (newPhotoFileName != null) {

                kit.setPhotoFileName(
                        newPhotoFileName
                );
            }


            InventoryProduct updatedKit =
                    productRepository.saveAndFlush(
                            kit
                    );


            if (componentDefinitionChanged) {

                componentRepository
                        .deleteAllByTenant_IdAndKit_Id(
                                tenantId,
                                kitId
                        );


                componentRepository.flush();


                componentRepository.saveAll(

                        createComponentEntities(
                                updatedKit,
                                requestedComponents
                        )
                );


                componentRepository.flush();
            }


            if (
                    newPhotoFileName != null
                            &&
                            previousPhoto != null
                            &&
                            !previousPhoto.isBlank()
            ) {

                deletePhotoQuietly(
                        previousPhoto
                );
            }


            return buildKitResponse(
                    updatedKit
            );

        } catch (RuntimeException exception) {

            deletePhotoQuietly(
                    newPhotoFileName
            );

            throw exception;
        }
    }


    // =====================================================
    // DELETE
    // =====================================================

    @Transactional
    public void deleteKit(
            Long kitId
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        InventoryProduct kit =
                getKit(
                        kitId
                );


        if (
                kit.getQuantity()
                        >
                        0
        ) {

            throw new ConflictException(
                    "No puedes eliminar un kit mientras tenga existencias armadas."
            );
        }


        if (
                movementRepository
                        .existsByTenant_IdAndProduct_Id(
                                tenantId,
                                kitId
                        )
        ) {

            throw new ConflictException(
                    "No puedes eliminar el kit porque tiene historial de movimientos."
            );
        }


        String photoFileName =
                kit.getPhotoFileName();


        productRepository.delete(
                kit
        );


        productRepository.flush();


        deletePhotoQuietly(
                photoFileName
        );
    }


    // =====================================================
    // ASSEMBLE
    // =====================================================

    @Transactional
    public void assembleKit(
            Long currentUserId,
            Long kitId,
            KitOperationRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Long tenantId =
                currentUser.getTenantId();


        InventoryProduct kit =
                getKitForUpdate(
                        tenantId,
                        kitId
                );


        List<InventoryKitComponent> bom =
                getKitComponents(
                        tenantId,
                        kitId
                );


        List<Long> componentIds =
                bom
                        .stream()
                        .map(
                                component ->
                                        component
                                                .getComponent()
                                                .getId()
                        )
                        .distinct()
                        .sorted()
                        .toList();


        List<InventoryProduct> lockedProducts =
                productRepository
                        .findAllByTenantAndIdsForUpdate(
                                tenantId,
                                componentIds
                        );


        if (
                lockedProducts.size()
                        !=
                        componentIds.size()
        ) {

            throw new ConflictException(
                    "Uno de los componentes del kit ya no está disponible."
            );
        }


        Map<Long, InventoryProduct> productsById =
                indexProducts(
                        lockedProducts
                );


        int kitQuantity =
                request.quantity();


        for (
                InventoryKitComponent component :
                bom
        ) {

            InventoryProduct product =
                    productsById.get(

                            component
                                    .getComponent()
                                    .getId()
                    );


            int required =
                    multiplyExact(
                            component.getRequiredQuantity(),
                            kitQuantity
                    );


            if (
                    product.getQuantity()
                            <
                            required
            ) {

                throw new ConflictException(
                        "Stock insuficiente del componente \""
                                +
                                product.getDescription()
                                +
                                "\". Se requieren "
                                +
                                required
                                +
                                " y hay "
                                +
                                product.getQuantity()
                                +
                                "."
                );
            }
        }


        List<InventoryStockMovement> movements =
                new ArrayList<>();


        for (
                InventoryKitComponent component :
                bom
        ) {

            InventoryProduct product =
                    productsById.get(

                            component
                                    .getComponent()
                                    .getId()
                    );


            int required =
                    multiplyExact(
                            component.getRequiredQuantity(),
                            kitQuantity
                    );


            int resultingQuantity =
                    product.getQuantity()
                            -
                            required;


            product.setQuantity(
                    resultingQuantity
            );


            movements.add(

                    new InventoryStockMovement(
                            product,
                            currentUser,
                            InventoryMovementType.KIT_ASSEMBLY,
                            -required,
                            resultingQuantity,
                            "Componente utilizado para armar "
                                    +
                                    kitQuantity
                                    +
                                    " unidad(es) del kit "
                                    +
                                    kit.getDescription()
                    )
            );
        }


        int resultingKitQuantity;


        try {

            resultingKitQuantity =
                    Math.addExact(
                            kit.getQuantity(),
                            kitQuantity
                    );

        } catch (
                ArithmeticException exception
        ) {

            throw new ConflictException(
                    "La cantidad resultante del kit es demasiado grande."
            );
        }


        kit.setQuantity(
                resultingKitQuantity
        );


        movements.add(

                new InventoryStockMovement(
                        kit,
                        currentUser,
                        InventoryMovementType.KIT_ASSEMBLY,
                        kitQuantity,
                        resultingKitQuantity,
                        "Armado de kit"
                )
        );


        productRepository.saveAll(
                lockedProducts
        );

        productRepository.save(
                kit
        );

        movementRepository.saveAll(
                movements
        );


        productRepository.flush();

        movementRepository.flush();
    }


    // =====================================================
    // DISASSEMBLE
    // =====================================================

    @Transactional
    public void disassembleKit(
            Long currentUserId,
            Long kitId,
            KitOperationRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Long tenantId =
                currentUser.getTenantId();


        InventoryProduct kit =
                getKitForUpdate(
                        tenantId,
                        kitId
                );


        int kitQuantity =
                request.quantity();


        if (
                kit.getQuantity()
                        <
                        kitQuantity
        ) {

            throw new ConflictException(
                    "No hay suficientes kits armados para realizar el desarmado."
            );
        }


        List<InventoryKitComponent> bom =
                getKitComponents(
                        tenantId,
                        kitId
                );


        List<Long> componentIds =
                bom
                        .stream()
                        .map(
                                component ->
                                        component
                                                .getComponent()
                                                .getId()
                        )
                        .distinct()
                        .sorted()
                        .toList();


        List<InventoryProduct> lockedProducts =
                productRepository
                        .findAllByTenantAndIdsForUpdate(
                                tenantId,
                                componentIds
                        );


        if (
                lockedProducts.size()
                        !=
                        componentIds.size()
        ) {

            throw new ConflictException(
                    "Uno de los componentes del kit ya no está disponible."
            );
        }


        Map<Long, InventoryProduct> productsById =
                indexProducts(
                        lockedProducts
                );


        List<InventoryStockMovement> movements =
                new ArrayList<>();


        int resultingKitQuantity =
                kit.getQuantity()
                        -
                        kitQuantity;


        kit.setQuantity(
                resultingKitQuantity
        );


        movements.add(

                new InventoryStockMovement(
                        kit,
                        currentUser,
                        InventoryMovementType.KIT_DISASSEMBLY,
                        -kitQuantity,
                        resultingKitQuantity,
                        "Desarmado de kit"
                )
        );


        for (
                InventoryKitComponent component :
                bom
        ) {

            InventoryProduct product =
                    productsById.get(

                            component
                                    .getComponent()
                                    .getId()
                    );


            int returnedQuantity =
                    multiplyExact(
                            component.getRequiredQuantity(),
                            kitQuantity
                    );


            int resultingQuantity;


            try {

                resultingQuantity =
                        Math.addExact(
                                product.getQuantity(),
                                returnedQuantity
                        );

            } catch (
                    ArithmeticException exception
            ) {

                throw new ConflictException(
                        "La cantidad resultante del componente es demasiado grande."
                );
            }


            product.setQuantity(
                    resultingQuantity
            );


            movements.add(

                    new InventoryStockMovement(
                            product,
                            currentUser,
                            InventoryMovementType.KIT_DISASSEMBLY,
                            returnedQuantity,
                            resultingQuantity,
                            "Componente recuperado al desarmar "
                                    +
                                    kitQuantity
                                    +
                                    " unidad(es) del kit "
                                    +
                                    kit.getDescription()
                    )
            );
        }


        productRepository.saveAll(
                lockedProducts
        );

        productRepository.save(
                kit
        );

        movementRepository.saveAll(
                movements
        );


        productRepository.flush();

        movementRepository.flush();
    }


    // =====================================================
    // COMPONENT PREPARATION
    // =====================================================

    private List<PreparedComponent> prepareComponents(
            List<KitComponentRequest> requests
    ) {

        if (
                requests == null
                        ||
                        requests.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Agrega al menos un componente."
            );
        }


        Map<Long, Integer> requestedQuantities =
                new LinkedHashMap<>();


        for (
                KitComponentRequest request :
                requests
        ) {

            if (
                    requestedQuantities.containsKey(
                            request.productId()
                    )
            ) {

                throw new ConflictException(
                        "Un producto no puede repetirse dentro del mismo kit."
                );
            }


            requestedQuantities.put(
                    request.productId(),

                    normalizeRequiredQuantity(
                            request.requiredQuantity()
                    )
            );
        }


        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        List<InventoryProduct> products =
                productRepository
                        .findAllByTenant_IdAndIdIn(
                                tenantId,
                                requestedQuantities.keySet()
                        );


        if (
                products.size()
                        !=
                        requestedQuantities.size()
        ) {

            throw new ResourceNotFoundException(
                    "Uno o más productos seleccionados como componentes no existen."
            );
        }


        Map<Long, InventoryProduct> productsById =
                new HashMap<>();


        for (InventoryProduct product : products) {

            if (product.isKit()) {

                throw new ConflictException(
                        "Un kit no puede utilizarse como componente de otro kit."
                );
            }


            productsById.put(
                    product.getId(),
                    product
            );
        }


        List<PreparedComponent> result =
                new ArrayList<>();


        for (
                Map.Entry<Long, Integer> entry :
                requestedQuantities.entrySet()
        ) {

            result.add(

                    new PreparedComponent(
                            productsById.get(
                                    entry.getKey()
                            ),
                            entry.getValue()
                    )
            );
        }


        return result;
    }


    private int normalizeRequiredQuantity(
            BigDecimal value
    ) {

        if (
                value == null
                        ||
                        value.compareTo(
                                BigDecimal.ZERO
                        )
                                <=
                                0
        ) {

            throw new IllegalArgumentException(
                    "Las cantidades de los componentes deben ser mayores que cero."
            );
        }


        BigDecimal normalized =
                value.stripTrailingZeros();


        if (
                normalized.scale()
                        >
                        0
        ) {

            throw new IllegalArgumentException(
                    "La cantidad requerida de cada componente debe ser un número entero mayor que cero."
            );
        }


        try {

            return normalized.intValueExact();

        } catch (
                ArithmeticException exception
        ) {

            throw new IllegalArgumentException(
                    "La cantidad requerida de un componente es demasiado grande."
            );
        }
    }


    private List<InventoryKitComponent> createComponentEntities(
            InventoryProduct kit,
            List<PreparedComponent> components
    ) {

        return components
                .stream()
                .map(
                        component ->

                                new InventoryKitComponent(
                                        kit,
                                        component.product(),
                                        component.requiredQuantity()
                                )
                )
                .toList();
    }


    private boolean sameComponentDefinition(
            List<InventoryKitComponent> current,
            List<PreparedComponent> requested
    ) {

        if (
                current.size()
                        !=
                        requested.size()
        ) {

            return false;
        }


        Map<Long, Integer> currentMap =
                new HashMap<>();


        for (
                InventoryKitComponent component :
                current
        ) {

            currentMap.put(
                    component
                            .getComponent()
                            .getId(),

                    component.getRequiredQuantity()
            );
        }


        for (
                PreparedComponent component :
                requested
        ) {

            Integer currentQuantity =
                    currentMap.get(
                            component
                                    .product()
                                    .getId()
                    );


            if (
                    currentQuantity == null
                            ||
                            currentQuantity
                                    !=
                                    component.requiredQuantity()
            ) {

                return false;
            }
        }


        return true;
    }


    // =====================================================
    // RESPONSE
    // =====================================================

    private KitResponse buildKitResponse(
            InventoryProduct kit
    ) {

        List<KitComponentResponse> components =
                componentRepository
                        .findAllByTenant_IdAndKit_IdOrderByIdAsc(
                                kit.getTenantId(),
                                kit.getId()
                        )
                        .stream()
                        .map(
                                component ->

                                        new KitComponentResponse(

                                                component
                                                        .getComponent()
                                                        .getId(),

                                                KitAvailableProductResponse.from(
                                                        component.getComponent()
                                                ),

                                                component.getRequiredQuantity()
                                        )
                        )
                        .toList();


        return new KitResponse(
                kit.getId(),
                kit.getDescription(),
                kit.getQuantity(),
                kit.getUnitPrice(),
                kit.getRowLocation(),

                storageService.getPublicUrl(
                        kit.getPhotoFileName()
                ),

                catalog(
                        kit.getCategory()
                ),

                catalog(
                        kit.getWarehouse()
                ),

                components
        );
    }


    private CatalogItemResponse catalog(
            InventoryCatalogItem item
    ) {

        if (item == null) {
            return null;
        }


        return CatalogItemResponse.from(
                item
        );
    }


    // =====================================================
    // KIT ENTITIES
    // =====================================================

    private InventoryProduct getKit(
            Long kitId
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        InventoryProduct product =
                productRepository
                        .findByIdAndTenant_Id(
                                kitId,
                                tenantId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "El kit solicitado no existe."
                                        )
                        );


        validateKit(
                product
        );


        return product;
    }


    private InventoryProduct getKitForUpdate(
            Long tenantId,
            Long kitId
    ) {

        InventoryProduct product =
                productRepository
                        .findByTenantAndIdForUpdate(
                                tenantId,
                                kitId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "El kit solicitado no existe."
                                        )
                        );


        validateKit(
                product
        );


        return product;
    }


    private List<InventoryKitComponent> getKitComponents(
            Long tenantId,
            Long kitId
    ) {

        List<InventoryKitComponent> components =
                componentRepository
                        .findAllByTenant_IdAndKit_IdOrderByIdAsc(
                                tenantId,
                                kitId
                        );


        if (components.isEmpty()) {

            throw new ConflictException(
                    "El kit no tiene componentes configurados."
            );
        }


        return components;
    }


    private void validateKit(
            InventoryProduct product
    ) {

        if (!product.isKit()) {

            throw new ConflictException(
                    "El producto seleccionado no es un kit."
            );
        }
    }


    // =====================================================
    // CATALOG
    // =====================================================

    private InventoryCatalogItem resolveCatalogItem(
            Long id,
            InventoryCatalogType type,
            String message
    ) {

        if (id == null) {
            return null;
        }


        return catalogRepository
                .findByIdAndTenant_IdAndType(
                        id,

                        currentTenantService
                                .getCurrentTenantId(),

                        type
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        message
                                )
                );
    }


    // =====================================================
    // USER
    // =====================================================

    private User getCurrentUser(
            Long userId
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        return userRepository
                .findByIdAndTenant_Id(
                        userId,
                        tenantId
                )
                .orElseThrow(
                        () ->
                                new AccessDeniedException(
                                        "La sesión no corresponde a un usuario válido."
                                )
                );
    }


    // =====================================================
    // HELPERS
    // =====================================================

    private Map<Long, InventoryProduct> indexProducts(
            List<InventoryProduct> products
    ) {

        Map<Long, InventoryProduct> result =
                new HashMap<>();


        for (InventoryProduct product : products) {

            result.put(
                    product.getId(),
                    product
            );
        }


        return result;
    }


    private String normalizeText(
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


    private BigDecimal money(
            BigDecimal value
    ) {

        if (value == null) {
            return new BigDecimal("0.00");
        }


        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }


    private int multiplyExact(
            int first,
            int second
    ) {

        try {

            return Math.multiplyExact(
                    first,
                    second
            );

        } catch (
                ArithmeticException exception
        ) {

            throw new ConflictException(
                    "La cantidad requerida es demasiado grande."
            );
        }
    }


    private void deletePhotoQuietly(
            String fileName
    ) {

        if (
                fileName == null
                        ||
                        fileName.isBlank()
        ) {

            return;
        }


        try {

            storageService.delete(
                    fileName
            );

        } catch (RuntimeException ignored) {
        }
    }


    private record PreparedComponent(
            InventoryProduct product,
            int requiredQuantity
    ) {
    }
}