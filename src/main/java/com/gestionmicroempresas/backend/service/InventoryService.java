package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.CreateInventoryProductRequest;
import com.gestionmicroempresas.backend.dto.request.DeleteInventoryProductsRequest;
import com.gestionmicroempresas.backend.dto.request.StockAdjustmentRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateInventoryProductRequest;

import com.gestionmicroempresas.backend.dto.response.CatalogItemResponse;
import com.gestionmicroempresas.backend.dto.response.InventoryCatalogsResponse;
import com.gestionmicroempresas.backend.dto.response.InventoryPageResponse;
import com.gestionmicroempresas.backend.dto.response.InventoryProductPermissionsResponse;
import com.gestionmicroempresas.backend.dto.response.InventoryProductResponse;
import com.gestionmicroempresas.backend.dto.response.InventoryReportResponse;
import com.gestionmicroempresas.backend.dto.response.InventorySummaryResponse;

import com.gestionmicroempresas.backend.entity.InventoryCatalogItem;
import com.gestionmicroempresas.backend.entity.InventoryProduct;
import com.gestionmicroempresas.backend.entity.InventoryStockMovement;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.InventoryCatalogType;
import com.gestionmicroempresas.backend.enums.InventoryMovementType;

import com.gestionmicroempresas.backend.exception.ConflictException;
import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.repository.InventoryCatalogRepository;
import com.gestionmicroempresas.backend.repository.InventoryProductRepository;
import com.gestionmicroempresas.backend.repository.InventoryStockMovementRepository;
import com.gestionmicroempresas.backend.repository.UserRepository;

import com.gestionmicroempresas.backend.storage.InventoryPhotoStorageService;

import jakarta.persistence.criteria.JoinType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class InventoryService {

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");


    private final InventoryProductRepository productRepository;

    private final InventoryCatalogRepository catalogRepository;

    private final InventoryStockMovementRepository movementRepository;

    private final UserRepository userRepository;

    private final InventoryPhotoStorageService storageService;

    private final CurrentTenantService currentTenantService;


    public InventoryService(
            InventoryProductRepository productRepository,
            InventoryCatalogRepository catalogRepository,
            InventoryStockMovementRepository movementRepository,
            UserRepository userRepository,
            InventoryPhotoStorageService storageService,
            CurrentTenantService currentTenantService
    ) {

        this.productRepository = productRepository;
        this.catalogRepository = catalogRepository;
        this.movementRepository = movementRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.currentTenantService = currentTenantService;
    }


    // =====================================================
    // LIST
    // =====================================================

    @Transactional(readOnly = true)
    public InventoryPageResponse getInventory(
            int page,
            int size,
            String sort,
            String search,
            Long brandId,
            Long categoryId,
            Long measureId,
            Long warehouseId,
            String status
    ) {

        Specification<InventoryProduct> specification =
                buildSpecification(
                        search,
                        brandId,
                        categoryId,
                        measureId,
                        warehouseId,
                        status
                );


        PageRequest pageable =
                PageRequest.of(
                        Math.max(page, 0),

                        Math.min(
                                Math.max(size, 1),
                                100
                        ),

                        resolveSort(sort)
                );


        Page<InventoryProduct> result =
                productRepository.findAll(
                        specification,
                        pageable
                );


        List<InventoryProduct> filteredProducts =
                productRepository.findAll(
                        specification
                );


        return new InventoryPageResponse(

                result
                        .getContent()
                        .stream()
                        .map(
                                this::buildResponse
                        )
                        .toList(),

                result.getNumber(),

                result.getSize(),

                result.getTotalElements(),

                result.getTotalPages(),

                calculateSummary(
                        filteredProducts
                )
        );
    }


    // =====================================================
    // CATALOGS
    // =====================================================

    @Transactional(readOnly = true)
    public InventoryCatalogsResponse getCatalogs() {

        return new InventoryCatalogsResponse(

                getCatalog(
                        InventoryCatalogType.UNIT_OF_MEASURE
                ),

                getCatalog(
                        InventoryCatalogType.BRAND
                ),

                getCatalog(
                        InventoryCatalogType.WAREHOUSE
                ),

                getCatalog(
                        InventoryCatalogType.CONDITION
                ),

                getCatalog(
                        InventoryCatalogType.CATEGORY
                ),

                getCatalog(
                        InventoryCatalogType.SUPPLIER
                ),

                getCatalog(
                        InventoryCatalogType.MEASURE
                )
        );
    }


    // =====================================================
    // QR
    // =====================================================

    @Transactional(readOnly = true)
    public InventoryProductResponse getProductByQr(
            String qrCode
    ) {

        if (
                qrCode == null
                        ||
                        qrCode.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El código QR es obligatorio."
            );
        }


        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        InventoryProduct product =
                productRepository
                        .findByTenant_IdAndQrCode(
                                tenantId,
                                qrCode.trim()
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "No existe un producto con ese código QR."
                                        )
                        );


        return buildResponse(
                product
        );
    }


    // =====================================================
    // CREATE
    // =====================================================

    @Transactional
    public InventoryProductResponse createProduct(
            Long currentUserId,
            CreateInventoryProductRequest request,
            MultipartFile photo
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        ProductCatalogs catalogs =
                resolveCatalogs(
                        request.categoryId(),
                        request.brandId(),
                        request.unitMeasureId(),
                        request.supplierId(),
                        request.measureId(),
                        request.warehouseId(),
                        request.conditionId()
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


            InventoryProduct product =
                    new InventoryProduct(

                            currentUser.getTenant(),

                            request
                                    .description()
                                    .trim(),

                            normalizeText(
                                    request.model()
                            ),

                            catalogs.category(),

                            catalogs.brand(),

                            catalogs.unitMeasure(),

                            catalogs.supplier(),

                            catalogs.measure(),

                            catalogs.warehouse(),

                            catalogs.condition(),

                            normalizeText(
                                    request.rowLocation()
                            ),

                            money(
                                    request.unitPrice()
                            ),

                            request.initialQuantity(),

                            request.minimumStock()
                    );


            product.setPhotoFileName(
                    photoFileName
            );


            InventoryProduct savedProduct =
                    productRepository.saveAndFlush(
                            product
                    );


            savedProduct.setQrCode(
                    "INV:"
                            +
                            savedProduct.getId()
            );


            savedProduct =
                    productRepository.saveAndFlush(
                            savedProduct
                    );


            if (
                    request.initialQuantity()
                            >
                            0
            ) {

                movementRepository.saveAndFlush(

                        new InventoryStockMovement(
                                savedProduct,
                                currentUser,
                                InventoryMovementType.INITIAL,
                                request.initialQuantity(),
                                request.initialQuantity(),
                                "Existencia inicial"
                        )
                );
            }


            return buildResponse(
                    savedProduct
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
    public InventoryProductResponse updateProduct(
            Long productId,
            UpdateInventoryProductRequest request,
            MultipartFile photo
    ) {

        InventoryProduct product =
                getProduct(
                        productId
                );


        validateRegularProduct(
                product
        );


        ProductCatalogs catalogs =
                resolveCatalogs(
                        request.categoryId(),
                        request.brandId(),
                        request.unitMeasureId(),
                        request.supplierId(),
                        request.measureId(),
                        request.warehouseId(),
                        request.conditionId()
                );


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
                    product.getPhotoFileName();


            product.setDescription(
                    request
                            .description()
                            .trim()
            );


            product.setModel(
                    normalizeText(
                            request.model()
                    )
            );


            product.setCategory(
                    catalogs.category()
            );

            product.setBrand(
                    catalogs.brand()
            );

            product.setUnitMeasure(
                    catalogs.unitMeasure()
            );

            product.setSupplier(
                    catalogs.supplier()
            );

            product.setMeasure(
                    catalogs.measure()
            );

            product.setWarehouse(
                    catalogs.warehouse()
            );

            product.setCondition(
                    catalogs.condition()
            );


            product.setRowLocation(
                    normalizeText(
                            request.rowLocation()
                    )
            );


            product.setUnitPrice(
                    money(
                            request.unitPrice()
                    )
            );


            product.setMinimumStock(
                    request.minimumStock()
            );


            if (newPhotoFileName != null) {

                product.setPhotoFileName(
                        newPhotoFileName
                );
            }


            InventoryProduct updatedProduct =
                    productRepository.saveAndFlush(
                            product
                    );


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


            return buildResponse(
                    updatedProduct
            );

        } catch (RuntimeException exception) {

            deletePhotoQuietly(
                    newPhotoFileName
            );

            throw exception;
        }
    }


    // =====================================================
    // DELETE ONE
    // =====================================================

    @Transactional
    public void deleteProduct(
            Long productId
    ) {

        InventoryProduct product =
                getProduct(
                        productId
                );


        validateRegularProduct(
                product
        );


        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        if (
                movementRepository
                        .existsByTenant_IdAndProduct_Id(
                                tenantId,
                                productId
                        )
        ) {

            throw new ConflictException(
                    "No puedes eliminar el producto porque tiene historial de movimientos."
            );
        }


        String photo =
                product.getPhotoFileName();


        productRepository.delete(
                product
        );


        productRepository.flush();


        deletePhotoQuietly(
                photo
        );
    }


    // =====================================================
    // DELETE BATCH
    // =====================================================

    @Transactional
    public void deleteProducts(
            DeleteInventoryProductsRequest request
    ) {

        Set<Long> uniqueIds =
                new LinkedHashSet<>(
                        request.ids()
                );


        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        List<InventoryProduct> products =
                productRepository
                        .findAllByTenant_IdAndIdIn(
                                tenantId,
                                uniqueIds
                        );


        if (
                products.size()
                        !=
                        uniqueIds.size()
        ) {

            throw new ResourceNotFoundException(
                    "Uno o más productos seleccionados no existen."
            );
        }


        for (InventoryProduct product : products) {

            validateRegularProduct(
                    product
            );


            if (
                    movementRepository
                            .existsByTenant_IdAndProduct_Id(
                                    tenantId,
                                    product.getId()
                            )
            ) {

                throw new ConflictException(
                        "Uno o más productos seleccionados tienen historial de movimientos y no pueden eliminarse."
                );
            }
        }


        List<String> photos =
                products
                        .stream()
                        .map(
                                InventoryProduct::getPhotoFileName
                        )
                        .filter(
                                value ->
                                        value != null
                                                &&
                                                !value.isBlank()
                        )
                        .toList();


        productRepository.deleteAll(
                products
        );


        productRepository.flush();


        for (String photo : photos) {

            deletePhotoQuietly(
                    photo
            );
        }
    }


    // =====================================================
    // DUPLICATE
    // =====================================================

    @Transactional
    public InventoryProductResponse duplicateProduct(
            Long productId
    ) {

        InventoryProduct source =
                getProduct(
                        productId
                );


        validateRegularProduct(
                source
        );


        String duplicatedPhoto = null;


        try {

            duplicatedPhoto =
                    storageService.duplicatePhoto(
                            source.getPhotoFileName()
                    );


            InventoryProduct duplicate =
                    new InventoryProduct(

                            source.getTenant(),

                            source.getDescription()
                                    +
                                    " - Copia",

                            source.getModel(),

                            source.getCategory(),

                            source.getBrand(),

                            source.getUnitMeasure(),

                            source.getSupplier(),

                            source.getMeasure(),

                            source.getWarehouse(),

                            source.getCondition(),

                            source.getRowLocation(),

                            source.getUnitPrice(),

                            0,

                            source.getMinimumStock()
                    );


            duplicate.setPhotoFileName(
                    duplicatedPhoto
            );


            InventoryProduct saved =
                    productRepository.saveAndFlush(
                            duplicate
                    );


            saved.setQrCode(
                    "INV:"
                            +
                            saved.getId()
            );


            saved =
                    productRepository.saveAndFlush(
                            saved
                    );


            return buildResponse(
                    saved
            );

        } catch (RuntimeException exception) {

            deletePhotoQuietly(
                    duplicatedPhoto
            );

            throw exception;
        }
    }


    // =====================================================
    // STOCK
    // =====================================================

    @Transactional
    public InventoryProductResponse adjustStock(
            Long currentUserId,
            Long productId,
            StockAdjustmentRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Long tenantId =
                currentUser.getTenantId();


        InventoryProduct product =
                productRepository
                        .findByTenantAndIdForUpdate(
                                tenantId,
                                productId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "El producto solicitado no existe."
                                        )
                        );


        validateRegularProduct(
                product
        );


        InventoryMovementType type =
                InventoryMovementType
                        .fromAdjustmentApiValue(
                                request.type()
                        );


        int currentQuantity =
                product.getQuantity();


        int change;

        int resultingQuantity;


        if (
                type
                        ==
                        InventoryMovementType.ENTRY
        ) {

            change =
                    request.quantity();


            try {

                resultingQuantity =
                        Math.addExact(
                                currentQuantity,
                                request.quantity()
                        );

            } catch (
                    ArithmeticException exception
            ) {

                throw new ConflictException(
                        "La cantidad resultante es demasiado grande."
                );
            }

        } else {

            if (
                    request.quantity()
                            >
                            currentQuantity
            ) {

                throw new ConflictException(
                        "No hay suficiente stock para realizar la salida."
                );
            }


            change =
                    -request.quantity();


            resultingQuantity =
                    currentQuantity
                            -
                            request.quantity();
        }


        product.setQuantity(
                resultingQuantity
        );


        InventoryStockMovement movement =
                new InventoryStockMovement(
                        product,
                        currentUser,
                        type,
                        change,
                        resultingQuantity,
                        normalizeText(
                                request.reason()
                        )
                );


        productRepository.save(
                product
        );

        movementRepository.save(
                movement
        );

        productRepository.flush();

        movementRepository.flush();


        return buildResponse(
                product
        );
    }


    // =====================================================
    // REPORT
    // =====================================================

    @Transactional(readOnly = true)
    public InventoryReportResponse getReport(
            String search,
            Long brandId,
            Long categoryId,
            Long measureId,
            Long warehouseId,
            String status
    ) {

        Specification<InventoryProduct> specification =
                buildSpecification(
                        search,
                        brandId,
                        categoryId,
                        measureId,
                        warehouseId,
                        status
                );


        List<InventoryProduct> products =
                productRepository.findAll(
                        specification,

                        Sort.by(
                                Sort.Direction.ASC,
                                "description"
                        )
                );


        return new InventoryReportResponse(

                products
                        .stream()
                        .map(
                                this::buildResponse
                        )
                        .toList(),

                calculateInventoryValue(
                        products
                )
        );
    }


    // =====================================================
    // PHOTO ACCESS
    // =====================================================

    @Transactional(readOnly = true)
    public void validatePhotoAccess(
            Long currentUserId,
            String fileName
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        if (
                fileName == null
                        ||
                        fileName.isBlank()
        ) {

            throw new ResourceNotFoundException(
                    "La fotografía solicitada no existe."
            );
        }


        productRepository
                .findByTenant_IdAndPhotoFileName(
                        currentUser.getTenantId(),
                        fileName
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "La fotografía solicitada no existe."
                                )
                );
    }


    // =====================================================
    // SPECIFICATION
    // =====================================================

    private Specification<InventoryProduct> buildSpecification(
            String search,
            Long brandId,
            Long categoryId,
            Long measureId,
            Long warehouseId,
            String status
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        Specification<InventoryProduct> specification =
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


        if (
                search != null
                        &&
                        !search.isBlank()
        ) {

            String pattern =
                    "%"
                            +
                            search
                                    .trim()
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
                            ) -> {

                                if (query != null) {
                                    query.distinct(true);
                                }


                                var supplier =
                                        root.join(
                                                "supplier",
                                                JoinType.LEFT
                                        );


                                return criteriaBuilder.or(

                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get(
                                                                "description"
                                                        )
                                                ),
                                                pattern
                                        ),

                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get(
                                                                "model"
                                                        )
                                                ),
                                                pattern
                                        ),

                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get(
                                                                "rowLocation"
                                                        )
                                                ),
                                                pattern
                                        ),

                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        supplier.get(
                                                                "name"
                                                        )
                                                ),
                                                pattern
                                        )
                                );
                            }
                    );
        }


        if (brandId != null) {

            specification =
                    specification.and(
                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.equal(
                                            root
                                                    .get("brand")
                                                    .get("id"),

                                            brandId
                                    )
                    );
        }


        if (categoryId != null) {

            specification =
                    specification.and(
                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.equal(
                                            root
                                                    .get("category")
                                                    .get("id"),

                                            categoryId
                                    )
                    );
        }


        if (measureId != null) {

            specification =
                    specification.and(
                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.equal(
                                            root
                                                    .get("measure")
                                                    .get("id"),

                                            measureId
                                    )
                    );
        }


        if (warehouseId != null) {

            specification =
                    specification.and(
                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.equal(
                                            root
                                                    .get("warehouse")
                                                    .get("id"),

                                            warehouseId
                                    )
                    );
        }


        if (
                status != null
                        &&
                        !status.isBlank()
        ) {

            if (
                    !"STOCK_BAJO"
                            .equalsIgnoreCase(
                                    status.trim()
                            )
            ) {

                throw new IllegalArgumentException(
                        "El estatus de inventario no es válido."
                );
            }


            specification =
                    specification.and(
                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.and(

                                            criteriaBuilder.isFalse(
                                                    root.get(
                                                            "kit"
                                                    )
                                            ),

                                            criteriaBuilder.lessThanOrEqualTo(
                                                    root.get(
                                                            "quantity"
                                                    ),

                                                    root.get(
                                                            "minimumStock"
                                                    )
                                            )
                                    )
                    );
        }


        return specification;
    }


    // =====================================================
    // SUMMARY
    // =====================================================

    private InventorySummaryResponse calculateSummary(
            List<InventoryProduct> products
    ) {

        long lowStock =
                products
                        .stream()
                        .filter(
                                product ->
                                        !product.isKit()
                                                &&
                                                product.getQuantity()
                                                        <=
                                                        product.getMinimumStock()
                        )
                        .count();


        return new InventorySummaryResponse(
                calculateInventoryValue(
                        products
                ),
                lowStock,
                products.size()
        );
    }


    private BigDecimal calculateInventoryValue(
            List<InventoryProduct> products
    ) {

        BigDecimal result =
                ZERO;


        for (InventoryProduct product : products) {

            result =
                    result.add(

                            product
                                    .getUnitPrice()
                                    .multiply(
                                            BigDecimal.valueOf(
                                                    product.getQuantity()
                                            )
                                    )
                    );
        }


        return money(
                result
        );
    }


    // =====================================================
    // CATALOG RESOLUTION
    // =====================================================

    private List<CatalogItemResponse> getCatalog(
            InventoryCatalogType type
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        return catalogRepository
                .findAllByTenant_IdAndTypeOrderByNameAsc(
                        tenantId,
                        type
                )
                .stream()
                .map(
                        CatalogItemResponse::from
                )
                .toList();
    }


    private ProductCatalogs resolveCatalogs(
            Long categoryId,
            Long brandId,
            Long unitMeasureId,
            Long supplierId,
            Long measureId,
            Long warehouseId,
            Long conditionId
    ) {

        return new ProductCatalogs(

                resolveCatalogItem(
                        categoryId,
                        InventoryCatalogType.CATEGORY,
                        "La categoría seleccionada no existe."
                ),

                resolveCatalogItem(
                        brandId,
                        InventoryCatalogType.BRAND,
                        "La marca seleccionada no existe."
                ),

                resolveCatalogItem(
                        unitMeasureId,
                        InventoryCatalogType.UNIT_OF_MEASURE,
                        "La unidad de medida seleccionada no existe."
                ),

                resolveCatalogItem(
                        supplierId,
                        InventoryCatalogType.SUPPLIER,
                        "El proveedor seleccionado no existe."
                ),

                resolveCatalogItem(
                        measureId,
                        InventoryCatalogType.MEASURE,
                        "La medida seleccionada no existe."
                ),

                resolveCatalogItem(
                        warehouseId,
                        InventoryCatalogType.WAREHOUSE,
                        "El almacén seleccionado no existe."
                ),

                resolveCatalogItem(
                        conditionId,
                        InventoryCatalogType.CONDITION,
                        "La condición seleccionada no existe."
                )
        );
    }


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
    // RESPONSE
    // =====================================================

    private InventoryProductResponse buildResponse(
            InventoryProduct product
    ) {

        InventoryProductPermissionsResponse permissions;


        if (product.isKit()) {

            permissions =
                    new InventoryProductPermissionsResponse(
                            false,
                            false,
                            false,
                            false,
                            true,
                            product.getQuantity() > 0
                    );

        } else {

            permissions =
                    new InventoryProductPermissionsResponse(
                            true,
                            true,
                            true,
                            true,
                            false,
                            false
                    );
        }


        return InventoryProductResponse.from(
                product,

                storageService.getPublicUrl(
                        product.getPhotoFileName()
                ),

                permissions
        );
    }


    // =====================================================
    // ENTITIES
    // =====================================================

    private InventoryProduct getProduct(
            Long productId
    ) {

        return productRepository
                .findByIdAndTenant_Id(
                        productId,

                        currentTenantService
                                .getCurrentTenantId()
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El producto solicitado no existe."
                                )
                );
    }


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
    // VALIDATION
    // =====================================================

    private void validateRegularProduct(
            InventoryProduct product
    ) {

        if (product.isKit()) {

            throw new ConflictException(
                    "Los kits deben administrarse desde la configuración de kits."
            );
        }
    }


    // =====================================================
    // STORAGE
    // =====================================================

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


    // =====================================================
    // NORMALIZATION
    // =====================================================

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
            return ZERO;
        }


        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }


    private Sort resolveSort(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return Sort.by(
                    Sort.Direction.ASC,
                    "description"
            );
        }


        String[] parts =
                value.split(",");


        String property =
                switch (
                        parts[0].trim()
                        ) {

                    case "cantidad" ->
                            "quantity";

                    case "precioUnitario" ->
                            "unitPrice";

                    case "creadoEn" ->
                            "createdAt";

                    default ->
                            "description";
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


    private record ProductCatalogs(
            InventoryCatalogItem category,
            InventoryCatalogItem brand,
            InventoryCatalogItem unitMeasure,
            InventoryCatalogItem supplier,
            InventoryCatalogItem measure,
            InventoryCatalogItem warehouse,
            InventoryCatalogItem condition
    ) {
    }
}