package com.panacea.backend.controller;

import com.google.api.services.drive.model.File;
import com.panacea.backend.service.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequestMapping("/api/br-cards")
@RequiredArgsConstructor
public class BrCardPaymentScreenshotController {

    private final GoogleDriveService googleDriveService;

    @GetMapping("/payment-screenshot/{fileId}")
    public ResponseEntity<InputStreamResource> getPaymentScreenshot(
            @PathVariable String fileId
    ) {

        File metadata =
                googleDriveService.getFileMetadata(fileId);

        InputStream inputStream =
                googleDriveService.downloadFile(fileId);

        String mimeType = metadata.getMimeType();

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(mimeType);
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        InputStreamResource resource =
                new InputStreamResource(inputStream);

        ResponseEntity.BodyBuilder response =
                ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(
                                HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" +
                                        metadata.getName() +
                                        "\""
                        );

        if (metadata.getSize() != null) {
            response.contentLength(metadata.getSize());
        }

        return response.body(resource);
    }
}