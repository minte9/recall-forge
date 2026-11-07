package dev.recallforge.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.recallforge.config.AppMarkdownLimitProperties;
import dev.recallforge.domain.ImageAsset;
import dev.recallforge.repository.ImageAssetRepository;
import dev.recallforge.security.CurrentUserService;

/**
 * Stores and serves images embedded in Obsidian markdown
 * ({@code ![[name.png]]}).
 *
 * Every upload is treated as hostile:
 * - only PNG and JPEG are accepted, detected from the file's leading bytes
 *   (the filename extension and client Content-Type are never trusted) - no
 *   SVG, which can carry script;
 * - pixel dimensions are read from the header before decoding, so a small
 *   file that would expand to a huge bitmap is rejected cheaply;
 * - the image is decoded and re-encoded, and only the re-encoded bytes are
 *   stored, which drops EXIF/GPS metadata and anything appended to the
 *   original (polyglot files);
 * - per-user image count and total storage are capped.
 *
 * Lookups are scoped to the current user, falling back to the template
 * user's images so starter-pack copies (cloned from the template user)
 * still show theirs. No other user's images are ever reachable.
 */
@Service
public class ImageService {

    public static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024;  // 2 MB
    static final int MAX_DIMENSION = 8000;
    static final long MAX_PIXELS = 40_000_000L;

    private static final Long TEMPLATE_USER_ID = 1L;

    private static final byte[] PNG_SIGNATURE = {
        (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'
    };
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private final ImageAssetRepository imageAssetRepository;
    private final CurrentUserService currentUserService;
    private final AppMarkdownLimitProperties markdownLimitProperties;

    public ImageService(
            ImageAssetRepository imageAssetRepository,
            CurrentUserService currentUserService,
            AppMarkdownLimitProperties markdownLimitProperties
    ) {
        this.imageAssetRepository = imageAssetRepository;
        this.currentUserService = currentUserService;
        this.markdownLimitProperties = markdownLimitProperties;
    }

    /**
     * True for filenames the vault upload should route here rather than
     * skip. Only a routing hint - the actual format check is on the bytes.
     */
    public static boolean isImageFilename(String filename) {
        if (filename == null) {
            return false;
        }

        String lower = filename.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    /**
     * Validates, re-encodes and stores one image for the current user,
     * replacing any existing image with the same filename. Re-uploading
     * identical bytes is a no-op.
     *
     * @throws IllegalArgumentException with a user-facing message when the
     *         image is rejected. Always thrown before anything is written,
     *         and excluded from rollback: uploadVault calls this inside its
     *         own transaction and records the rejection as a per-file error,
     *         so it must not mark the whole vault import rollback-only.
     */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void store(String filename, byte[] bytes) {
        String safeFilename = validateFilename(filename);

        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Image is empty.");
        }

        if (bytes.length > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Image is too large. Maximum size is 2 MB.");
        }

        ImageFormat format = detectFormat(bytes);
        byte[] reencoded = reencode(bytes, format);
        String sha256 = sha256(reencoded);

        Long userId = currentUserService.currentUserId();
        Optional<ImageAsset> existing = imageAssetRepository.findByUserIdAndFilename(userId, safeFilename);

        if (existing.isPresent() && existing.get().getSha256().equals(sha256)) {
            return;
        }

        long existingSize = existing.map(ImageAsset::getSizeBytes).orElse(0L);
        ensureWithinQuota(userId, reencoded.length - existingSize, existing.isEmpty());

        if (existing.isPresent()) {
            existing.get().replaceData(format.contentType, sha256, reencoded);
            return;
        }

        imageAssetRepository.save(new ImageAsset(
                safeFilename, format.contentType, sha256, reencoded, currentUserService.currentUser()));
    }

    /**
     * The current user's image with this filename, else the template user's,
     * else empty.
     */
    @Transactional(readOnly = true)
    public Optional<ImageAsset> find(String filename) {
        if (filename == null || filename.isBlank()) {
            return Optional.empty();
        }

        Long userId = currentUserService.currentUserId();
        Optional<ImageAsset> own = imageAssetRepository.findByUserIdAndFilename(userId, filename);

        if (own.isPresent() || TEMPLATE_USER_ID.equals(userId)) {
            return own;
        }

        return imageAssetRepository.findByUserIdAndFilename(TEMPLATE_USER_ID, filename);
    }

    private void ensureWithinQuota(Long userId, long addedBytes, boolean isNewImage) {
        if (isNewImage && imageAssetRepository.countByUserId(userId) >= markdownLimitProperties.maxImagesPerUser()) {
            throw new IllegalArgumentException(
                    "Image limit reached. Maximum is " + markdownLimitProperties.maxImagesPerUser() + " images.");
        }

        long maxBytes = markdownLimitProperties.maxImageStorageMbPerUser() * 1024L * 1024L;

        if (imageAssetRepository.sumSizeBytesByUserId(userId) + addedBytes > maxBytes) {
            throw new IllegalArgumentException(
                    "Image storage limit reached. Maximum is "
                            + markdownLimitProperties.maxImageStorageMbPerUser() + " MB.");
        }
    }

    private String validateFilename(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Image filename is missing.");
        }

        // The client may send a path; only the last segment is the name
        // markdown embeds refer to.
        String normalized = filename.replace('\\', '/');
        String basename = normalized.substring(normalized.lastIndexOf('/') + 1).strip();

        if (basename.isEmpty() || basename.length() > 255 || basename.equals(".") || basename.equals("..")) {
            throw new IllegalArgumentException("Invalid image filename.");
        }

        for (char c : basename.toCharArray()) {
            if (Character.isISOControl(c)) {
                throw new IllegalArgumentException("Invalid image filename.");
            }
        }

        if (!isImageFilename(basename)) {
            throw new IllegalArgumentException("Only .png, .jpg and .jpeg images are allowed.");
        }

        return basename;
    }

    private ImageFormat detectFormat(byte[] bytes) {
        if (startsWith(bytes, PNG_SIGNATURE)) {
            return ImageFormat.PNG;
        }

        if (startsWith(bytes, JPEG_SIGNATURE)) {
            return ImageFormat.JPEG;
        }

        throw new IllegalArgumentException("File is not a valid PNG or JPEG image.");
    }

    private byte[] reencode(byte[] bytes, ImageFormat format) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(format.readerName);

            if (input == null || !readers.hasNext()) {
                throw new IllegalArgumentException("File is not a valid PNG or JPEG image.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(input, true, true);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0 || width > MAX_DIMENSION || height > MAX_DIMENSION
                        || (long) width * height > MAX_PIXELS) {
                    throw new IllegalArgumentException(
                            "Image dimensions are too large. Maximum is "
                                    + MAX_DIMENSION + "x" + MAX_DIMENSION + " pixels.");
                }

                BufferedImage image = reader.read(0);

                if (format == ImageFormat.JPEG && image.getColorModel().hasAlpha()) {
                    throw new IllegalArgumentException("File is not a valid PNG or JPEG image.");
                }

                ByteArrayOutputStream output = new ByteArrayOutputStream();

                if (!ImageIO.write(image, format.writerName, output)) {
                    throw new IllegalArgumentException("Unsupported image encoding.");
                }

                return output.toByteArray();

            } finally {
                reader.dispose();
            }

        } catch (IOException | RuntimeException e) {
            if (e instanceof IllegalArgumentException iae) {
                throw iae;
            }
            // Corrupt or unsupported data (e.g. CMYK JPEG) - surface as a
            // per-file error rather than a 500.
            throw new IllegalArgumentException("File is not a valid PNG or JPEG image.");
        }
    }

    private static boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }

        return true;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private enum ImageFormat {
        PNG("png", "png", "image/png"),
        JPEG("jpeg", "jpg", "image/jpeg");

        private final String readerName;
        private final String writerName;
        private final String contentType;

        ImageFormat(String readerName, String writerName, String contentType) {
            this.readerName = readerName;
            this.writerName = writerName;
            this.contentType = contentType;
        }
    }
}
