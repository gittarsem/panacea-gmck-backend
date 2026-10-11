package com.panacea.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentScreenshotService {

    private static final long MAX_FILE_SIZE =
            100 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private final Cloudinary cloudinary;

    public PaymentScreenshotService(
            Cloudinary cloudinary
    ) {
        this.cloudinary = cloudinary;
    }

    public String upload(
            MultipartFile file
    ) {

        validate(file);

        try {

            String publicId =
                    "panacea26/br-card/payment-proofs/"
                            + UUID.randomUUID();

            Map<?, ?> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.asMap(
                                    "public_id",
                                    publicId,

                                    "resource_type",
                                    "image",

                                    "folder",
                                    "panacea26/br-card/payment-proofs",

                                    "overwrite",
                                    false
                            )
                    );

            Object secureUrl =
                    result.get("secure_url");

            if (secureUrl == null) {
                throw new IllegalStateException(
                        "Cloudinary did not return a secure URL"
                );
            }

            return secureUrl.toString();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to upload payment screenshot",
                    e
            );
        }
    }

    private void validate(
            MultipartFile file
    ) {

        if (file == null ||
                file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Payment screenshot is required"
            );
        }

        if (file.getSize() >
                MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "Payment screenshot must be smaller than 5MB"
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null ||
                !ALLOWED_TYPES.contains(
                        contentType.toLowerCase()
                )) {

            throw new IllegalArgumentException(
                    "Only JPG, PNG and WEBP images are allowed"
            );
        }
    }
}