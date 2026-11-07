package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.recallforge.config.AppMarkdownLimitProperties;
import dev.recallforge.domain.ImageAsset;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.repository.ImageAssetRepository;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long TEMPLATE_USER_ID = 1L;

    @Mock
    private ImageAssetRepository imageAssetRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AppMarkdownLimitProperties markdownLimitProperties;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(imageAssetRepository, currentUserService, markdownLimitProperties);

        lenient().when(currentUserService.currentUserId()).thenReturn(USER_ID);
        lenient().when(currentUserService.currentUser()).thenReturn(new UserAccount());
        lenient().when(markdownLimitProperties.maxImagesPerUser()).thenReturn(500);
        lenient().when(markdownLimitProperties.maxImageStorageMbPerUser()).thenReturn(20);
        lenient().when(imageAssetRepository.findByUserIdAndFilename(any(), any())).thenReturn(Optional.empty());
    }

    private static byte[] encode(int width, int height, String format) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xFF0000);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private ImageAsset storeAndCapture(String filename, byte[] bytes) {
        imageService.store(filename, bytes);
        ArgumentCaptor<ImageAsset> captor = ArgumentCaptor.forClass(ImageAsset.class);
        verify(imageAssetRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void store_validPng_savesReencodedPngUnderBasename() throws Exception {
        ImageAsset saved = storeAndCapture("_images/Pasted image 1.png", encode(10, 5, "png"));

        assertThat(saved.getFilename()).isEqualTo("Pasted image 1.png");
        assertThat(saved.getContentType()).isEqualTo("image/png");
        assertThat(saved.getSizeBytes()).isEqualTo(saved.getData().length);
        assertThat(saved.getSha256()).hasSize(64);

        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(saved.getData()));
        assertThat(decoded.getWidth()).isEqualTo(10);
        assertThat(decoded.getHeight()).isEqualTo(5);
    }

    @Test
    void store_validJpeg_savedAsJpeg() throws Exception {
        ImageAsset saved = storeAndCapture("photo.jpg", encode(4, 4, "jpg"));

        assertThat(saved.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void store_contentTypeComesFromBytesNotExtension() throws Exception {
        // A real JPEG named .png is stored and served as a JPEG.
        ImageAsset saved = storeAndCapture("misnamed.png", encode(4, 4, "jpg"));

        assertThat(saved.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void store_trailingDataAfterImage_isDroppedByReencoding() throws Exception {
        byte[] marker = "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);
        byte[] polyglot = concat(encode(4, 4, "png"), marker);

        ImageAsset saved = storeAndCapture("poly.png", polyglot);

        String stored = new String(saved.getData(), StandardCharsets.ISO_8859_1);
        assertThat(stored).doesNotContain("<script>");
    }

    @Test
    void store_svgNamedPng_isRejected() {
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" onload=\"alert(1)\"/>"
            .getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> imageService.store("evil.png", svg))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File is not a valid PNG or JPEG image.");
        verify(imageAssetRepository, never()).save(any());
    }

    @Test
    void store_svgExtension_isRejected() throws Exception {
        assertThatThrownBy(() -> imageService.store("diagram.svg", encode(4, 4, "png")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Only .png, .jpg and .jpeg images are allowed.");
    }

    @Test
    void store_pngSignatureWithCorruptBody_isRejected() {
        byte[] corrupt = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 1, 2, 3};

        assertThatThrownBy(() -> imageService.store("broken.png", corrupt))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File is not a valid PNG or JPEG image.");
    }

    @Test
    void store_emptyBytes_isRejected() {
        assertThatThrownBy(() -> imageService.store("empty.png", new byte[0]))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Image is empty.");
    }

    @Test
    void store_largerThan2Mb_isRejectedBeforeDecoding() {
        byte[] big = new byte[(int) ImageService.MAX_IMAGE_SIZE + 1];

        assertThatThrownBy(() -> imageService.store("big.png", big))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Image is too large. Maximum size is 2 MB.");
    }

    @Test
    void store_dimensionsOverLimit_isRejected() throws Exception {
        // A 1-pixel-high strip compresses to a tiny file but is wider than allowed.
        byte[] wide = encode(ImageService.MAX_DIMENSION + 1, 1, "png");

        assertThatThrownBy(() -> imageService.store("wide.png", wide))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageStartingWith("Image dimensions are too large.");
    }

    @Test
    void store_filenameWithControlCharacter_isRejected() throws Exception {
        assertThatThrownBy(() -> imageService.store("bad\nname.png", encode(4, 4, "png")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid image filename.");
    }

    @Test
    void store_traversalPath_keepsOnlyBasename() throws Exception {
        ImageAsset saved = storeAndCapture("../../etc/x.png", encode(4, 4, "png"));

        assertThat(saved.getFilename()).isEqualTo("x.png");
    }

    @Test
    void store_sameBytesAlreadyStored_isNoOp() throws Exception {
        ImageAsset first = storeAndCapture("a.png", encode(4, 4, "png"));
        when(imageAssetRepository.findByUserIdAndFilename(USER_ID, "a.png")).thenReturn(Optional.of(first));

        imageService.store("a.png", encode(4, 4, "png"));

        verify(imageAssetRepository).save(any());           // still only the first save
        verify(imageAssetRepository).countByUserId(USER_ID); // quota checked only for the first
    }

    @Test
    void store_differentBytesForExistingName_replacesData() throws Exception {
        ImageAsset first = storeAndCapture("a.png", encode(4, 4, "png"));
        String firstSha = first.getSha256();
        when(imageAssetRepository.findByUserIdAndFilename(USER_ID, "a.png")).thenReturn(Optional.of(first));

        imageService.store("a.png", encode(8, 8, "png"));

        assertThat(first.getSha256()).isNotEqualTo(firstSha);
        verify(imageAssetRepository).save(any());  // updated in place, not saved twice
    }

    @Test
    void store_imageCountLimitReached_isRejected() throws Exception {
        when(imageAssetRepository.countByUserId(USER_ID)).thenReturn(500L);

        assertThatThrownBy(() -> imageService.store("a.png", encode(4, 4, "png")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Image limit reached. Maximum is 500 images.");
        verify(imageAssetRepository, never()).save(any());
    }

    @Test
    void store_storageLimitReached_isRejected() throws Exception {
        when(imageAssetRepository.sumSizeBytesByUserId(USER_ID)).thenReturn(20L * 1024 * 1024);

        assertThatThrownBy(() -> imageService.store("a.png", encode(4, 4, "png")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Image storage limit reached. Maximum is 20 MB.");
        verify(imageAssetRepository, never()).save(any());
    }

    @Test
    void find_ownImage_returnedWithoutTemplateLookup() throws Exception {
        ImageAsset own = new ImageAsset("a.png", "image/png", "sha", new byte[]{1}, new UserAccount());
        when(imageAssetRepository.findByUserIdAndFilename(USER_ID, "a.png")).thenReturn(Optional.of(own));

        assertThat(imageService.find("a.png")).containsSame(own);
        verify(imageAssetRepository, never()).findByUserIdAndFilename(TEMPLATE_USER_ID, "a.png");
    }

    @Test
    void find_missingOwnImage_fallsBackToTemplateUser() throws Exception {
        ImageAsset template = new ImageAsset("a.png", "image/png", "sha", new byte[]{1}, new UserAccount());
        when(imageAssetRepository.findByUserIdAndFilename(TEMPLATE_USER_ID, "a.png"))
            .thenReturn(Optional.of(template));

        assertThat(imageService.find("a.png")).containsSame(template);
    }

    @Test
    void find_missingEverywhere_isEmpty() {
        assertThat(imageService.find("nope.png")).isEmpty();
    }
}
