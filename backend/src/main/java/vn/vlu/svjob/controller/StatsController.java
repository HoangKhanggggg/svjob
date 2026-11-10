package vn.vlu.svjob.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vlu.svjob.dto.CommonDtos.Stats;
import vn.vlu.svjob.service.StatsService;

@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/api/stats")
    public Stats stats() {
        return statsService.overview();
    }
}
