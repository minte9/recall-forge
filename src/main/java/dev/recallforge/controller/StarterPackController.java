package dev.recallforge.controller;

import dev.recallforge.service.StarterPackService;
import java.util.Map;
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

    @GetMapping("/unlock-status")
    public ResponseEntity<Map<String, Boolean>> getUnlockStatus() {
        return ResponseEntity.ok(starterPackService.getUnlockStatus());
    }

    @GetMapping("/mastery")
    public ResponseEntity<Map<String, Integer>> getReviewedPercentages() {
        return ResponseEntity.ok(starterPackService.getReviewedPercentages());
    }

    @GetMapping("/due")
    public ResponseEntity<Map<String, Integer>> getDuePercentages() {
        return ResponseEntity.ok(starterPackService.getDuePercentages());
    }
}