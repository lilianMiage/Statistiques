package fr.miage.lroux.statistique.expo;

import fr.miage.lroux.statistique.service.LocationSnapshotService;
import fr.miage.lroux.statistique.transientObj.LocationStatPerPeriodDTO;
import fr.miage.lroux.statistique.transientObj.StationStatPerPeriodDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/statistique/location")
public class LocationStatistiqueController {
    private final LocationSnapshotService locationSnapshotService;
    public LocationStatistiqueController(LocationSnapshotService locationSnapshotService) {
        this.locationSnapshotService = locationSnapshotService;
    }

    @GetMapping("/hour")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParHeure() {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodService("hour");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/hour/user/{userId}")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParHeureParUserId(@PathVariable("userId") Long userId) {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodByUser(userId,"hour");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/hour/car/{carId}")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParHeureParCarId(@PathVariable("carId") Long carId) {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodByCar(carId,"hour");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/day")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParJour() {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodService("day");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/day/user/{userId}")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParJourParUserId(@PathVariable("userId") Long userId) {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodByUser(userId,"day");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/day/car/{carId}")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParJourParCarId(@PathVariable("carId") Long carId) {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodByCar(carId,"day");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/month")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParMois() {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodService("month");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/month/user/{userId}")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParMoisParUserId(@PathVariable("userId") Long userId) {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodByUser(userId,"month");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/month/car/{carId}")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParMoisParCarId(@PathVariable("carId") Long carId) {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodByCar(carId,"month");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/week")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParSemaine() {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodService("week");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/week/user/{userId}")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParSemaineParUserId(@PathVariable("userId") Long userId) {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodByUser(userId,"week");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/week/car/{carId}")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParSemaineParCarId(@PathVariable("carId") Long carId) {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodByCar(carId,"week");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/year")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParAnnee() {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodService("year");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/year/user/{userId}")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParAnneeParUserId(@PathVariable("userId") Long userId) {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodByUser(userId,"year");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/year/car/{carId}")
    public ResponseEntity<List<LocationStatPerPeriodDTO>> getStatsParAnneeParCarId(@PathVariable("carId") Long carId) {
        List<LocationStatPerPeriodDTO> stats = locationSnapshotService.computeLocationStatsPerPeriodByCar(carId,"year");
        return ResponseEntity.ok(stats);
    }
}
