package fr.miage.lroux.statistique.controller;

import fr.miage.lroux.statistique.entity.StationSnapshot;
import fr.miage.lroux.statistique.service.StationSnapshotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/snapshots/station")
public class StationSnapshotController {
    private final StationSnapshotService service;

    public StationSnapshotController(StationSnapshotService service) {
        this.service = service;
    }
    @PostMapping("/")
    public StationSnapshot createSnapshot(@RequestBody StationSnapshot snapshot) {
        return service.createSnapshot(snapshot);
    }

    @GetMapping("/{stationId}")
    public List<StationSnapshot> getSnapshotsByStationId(@PathVariable long stationId) {
        return service.getSnapshotsByStationId(stationId);
    }
}