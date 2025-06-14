package fr.miage.lroux.statistique.expo;

import fr.miage.lroux.statistique.service.CarSnapshotService;
import fr.miage.lroux.statistique.service.StationSnapshotService;
import fr.miage.lroux.statistique.transientObj.CarStatPerPeriodDTO;
import fr.miage.lroux.statistique.transientObj.CarStatPerPeriodPerCarDTO;
import fr.miage.lroux.statistique.transientObj.StationStatPerPeriodDTO;
import fr.miage.lroux.statistique.transientObj.StationUsageDTO;
import fr.miage.lroux.statistique.utilities.CarInconnuException;
import fr.miage.lroux.statistique.utilities.StationInconnuException;
import fr.miage.lroux.statistique.utilities.UserInconnuException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController()
@RequestMapping("/api/statistique/station/")
public class StationStatistiqueController {

    private final StationSnapshotService stationSnapshotService;

    public StationStatistiqueController(StationSnapshotService stationSnapshotService) {
        this.stationSnapshotService = stationSnapshotService;
    }

    @GetMapping("/mostused/{seuil}")
    public List<StationUsageDTO> getMostUsedStations(@PathVariable double seuil) {
        return stationSnapshotService.getMostUsedStations(seuil);
    }

    @GetMapping("/lessused/{seuil}")
    public List<StationUsageDTO> getLessUsedStations(@PathVariable double seuil) {
        return stationSnapshotService.getLessUsedStations(seuil);
    }

    @GetMapping("/hour")
    public ResponseEntity<List<StationStatPerPeriodDTO>> getStatsParHeure2() {
        List<StationStatPerPeriodDTO> stats = stationSnapshotService.computeStationStatsPerPeriodService("hour");
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/hour/{idStation}")
    public ResponseEntity<List<StationStatPerPeriodDTO>> getStatsParHeureParStation2(@PathVariable Long idStation) throws StationInconnuException {
        List<StationStatPerPeriodDTO> stats = stationSnapshotService.computeStationStatsPerPeriodServiceByStationId("hour",idStation);
        if (stats.isEmpty()) {
            throw new StationInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/day")
    public ResponseEntity<List<StationStatPerPeriodDTO>> getStatsParHeure() {
        List<StationStatPerPeriodDTO> stats = stationSnapshotService.computeStationStatsPerPeriodService("day");
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/day/{idStation}")
    public ResponseEntity<List<StationStatPerPeriodDTO>> getStatsParHeureParStation(@PathVariable Long idStation) throws StationInconnuException {
        List<StationStatPerPeriodDTO> stats = stationSnapshotService.computeStationStatsPerPeriodServiceByStationId("day",idStation);
        if (stats.isEmpty()) {
            throw new StationInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/month")
    public ResponseEntity<List<StationStatPerPeriodDTO>> getStatsParMois() {
        List<StationStatPerPeriodDTO> stats = stationSnapshotService.computeStationStatsPerPeriodService("month");
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/month/{idStation}")
    public ResponseEntity<List<StationStatPerPeriodDTO>> getStatsParMoisParStation(@PathVariable Long idStation) throws StationInconnuException {
        List<StationStatPerPeriodDTO> stats = stationSnapshotService.computeStationStatsPerPeriodServiceByStationId("month",idStation);
        if (stats.isEmpty()) {
            throw new StationInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/year")
    public ResponseEntity<List<StationStatPerPeriodDTO>> getStatsParAnnee() {
        List<StationStatPerPeriodDTO> stats = stationSnapshotService.computeStationStatsPerPeriodService("year");
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/year/{idStation}")
    public ResponseEntity<List<StationStatPerPeriodDTO>> getStatsParAnneeParStation2(@PathVariable Long idStation) throws StationInconnuException {
        List<StationStatPerPeriodDTO> stats = stationSnapshotService.computeStationStatsPerPeriodServiceByStationId("year",idStation);
        if (stats.isEmpty()) {
            throw new StationInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/week")
    public ResponseEntity<List<StationStatPerPeriodDTO>> getStatsParSemaine() {
        List<StationStatPerPeriodDTO> stats = stationSnapshotService.computeStationStatsPerPeriodService("week");
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/week/{idStation}")
    public ResponseEntity<List<StationStatPerPeriodDTO>> getStatsParSemaineParStation2(@PathVariable Long idStation) throws StationInconnuException {
        List<StationStatPerPeriodDTO> stats = stationSnapshotService.computeStationStatsPerPeriodServiceByStationId("week",idStation);
        if (stats.isEmpty()) {
            throw new StationInconnuException();
        }
        return ResponseEntity.ok(stats);
    }
}