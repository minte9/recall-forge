package dev.recallforge.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * New file order for one subcategory of the markdown list: the complete set
 * of the current user's file ids in that category/subcategory, in the order
 * they should be displayed (and reviewed).
 */
public record MarkdownReorderRequest(
    @NotBlank
    String category,

    @NotBlank
    String subcategory,

    @NotEmpty
    List<Long> markdownFileIds
) {
}
