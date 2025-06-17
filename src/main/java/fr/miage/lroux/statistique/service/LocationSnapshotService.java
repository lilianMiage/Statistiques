package fr.miage.lroux.statistique.service;

import fr.miage.lroux.statistique.entity.LocationSnapshot;
import fr.miage.lroux.statistique.entity.StationSnapshot;
import fr.miage.lroux.statistique.entity.TimeGranularity;
import fr.miage.lroux.statistique.repo.CarSnapshotRepository;
import fr.miage.lroux.statistique.repo.LocationSnapshotRepository;
import fr.miage.lroux.statistique.transientObj.LocationStatPerPeriodDTO;
import fr.miage.lroux.statistique.utilities.Periode;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationSnapshotService {
    private final LocationSnapshotRepository repository;
    public LocationSnapshotService(LocationSnapshotRepository repository) {
        this.repository = repository;
    }
    public LocationSnapshot createSnapshot(LocationSnapshot snapshot) {
        snapshot.setTimestamp(Instant.now());
        return repository.save(snapshot);
    }

    public List<LocationSnapshot> getSnapshotsByLocationId(long locationId) {
        return repository.findByLocationIdOrderByTimestampDesc(locationId);
    }

    public List<LocationStatPerPeriodDTO> computeLocationStatsPerPeriodService(String period) {
        List<LocationSnapshot> allSnapshots = repository.findAll();
        TimeGranularity granularity = TimeGranularity.valueOf(period.toUpperCase());

        List<LocationStatPerPeriodDTO> stats = computeAverageLocationStatsPerPeriod(allSnapshots, granularity);
        Collections.reverse(stats); // plus récent en premier
        return stats;
    }

    public List<LocationStatPerPeriodDTO> computeLocationStatsPerPeriodByUser(Long userId, String period) {
        List<LocationSnapshot> allSnapshots = repository.findByUserId(userId);

        TimeGranularity granularity = TimeGranularity.valueOf(period.toUpperCase());

        List<LocationStatPerPeriodDTO> stats = computeAverageLocationStatsPerPeriod(allSnapshots, granularity);
        Collections.reverse(stats);
        return stats;
    }

    public List<LocationStatPerPeriodDTO> computeLocationStatsPerPeriodByCar(Long carId, String period) {
        List<LocationSnapshot> allSnapshots = repository.findByCarId(carId);

        TimeGranularity granularity = TimeGranularity.valueOf(period.toUpperCase());

        List<LocationStatPerPeriodDTO> stats = computeAverageLocationStatsPerPeriod(allSnapshots, granularity);
        Collections.reverse(stats);
        return stats;
    }

    public List<LocationStatPerPeriodDTO> computeAverageLocationStatsPerPeriod(
            List<LocationSnapshot> allSnapshots,
            TimeGranularity granularity
    ) {
        if (allSnapshots == null || allSnapshots.isEmpty()) return Collections.emptyList();

        ZonedDateTime now = ZonedDateTime.now();
        List<ZonedDateTime> periods = Periode.generatePeriodsAfterFirstSnapshot(
                allSnapshots,
                now,
                granularity,
                LocationSnapshot::getTimestamp
        );

        Map<Long, List<LocationSnapshot>> snapshotsByLocation = allSnapshots.stream()
                .collect(Collectors.groupingBy(LocationSnapshot::getLocationId));

        List<LocationStatPerPeriodDTO> results = new ArrayList<>();

        for (int i = 0; i < periods.size() - 1; i++) {
            ZonedDateTime periodStart = periods.get(i);
            ZonedDateTime periodEnd = periods.get(i + 1);

            double totalDistance = 0.0;
            int totalSnapshots = 0;

            for (Map.Entry<Long, List<LocationSnapshot>> entry : snapshotsByLocation.entrySet()) {
                List<LocationSnapshot> locationSnapshots = entry.getValue().stream()
                        .sorted(Comparator.comparing(LocationSnapshot::getTimestamp))
                        .toList();

                List<LocationSnapshot> snapshotsInPeriod = locationSnapshots.stream()
                        .filter(s -> {
                            Instant ts = s.getTimestamp();
                            return !ts.isBefore(periodStart.toInstant()) && ts.isBefore(periodEnd.toInstant());
                        })
                        .toList();

                // On ne compte que les distances si active == false
                double locationDistance = snapshotsInPeriod.stream()
                        .filter(s -> !s.isActive())
                        .mapToDouble(LocationSnapshot::getDistanceTravelledLocation)
                        .sum();

                if (!snapshotsInPeriod.isEmpty()) {
                    totalDistance += locationDistance;
                    totalSnapshots += snapshotsInPeriod.size();
                }
            }

            results.add(LocationStatPerPeriodDTO.builder()
                    .periodLabel(Periode.formatPeriodLabel(periodStart, granularity))
                    .totalDistanceTravelled(totalDistance)
                    .snapshotCount(totalSnapshots)
                    .build());
        }

        return results;
    }


}
