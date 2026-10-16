package com.gestionmicroempresas.backend.storage;

import com.gestionmicroempresas.backend.exception.UploadTooLargeException;

import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ImageUploadValidatorTests {

    private final ImageUploadValidator validator =
            new ImageUploadValidator();


    // =====================================================
    // VALID WEBP
    // =====================================================

    @Test
    void validWebpMustBeAccepted() {

        MockMultipartFile file =
                new MockMultipartFile(

                        "foto",

                        "product.webp",

                        "image/webp",

                        validWebpBytes()
                );


        ValidatedImage result =
                validator.validate(

                        file,

                        ImageUploadPolicy.INVENTORY_PHOTO
                );


        assertEquals(
                "image/webp",
                result.contentType()
        );


        assertEquals(
                ".webp",
                result.extension()
        );


        assertEquals(
                file.getSize(),
                result.size()
        );
    }


    // =====================================================
    // FAKE WEBP
    // =====================================================

    @Test
    void executableDisguisedAsWebpMustBeRejected() {

        byte[] executableContent =
                new byte[]{

                        'M',
                        'Z',

                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0
                };


        MockMultipartFile file =
                new MockMultipartFile(

                        "foto",

                        "virus.webp",

                        "image/webp",

                        executableContent
                );


        IllegalArgumentException exception =
                assertThrows(

                        IllegalArgumentException.class,

                        () ->
                                validator.validate(

                                        file,

                                        ImageUploadPolicy.INVENTORY_PHOTO
                                )
                );


        assertTrue(

                exception
                        .getMessage()
                        .contains(
                                "WebP"
                        )
        );
    }


    // =====================================================
    // WRONG MIME
    // =====================================================

    @Test
    void nonWebpMimeTypeMustBeRejected() {

        MockMultipartFile file =
                new MockMultipartFile(

                        "foto",

                        "image.png",

                        "image/png",

                        validWebpBytes()
                );


        assertThrows(

                IllegalArgumentException.class,

                () ->
                        validator.validate(

                                file,

                                ImageUploadPolicy.INVENTORY_PHOTO
                        )
        );
    }


    // =====================================================
    // EMPTY FILE
    // =====================================================

    @Test
    void emptyFileMustBeRejected() {

        MockMultipartFile file =
                new MockMultipartFile(

                        "foto",

                        "empty.webp",

                        "image/webp",

                        new byte[0]
                );


        assertThrows(

                IllegalArgumentException.class,

                () ->
                        validator.validate(

                                file,

                                ImageUploadPolicy.INVENTORY_PHOTO
                        )
        );
    }


    // =====================================================
    // OVERSIZED LOGO
    // =====================================================

    @Test
    void oversizedCompanyLogoMustBeRejected() {

        byte[] content =
                new byte[
                        (256 * 1024)
                                +
                                1
                        ];


        byte[] header =
                validWebpBytes();


        System.arraycopy(

                header,

                0,

                content,

                0,

                header.length
        );


        MockMultipartFile file =
                new MockMultipartFile(

                        "logo",

                        "large-logo.webp",

                        "image/webp",

                        content
                );


        assertThrows(

                UploadTooLargeException.class,

                () ->
                        validator.validate(

                                file,

                                ImageUploadPolicy.COMPANY_LOGO
                        )
        );
    }


    // =====================================================
    // TEST IMAGE
    // =====================================================

    private byte[] validWebpBytes() {

        return new byte[]{

                'R',
                'I',
                'F',
                'F',

                8,
                0,
                0,
                0,

                'W',
                'E',
                'B',
                'P',

                'V',
                'P',
                '8',
                ' '
        };
    }
}