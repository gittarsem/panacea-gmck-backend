package com.panacea.backend.service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.UUID;

@Service
public class GoogleDriveService {

    private final Drive drive;

    @Value("${google.drive.folder-id}")
    private String folderId;

    public GoogleDriveService(Drive drive) {
        this.drive = drive;
    }

    /**
     * Upload payment screenshot to Google Drive.
     *
     * The file is streamed directly to Google Drive.
     * It is NOT loaded completely into memory.
     *
     * After upload, the file is made publicly readable.
     *
     * @param multipartFile uploaded payment screenshot
     * @return public Google Drive image URL
     */
    public String uploadFile(MultipartFile multipartFile) {

        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty"
            );
        }

        if (folderId == null || folderId.isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_DRIVE_FOLDER_ID is not configured"
            );
        }

        try (
                InputStream inputStream =
                        multipartFile.getInputStream()
        ) {

            // =========================================================
            // FILE NAME
            // =========================================================

            String originalFilename =
                    multipartFile.getOriginalFilename();

            String extension = "";

            if (
                    originalFilename != null
                            && originalFilename.contains(".")
            ) {
                extension =
                        originalFilename.substring(
                                originalFilename.lastIndexOf(".")
                        );
            }

            String fileName =
                    "PANACEA_PAYMENT_"
                            + UUID.randomUUID()
                            + extension;

            // =========================================================
            // FILE METADATA
            // =========================================================

            File fileMetadata = new File();

            fileMetadata.setName(fileName);

            fileMetadata.setParents(
                    Collections.singletonList(folderId)
            );

            // =========================================================
            // CONTENT TYPE
            // =========================================================

            String contentType =
                    multipartFile.getContentType();

            if (
                    contentType == null
                            || contentType.isBlank()
            ) {
                contentType =
                        "application/octet-stream";
            }

            InputStreamContent mediaContent =
                    new InputStreamContent(
                            contentType,
                            inputStream
                    );

            if (multipartFile.getSize() > 0) {
                mediaContent.setLength(
                        multipartFile.getSize()
                );
            }

            // =========================================================
            // UPLOAD TO GOOGLE DRIVE
            // =========================================================

            File uploadedFile =
                    drive.files()
                            .create(
                                    fileMetadata,
                                    mediaContent
                            )
                            .setFields(
                                    "id,name,mimeType,size,createdTime"
                            )
                            .execute();

            if (
                    uploadedFile == null
                            || uploadedFile.getId() == null
                            || uploadedFile.getId().isBlank()
            ) {
                throw new IllegalStateException(
                        "Google Drive upload failed: no file ID returned"
                );
            }

            String fileId =
                    uploadedFile.getId();

            // =========================================================
            // MAKE FILE PUBLIC
            // =========================================================

            Permission publicPermission =
                    new Permission()
                            .setType("anyone")
                            .setRole("reader");

            drive.permissions()
                    .create(
                            fileId,
                            publicPermission
                    )
                    .setFields("id,type,role")
                    .execute();

            // =========================================================
            // PUBLIC IMAGE URL
            // =========================================================
            //
            // This URL can directly be used by <img src="...">
            //
            // Example:
            // https://drive.google.com/thumbnail?id=FILE_ID&sz=w2000
            //
            // =========================================================

            return "https://drive.google.com/uc"
                    + "?export=view"
                    + "&id="
                    + fileId;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to upload file to Google Drive",
                    e
            );
        }
    }

    /**
     * Download a Google Drive file as a stream.
     *
     * Kept for backend/admin use if required.
     *
     * @param fileId Google Drive file ID
     * @return file InputStream
     */
    public InputStream downloadFile(String fileId) {

        validateFileId(fileId);

        try {

            return drive.files()
                    .get(fileId)
                    .executeMediaAsInputStream();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to download file from Google Drive",
                    e
            );
        }
    }

    /**
     * Get Google Drive file metadata.
     *
     * @param fileId Google Drive file ID
     * @return Google Drive file metadata
     */
    public File getFileMetadata(String fileId) {

        validateFileId(fileId);

        try {

            return drive.files()
                    .get(fileId)
                    .setFields(
                            "id,name,mimeType,size,createdTime"
                    )
                    .execute();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to retrieve Google Drive file metadata",
                    e
            );
        }
    }

    /**
     * Delete a file from Google Drive.
     *
     * @param fileId Google Drive file ID
     */
    public void deleteFile(String fileId) {

        if (fileId == null || fileId.isBlank()) {
            return;
        }

        try {

            drive.files()
                    .delete(fileId)
                    .execute();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to delete file from Google Drive",
                    e
            );
        }
    }

    /**
     * Validate Google Drive file ID.
     */
    private void validateFileId(String fileId) {

        if (fileId == null || fileId.isBlank()) {

            throw new IllegalArgumentException(
                    "Google Drive file ID cannot be empty"
            );
        }
    }
}