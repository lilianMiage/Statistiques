package fr.miage.lroux.statistique.controller;

import fr.miage.lroux.statistique.entity.CarSnapshot;
import fr.miage.lroux.statistique.service.CarSnapshotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/snapshots/car")
public class CarSnapshotController {
    private final CarSnapshotService service;

    public CarSnapshotController(CarSnapshotService service) {
        this.service = service;
    }
    @PostMapping("/")
    public CarSnapshot createSnapshot(@RequestBody CarSnapshot snapshot) {
        return service.createSnapshot(snapshot);
    }

    @GetMapping("/{carId}")
    public List<CarSnapshot> getSnapshotsByCarId(@PathVariable long carId) {
        return service.getSnapshotsByCarId(carId);
    }
}
