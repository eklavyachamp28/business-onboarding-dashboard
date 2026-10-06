package com.aayusheklavya.onboarding.api;

import com.aayusheklavya.onboarding.service.NaicsCode;
import com.aayusheklavya.onboarding.service.NaicsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/naics")
public class NaicsController {

    private final NaicsService naics;

    public NaicsController(NaicsService naics) {
        this.naics = naics;
    }

    @GetMapping
    public List<NaicsCode> search(@RequestParam(name = "q", required = false) String query,
                                  @RequestParam(defaultValue = "10") int limit) {
        return naics.search(query, Math.min(Math.max(limit, 1), 50));
    }
}
