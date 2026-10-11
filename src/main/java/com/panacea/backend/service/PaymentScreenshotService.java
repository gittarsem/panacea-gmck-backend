package com.panacea.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentScreenshotService {

    /*
     * Maximum file that Spring Boot will accept.
     * This is NOT the Cloudinary limit.
     */
    private static final long MAX_FILE_SIZE =
            100L * 1024 * 1024;

    /*
     * Keep the compressed image comfortably below
     * Cloudinary's 10 MB limit.
     */
    private static final long TARGET_FILE_SIZE =
            8L * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    /*
     * Maximum dimension for payment screenshots.
     *
     * There is no reason for a payment proof to remain
     * 4000-6000+ pixels wide.
     */
    private static final int MAX_IMAGE_DIMENSION = 2500;

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

            /*
             * Convert the uploaded image into a compressed JPEG.
             *
             * This means:
             *
             * 10-20 MB original
             *        ↓
             * resize if necessary
             *        ↓
             * JPEG compression
             *        ↓
             * <= 8 MB
             *        ↓
             * Cloudinary
             */
            byte[] compressedBytes =
                    compressImage(file);

            String publicId =
                    "panacea26/br-card/payment-proofs/"
                            + UUID.randomUUID();

            Map<?, ?> result =
                    cloudinary.uploader().upload(
                            compressedBytes,
                            ObjectUtils.asMap(
                                    "public_id",
                                    publicId,

                                    "resource_type",
                                    "image",

                                    "folder",
                                    "panacea26/br-card/payment-proofs",

                                    "format",
                                    "jpg",

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

    private byte[] compressImage(
            MultipartFile file
    ) throws IOException {

        BufferedImage original;

        /*
         * Read the image using ImageReader with subsampling.
         *
         * This is important for Render because ImageIO.read()
         * can consume a huge amount of heap when the uploaded
         * image has very high resolution.
         */
        try (var inputStream = file.getInputStream();
             var imageInput = ImageIO.createImageInputStream(inputStream)) {

            if (imageInput == null) {
                throw new IllegalArgumentException(
                        "Unable to read uploaded image"
                );
            }

            Iterator<javax.imageio.ImageReader> readers =
                    ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "Unsupported image format"
                );
            }

            javax.imageio.ImageReader reader =
                    readers.next();

            try {
                reader.setInput(imageInput, true, true);

                int originalWidth =
                        reader.getWidth(0);

                int originalHeight =
                        reader.getHeight(0);

                /*
                 * Calculate how much we should downsample
                 * while reading the image.
                 */
                int largestDimension =
                        Math.max(
                                originalWidth,
                                originalHeight
                        );

                int sample =
                        Math.max(
                                1,
                                (int) Math.ceil(
                                        (double) largestDimension
                                                / MAX_IMAGE_DIMENSION
                                )
                        );

                javax.imageio.ImageReadParam readParam =
                        reader.getDefaultReadParam();

                if (sample > 1) {
                    readParam.setSourceSubsampling(
                            sample,
                            sample,
                            0,
                            0
                    );
                }

                original =
                        reader.read(
                                0,
                                readParam
                        );

            } finally {
                reader.dispose();
            }
        }

        if (original == null) {
            throw new IllegalArgumentException(
                    "Unable to read the uploaded image"
            );
        }

        /*
         * Make absolutely sure the resulting image
         * does not exceed our maximum dimensions.
         */
        BufferedImage resized =
                resizeImage(original);

        /*
         * Start with high JPEG quality.
         */
        float quality = 0.90f;

        byte[] compressed =
                compressAsJpeg(
                        resized,
                        quality
                );

        /*
         * Reduce JPEG quality until the image
         * is safely below 8 MB.
         */
        while (
                compressed.length > TARGET_FILE_SIZE
                        && quality > 0.45f
        ) {

            quality -= 0.05f;

            compressed =
                    compressAsJpeg(
                            resized,
                            quality
                    );
        }

        /*
         * If quality reduction alone is not enough,
         * progressively reduce dimensions.
         */
        int currentWidth =
                resized.getWidth();

        int currentHeight =
                resized.getHeight();

        while (
                compressed.length > TARGET_FILE_SIZE
                        && (
                        currentWidth > 1000
                                || currentHeight > 1000
                )
        ) {

            currentWidth =
                    Math.max(
                            1000,
                            (int) (currentWidth * 0.8)
                    );

            currentHeight =
                    Math.max(
                            1000,
                            (int) (currentHeight * 0.8)
                    );

            BufferedImage smaller =
                    resizeImage(
                            resized,
                            currentWidth,
                            currentHeight
                    );

            compressed =
                    compressAsJpeg(
                            smaller,
                            0.80f
                    );

            resized = smaller;
        }

        /*
         * Final safety check.
         */
        if (compressed.length > TARGET_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "Image could not be compressed below 8MB"
            );
        }

        return compressed;
    }

    private BufferedImage resizeImage(
            BufferedImage original
    ) {

        int width =
                original.getWidth();

        int height =
                original.getHeight();

        /*
         * Already small enough.
         */
        if (
                width <= MAX_IMAGE_DIMENSION
                        && height <= MAX_IMAGE_DIMENSION
        ) {

            return original;
        }

        double scale =
                Math.min(
                        (double) MAX_IMAGE_DIMENSION / width,
                        (double) MAX_IMAGE_DIMENSION / height
                );

        int newWidth =
                Math.max(
                        1,
                        (int) Math.round(width * scale)
                );

        int newHeight =
                Math.max(
                        1,
                        (int) Math.round(height * scale)
                );

        return resizeImage(
                original,
                newWidth,
                newHeight
        );
    }

    private BufferedImage resizeImage(
            BufferedImage original,
            int width,
            int height
    ) {

        BufferedImage resized =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics =
                resized.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics.drawImage(
                original,
                0,
                0,
                width,
                height,
                null
        );

        graphics.dispose();

        return resized;
    }

    private byte[] compressAsJpeg(
            BufferedImage image,
            float quality
    ) throws IOException {

        Iterator<ImageWriter> writers =
                ImageIO.getImageWritersByFormatName("jpg");

        if (!writers.hasNext()) {

            throw new IllegalStateException(
                    "JPEG ImageWriter is not available"
            );
        }

        ImageWriter writer =
                writers.next();

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        try (
                ImageOutputStream imageOutput =
                        ImageIO.createImageOutputStream(output)
        ) {

            writer.setOutput(imageOutput);

            ImageWriteParam params =
                    writer.getDefaultWriteParam();

            if (params.canWriteCompressed()) {

                params.setCompressionMode(
                        ImageWriteParam.MODE_EXPLICIT
                );

                params.setCompressionQuality(
                        quality
                );
            }

            writer.write(
                    null,
                    new IIOImage(
                            image,
                            null,
                            null
                    ),
                    params
            );

        } finally {

            writer.dispose();
        }

        return output.toByteArray();
    }

    private void validate(
            MultipartFile file
    ) {

        if (
                file == null
                        || file.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Payment screenshot is required"
            );
        }

        if (
                file.getSize()
                        > MAX_FILE_SIZE
        ) {

            throw new IllegalArgumentException(
                    "Payment screenshot must be smaller than 100MB"
            );
        }

        String contentType =
                file.getContentType();

        if (
                contentType == null
                        || !ALLOWED_TYPES.contains(
                        contentType.toLowerCase()
                )
        ) {

            throw new IllegalArgumentException(
                    "Only JPG, PNG and WEBP images are allowed"
            );
        }
    }
}