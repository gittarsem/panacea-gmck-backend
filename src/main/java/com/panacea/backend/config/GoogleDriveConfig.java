package com.panacea.backend.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class GoogleDriveConfig {

    private static final GsonFactory JSON_FACTORY =
            GsonFactory.getDefaultInstance();

    @Value("${google.drive.client-id}")
    private String clientId;

    @Value("${google.drive.client-secret}")
    private String clientSecret;

    @Value("${google.drive.refresh-token}")
    private String refreshToken;

    @Bean
    public Drive googleDrive()
            throws GeneralSecurityException, IOException {

        // =========================================================
        // VALIDATE CONFIGURATION
        // =========================================================

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_DRIVE_CLIENT_ID is not configured"
            );
        }

        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_DRIVE_CLIENT_SECRET is not configured"
            );
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_DRIVE_REFRESH_TOKEN is not configured"
            );
        }

        // =========================================================
        // GOOGLE OAUTH USER CREDENTIALS
        // =========================================================

        UserCredentials userCredentials =
                UserCredentials.newBuilder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setRefreshToken(refreshToken)
                        .build();

        // =========================================================
        // DRIVE SCOPE
        // =========================================================

        GoogleCredentials credentials =
                userCredentials.createScoped(
                        DriveScopes.DRIVE
                );

        // =========================================================
        // HTTP CREDENTIALS ADAPTER
        // =========================================================

        HttpCredentialsAdapter credentialsAdapter =
                new HttpCredentialsAdapter(credentials);

        // =========================================================
        // GOOGLE DRIVE CLIENT
        // =========================================================

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credentialsAdapter
        )
                .setApplicationName("PANACEA 2026")
                .build();
    }
}