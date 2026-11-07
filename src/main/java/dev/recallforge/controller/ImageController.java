package dev.recallforge.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.domain.ImageAsset;
import dev.recallforge.service.ImageService;

/**
 * Serves images embedded in markdown ({@code ![[name.png]]}), resolved by
 * the frontend to {@code /api/images/{filename}}. Requires a logged-in user
 * (everything outside SecurityConfig's permitAll list does).
 *
 * The response headers make sure the bytes can only ever be treated as an
 * image, even if one somehow got past ImageService's checks: a fixed
 * Content-Type from the stored format, nosniff, and a sandboxing CSP for
 * anyone opening the URL directly.
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable String filename,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch
    ) {
        return imageService.find(filename)
                .map(image -> toResponse(image, ifNoneMatch))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<byte[]> toResponse(ImageAsset image, String ifNoneMatch) {
        String etag = "\"" + image.getSha256() + "\"";
        CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate();

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .cacheControl(cacheControl)
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .eTag(etag)
                .cacheControl(cacheControl)
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(image.getData());
    }
}
