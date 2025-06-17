package fr.miage.lroux.statistique.controller;

import fr.miage.lroux.statistique.entity.CarSnapshot;
import fr.miage.lroux.statistique.entity.LocationSnapshot;
import fr.miage.lroux.statistique.service.CarSnapshotService;
import fr.miage.lroux.statistique.service.LocationSnapshotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/snapshots/location")
public class LocationSnapshotController {
    private final LocationSnapshotService service;

    public LocationSnapshotController(LocationSnapshotService service) {
        this.service = service;
    }
    @PostMapping("/")
    public LocationSnapshot createSnapshot(@RequestBody LocationSnapshot snapshot) {
        return service.createSnapshot(snapshot);
    }

    @GetMapping("/{locationId}")
    public List<LocationSnapshot> getSnapshotsByLocationId(@PathVariable long locationId) {
        return service.getSnapshotsByLocationId(locationId);
    }
}
