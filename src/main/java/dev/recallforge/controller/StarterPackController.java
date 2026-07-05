package dev.recallforge.controller;

import dev.recallforge.service.StarterPackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/starter-packs")
public class StarterPackController {

    private final StarterPackService starterPackService;

    public StarterPackController(StarterPackService starterPackService) {
        this.starterPackService = starterPackService;
    }

    @PostMapping("/{category}/import")
    public ResponseEntity<Void> importStarterPack(@PathVariable String category) throws Exception {
        starterPackService.importStarterPack(category);
        return ResponseEntity.noContent().build();
    }
}