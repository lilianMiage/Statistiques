package fr.miage.lroux.statistique.expo;

import fr.miage.lroux.statistique.entity.CarSnapshot;
import fr.miage.lroux.statistique.service.CarSnapshotService;
import fr.miage.lroux.statistique.transientObj.CarGlobalStatsDTO;
import fr.miage.lroux.statistique.transientObj.CarStatPerPeriodDTO;
import fr.miage.lroux.statistique.transientObj.CarStatPerPeriodPerCarDTO;
import fr.miage.lroux.statistique.transientObj.StationStatPerPeriodDTO;
import fr.miage.lroux.statistique.utilities.CarInconnuException;
import fr.miage.lroux.statistique.utilities.UserInconnuException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController()
@RequestMapping("/api/statistique/car")
public class CarStatistiqueController {
//    @GetMapping("/lastKmMaintenance/{id}")
//    public int getLastKmMaintenanceCar(@PathVariable Long id) throws CarInconnuException{
//        return 0;
//    }

    private final CarSnapshotService carSnapshotService;
    public CarStatistiqueController(CarSnapshotService carSnapshotService) {
        this.carSnapshotService = carSnapshotService;
    }

    @GetMapping("/hour")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParHeure() {
        List<CarStatPerPeriodDTO> stats = carSnapshotService.computeAverageCarStatsPerPeriodService("hour");
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/hour/{idCar}")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParHeureParVoiture(@PathVariable Long idCar) throws CarInconnuException {
        List<CarStatPerPeriodDTO> stats = carSnapshotService.computeCarStatsPerPeriodServiceByCarId("hour",idCar);
        if (stats.isEmpty()) {
            throw new CarInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/day")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParJour() {
        List<CarStatPerPeriodDTO> stats = carSnapshotService.computeAverageCarStatsPerPeriodService("day");
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/day/{idCar}")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParJourParVoiture(@PathVariable Long idCar) throws CarInconnuException {
        List<CarStatPerPeriodDTO> stats = carSnapshotService.computeCarStatsPerPeriodServiceByCarId("day",idCar);
        if (stats.isEmpty()) {
            throw new CarInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/month")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParMois() {
        List<CarStatPerPeriodDTO> stats = carSnapshotService.computeAverageCarStatsPerPeriodService("month");
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/month/{idCar}")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParMoisParVoiture(@PathVariable Long idCar) throws CarInconnuException {
        List<CarStatPerPeriodDTO> stats = carSnapshotService.computeCarStatsPerPeriodServiceByCarId("month",idCar);
        if (stats.isEmpty()) {
            throw new CarInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/year")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParAnnee() {
        List<CarStatPerPeriodDTO> stats = carSnapshotService.computeAverageCarStatsPerPeriodService("year");
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/year/{idCar}")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParAnneeParVoiture(@PathVariable Long idCar) throws CarInconnuException {
        List<CarStatPerPeriodDTO> stats = carSnapshotService.computeCarStatsPerPeriodServiceByCarId("year",idCar);
        if (stats.isEmpty()) {
            throw new CarInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/week")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParSemaine() {
        List<CarStatPerPeriodDTO> stats = carSnapshotService.computeAverageCarStatsPerPeriodService("week");
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/week/{idCar}")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParSemaineParVoiture(@PathVariable Long idCar) throws CarInconnuException {
        List<CarStatPerPeriodDTO> stats = carSnapshotService.computeCarStatsPerPeriodServiceByCarId("week",idCar);
        if (stats.isEmpty()) {
            throw new CarInconnuException();
        }
        return ResponseEntity.ok(stats);
    }

}