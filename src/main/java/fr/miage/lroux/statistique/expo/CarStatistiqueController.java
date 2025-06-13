package fr.miage.lroux.statistique.expo;

import fr.miage.lroux.statistique.entity.CarSnapshot;
import fr.miage.lroux.statistique.service.CarSnapshotService;
import fr.miage.lroux.statistique.transientObj.CarGlobalStatsDTO;
import fr.miage.lroux.statistique.transientObj.CarStatPerPeriodDTO;
import fr.miage.lroux.statistique.transientObj.CarStatPerPeriodPerCarDTO;
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
    @GetMapping("/lastKmMaintenance/{id}")
    public int getLastKmMaintenanceCar(@PathVariable Long id) throws CarInconnuException{
        return 0;
    }

    @GetMapping("/kmUser/{idCar},{idUser}")
    public int getKmUserCar(@PathVariable Long idCar, @PathVariable Long idUser) throws CarInconnuException, UserInconnuException {
        return 0;
    }

    private final CarSnapshotService carSnapshotService;
    public CarStatistiqueController(CarSnapshotService carSnapshotService) {
        this.carSnapshotService = carSnapshotService;
    }

    @GetMapping("/hour")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParHeure() {
        return ResponseEntity.ok(carSnapshotService.getStatsGroupedByPeriod("hour"));
    }
    @GetMapping("/hour/{idCar}")
    public ResponseEntity<List<CarStatPerPeriodPerCarDTO>> getStatsParHeureParVoiture(@PathVariable Long idCar) throws CarInconnuException {
        List<CarStatPerPeriodPerCarDTO> stats = carSnapshotService.getStatsGroupedByPeriodPerCarById("hour",idCar);
        if (stats.isEmpty()) {
            throw new CarInconnuException();
        }
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/day")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParJour() {
        return ResponseEntity.ok(carSnapshotService.getStatsGroupedByPeriod("day"));
    }
    @GetMapping("/day/{idCar}")
    public ResponseEntity<List<CarStatPerPeriodPerCarDTO>> getStatsParJourParVoiture(@PathVariable Long idCar) throws CarInconnuException {
        List<CarStatPerPeriodPerCarDTO> stats = carSnapshotService.getStatsGroupedByPeriodPerCarById("day",idCar);
        if (stats.isEmpty()) {
            throw new CarInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/month")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParMois() {
        return ResponseEntity.ok(carSnapshotService.getStatsGroupedByPeriod("month"));
    }
    @GetMapping("/month/{idCar}")
    public ResponseEntity<List<CarStatPerPeriodPerCarDTO>> getStatsParMoisParVoiture(@PathVariable Long idCar) throws CarInconnuException {
        List<CarStatPerPeriodPerCarDTO> stats = carSnapshotService.getStatsGroupedByPeriodPerCarById("month",idCar);
        if (stats.isEmpty()) {
            throw new CarInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/year")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParAnnee() {
        return ResponseEntity.ok(carSnapshotService.getStatsGroupedByPeriod("year"));
    }
    @GetMapping("/year/{idCar}")
    public ResponseEntity<List<CarStatPerPeriodPerCarDTO>> getStatsParAnneeParVoiture(@PathVariable Long idCar) throws CarInconnuException {
        List<CarStatPerPeriodPerCarDTO> stats = carSnapshotService.getStatsGroupedByPeriodPerCarById("year",idCar);
        if (stats.isEmpty()) {
            throw new CarInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/week")
    public ResponseEntity<List<CarStatPerPeriodDTO>> getStatsParSemaine() {
        return ResponseEntity.ok(carSnapshotService.getStatsGroupedByPeriod("week"));
    }
    @GetMapping("/week/{idCar}")
    public ResponseEntity<List<CarStatPerPeriodPerCarDTO>> getStatsParSemaineParVoiture(@PathVariable Long idCar) throws CarInconnuException {
        List<CarStatPerPeriodPerCarDTO> stats = carSnapshotService.getStatsGroupedByPeriodPerCarById("week",idCar);
        if (stats.isEmpty()) {
            throw new CarInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
}