package fr.miage.lroux.statistique.service;

import fr.miage.lroux.statistique.entity.CarSnapshot;
import fr.miage.lroux.statistique.entity.TimeGranularity;
import fr.miage.lroux.statistique.repo.CarSnapshotRepository;
import fr.miage.lroux.statistique.transientObj.CarGlobalStatsDTO;
import fr.miage.lroux.statistique.transientObj.CarStatPerPeriodDTO;
import fr.miage.lroux.statistique.transientObj.CarStatPerPeriodPerCarDTO;
import fr.miage.lroux.statistique.utilities.Periode;
import fr.miage.lroux.statistique.utilities.YearWeek;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarSnapshotService {
    private final CarSnapshotRepository repository;
    public CarSnapshotService(CarSnapshotRepository repository) {
        this.repository = repository;
    }

    public CarSnapshot createSnapshot(CarSnapshot snapshot) {
        snapshot.setTimestamp(Instant.now());
        return repository.save(snapshot);
    }

    public List<CarSnapshot> getSnapshotsByCarId(long carId) {
        return repository.findByCarIdOrderByTimestampDesc(carId);
    }

    public double getUsedPercentage(
            List<CarSnapshot> snapshots,
            boolean initialUsed,
            ZonedDateTime start,
            ZonedDateTime end
    ) {
        if (!start.isBefore(end)) return 0.0;

        Instant periodStart = start.toInstant();
        Instant periodEnd = end.toInstant();

        if (snapshots.isEmpty()) {
            // S'il n'y a aucun snapshot, alors tout dépend de l'état initial
            return initialUsed ? 1.0 : 0.0;
        }

        double usedMillis = 0.0;
        Instant lastTime = periodStart;
        boolean currentUsed = initialUsed;

        for (CarSnapshot snapshot : snapshots) {
            Instant ts = snapshot.getTimestamp();
            if (ts.isBefore(periodStart)) continue;
            if (!ts.isBefore(periodEnd)) break;

            long delta = Duration.between(lastTime, ts).toMillis();
            if (currentUsed) usedMillis += delta;

            currentUsed = snapshot.isUsed();
            lastTime = ts;
        }

        if (lastTime.isBefore(periodEnd) && currentUsed) {
            usedMillis += Duration.between(lastTime, periodEnd).toMillis();
        }

        long totalMillis = Duration.between(periodStart, periodEnd).toMillis();
        return totalMillis > 0 ? usedMillis / totalMillis : 0.0;
    }

    public List<CarStatPerPeriodDTO> computeAverageCarStatsPerPeriodService(String period) {
        List<CarSnapshot> allSnapshots = repository.findAll();
        TimeGranularity granularity = TimeGranularity.valueOf(period.toUpperCase());

        List<CarStatPerPeriodDTO> stats = computeAverageCarStatsPerPeriod(allSnapshots, granularity);
        Collections.reverse(stats); // pour avoir la plus récente en premier
        return stats;
    }
    public List<CarStatPerPeriodDTO> computeCarStatsPerPeriodServiceByCarId(String period, Long carId) {
        List<CarSnapshot> allSnapshots = repository.findAll(); // à adapter si besoin
        TimeGranularity granularity = TimeGranularity.valueOf(period.toUpperCase());

        List<CarStatPerPeriodDTO> stats = computeCarStatsPerPeriod(allSnapshots, carId, granularity);
        Collections.reverse(stats); // pour avoir la plus récente en premier
        return stats;
    }
    public List<CarStatPerPeriodDTO> computeAverageCarStatsPerPeriod(
            List<CarSnapshot> allSnapshots,
            TimeGranularity granularity
    ) {
        if (allSnapshots == null || allSnapshots.isEmpty()) return Collections.emptyList();

        ZonedDateTime now = ZonedDateTime.now();
        List<ZonedDateTime> periods = Periode.generatePeriodsAfterFirstSnapshot(allSnapshots, now, granularity,CarSnapshot::getTimestamp);

        Map<Long, List<CarSnapshot>> snapshotsByCar = allSnapshots.stream()
                .collect(Collectors.groupingBy(CarSnapshot::getCarId));

        List<CarStatPerPeriodDTO> results = new ArrayList<>();

        for (int i = 0; i < periods.size() - 1; i++) {
            ZonedDateTime periodStart = periods.get(i);
            ZonedDateTime periodEnd = periods.get(i + 1);

            double totalUsedRatio = 0.0;
            double totalKilometres = 0.0;
            int carCount = 0;
            int carCountForKilometres = 0;
            int totalSnapshots = 0;

            for (Map.Entry<Long, List<CarSnapshot>> carEntry : snapshotsByCar.entrySet()) {
                long carId = carEntry.getKey();
                List<CarSnapshot> snapshotsForCar = carEntry.getValue().stream()
                        .sorted(Comparator.comparing(CarSnapshot::getTimestamp))
                        .toList();

                // Snapshot avant la période pour l’état initial
                Optional<CarSnapshot> previousSnapshotOpt = allSnapshots.stream()
                        .filter(s -> s.getCarId() == carId && s.getTimestamp().isBefore(periodStart.toInstant()))
                        .max(Comparator.comparing(CarSnapshot::getTimestamp));

                boolean initialUsed;
                ZonedDateTime adjustedStart = periodStart;

                if (previousSnapshotOpt.isPresent()) {
                    initialUsed = previousSnapshotOpt.get().isUsed();
                } else {
                    initialUsed = true; // par défaut
                    Optional<ZonedDateTime> firstSnapshotDateOpt = snapshotsForCar.stream()
                            .map(CarSnapshot::getTimestamp)
                            .filter(ts -> !ts.isBefore(periodStart.toInstant()))
                            .map(ts -> ZonedDateTime.ofInstant(ts, periodStart.getZone()))
                            .min(Comparator.naturalOrder());
                    if (firstSnapshotDateOpt.isPresent()) {
                        adjustedStart = firstSnapshotDateOpt.get();
                    }
                }

                double usedRatio = getUsedPercentage(snapshotsForCar, initialUsed, adjustedStart, periodEnd);

                ZonedDateTime finalAdjustedStart = adjustedStart; // ← maintenant c’est "final"

                List<CarSnapshot> snapshotsInPeriod = snapshotsForCar.stream()
                        .filter(s -> {
                            Instant ts = s.getTimestamp();
                            return !ts.isBefore(periodStart.toInstant()) && ts.isBefore(periodEnd.toInstant());
                        })
                        .toList();

                Optional<CarSnapshot> lastSnapshotInPeriodOpt = snapshotsInPeriod.stream()
                        .max(Comparator.comparing(CarSnapshot::getTimestamp));

                double travelledKm = 0.0;

                if (lastSnapshotInPeriodOpt.isPresent()) {
                    CarSnapshot lastSnapshot = lastSnapshotInPeriodOpt.get();

                    // Chercher snapshot AVANT la période
                    Optional<CarSnapshot> snapshotBeforePeriodOpt = snapshotsForCar.stream()
                            .filter(s -> s.getTimestamp().isBefore(periodStart.toInstant()))
                            .max(Comparator.comparing(CarSnapshot::getTimestamp));

                    double kmStart;
                    if (snapshotBeforePeriodOpt.isPresent()) {
                        kmStart = snapshotBeforePeriodOpt.get().getKilometresTravelled();
                    } else {
                        // Sinon, snapshot le plus ancien dans la période
                        Optional<CarSnapshot> firstInPeriodOpt = snapshotsInPeriod.stream()
                                .min(Comparator.comparing(CarSnapshot::getTimestamp));
                        kmStart = firstInPeriodOpt.map(CarSnapshot::getKilometresTravelled).orElse(lastSnapshot.getKilometresTravelled());
                    }

                    travelledKm = lastSnapshot.getKilometresTravelled() - kmStart;
                }

                int snapshotCount = (int) snapshotsForCar.stream()
                        .filter(s -> {
                            Instant ts = s.getTimestamp();
                            return !ts.isBefore(finalAdjustedStart.toInstant()) && ts.isBefore(periodEnd.toInstant());
                        }).count();

                boolean hasPreviousSnapshot = previousSnapshotOpt.isPresent();

                boolean hasSnapshotInPeriod = snapshotsForCar.stream().anyMatch(s -> {
                    Instant ts = s.getTimestamp();
                    return !ts.isBefore(finalAdjustedStart.toInstant()) && ts.isBefore(periodEnd.toInstant());
                });

                if (hasPreviousSnapshot || hasSnapshotInPeriod) {
                    totalUsedRatio += usedRatio;
                    totalKilometres += travelledKm;
                    totalSnapshots += snapshotCount;
                    carCount++;


                }
                if(hasSnapshotInPeriod){
                    carCountForKilometres++;
                }
            }

            double avgUsed = carCount > 0 ? totalUsedRatio / carCount : 0.0;
            double avgKm = carCountForKilometres > 0 ? totalKilometres / carCountForKilometres : 0.0;

            results.add(CarStatPerPeriodDTO.builder()
                    .periodLabel(Periode.formatPeriodLabel(periodStart, granularity))
                    .usedPercentage(avgUsed * 100)
                    .averageKilometresTravelled(avgKm)
                    .snapshotCount(totalSnapshots)
                    .build());
        }

        return results;
    }

    public List<CarStatPerPeriodDTO> computeCarStatsPerPeriod(
            List<CarSnapshot> allSnapshots,
            Long carId,
            TimeGranularity granularity
    ) {
        if (allSnapshots == null || allSnapshots.isEmpty()) return Collections.emptyList();

        // Ne garder que les snapshots de cette voiture
        List<CarSnapshot> snapshotsForCar = allSnapshots.stream()
                .filter(s -> s.getCarId() == carId)
                .sorted(Comparator.comparing(CarSnapshot::getTimestamp))
                .toList();

        if (snapshotsForCar.isEmpty()) return Collections.emptyList();

        ZonedDateTime now = ZonedDateTime.now();

        List<ZonedDateTime> periods = Periode.generatePeriodsAfterFirstSnapshot(
                snapshotsForCar,
                now,
                granularity,
                CarSnapshot::getTimestamp
        );

        List<CarStatPerPeriodDTO> results = new ArrayList<>();

        for (int i = 0; i < periods.size() - 1; i++) {
            ZonedDateTime periodStart = periods.get(i);
            ZonedDateTime periodEnd = periods.get(i + 1);

            // Snapshot avant la période pour l’état initial
            Optional<CarSnapshot> previousSnapshotOpt = allSnapshots.stream()
                    .filter(s -> s.getCarId() == carId && s.getTimestamp().isBefore(periodStart.toInstant()))
                    .max(Comparator.comparing(CarSnapshot::getTimestamp));

            boolean initialUsed;
            ZonedDateTime adjustedStart = periodStart;

            if (previousSnapshotOpt.isPresent()) {
                initialUsed = previousSnapshotOpt.get().isUsed();
            } else {
                initialUsed = true; // par défaut
                Optional<ZonedDateTime> firstSnapshotDateOpt = snapshotsForCar.stream()
                        .map(CarSnapshot::getTimestamp)
                        .filter(ts -> !ts.isBefore(periodStart.toInstant()))
                        .map(ts -> ZonedDateTime.ofInstant(ts, periodStart.getZone()))
                        .min(Comparator.naturalOrder());

                if (firstSnapshotDateOpt.isPresent()) {
                    adjustedStart = firstSnapshotDateOpt.get();
                }
            }

            double usedRatio = getUsedPercentage(snapshotsForCar, initialUsed, adjustedStart, periodEnd);

            ZonedDateTime finalAdjustedStart = adjustedStart;

            List<CarSnapshot> filtered = snapshotsForCar.stream()
                    .filter(s -> {
                        Instant ts = s.getTimestamp();
                        return !ts.isBefore(finalAdjustedStart.toInstant()) && ts.isBefore(periodEnd.toInstant());
                    })
                    .toList();

            List<CarSnapshot> snapshotsInPeriod = snapshotsForCar.stream()
                    .filter(s -> {
                        Instant ts = s.getTimestamp();
                        return !ts.isBefore(periodStart.toInstant()) && ts.isBefore(periodEnd.toInstant());
                    })
                    .toList();

            Optional<CarSnapshot> lastSnapshotInPeriodOpt = snapshotsInPeriod.stream()
                    .max(Comparator.comparing(CarSnapshot::getTimestamp));

            double travelledKm = 0.0;

            if (lastSnapshotInPeriodOpt.isPresent()) {
                CarSnapshot lastSnapshot = lastSnapshotInPeriodOpt.get();

                // Chercher snapshot AVANT la période
                Optional<CarSnapshot> snapshotBeforePeriodOpt = snapshotsForCar.stream()
                        .filter(s -> s.getTimestamp().isBefore(periodStart.toInstant()))
                        .max(Comparator.comparing(CarSnapshot::getTimestamp));

                double kmStart;
                if (snapshotBeforePeriodOpt.isPresent()) {
                    kmStart = snapshotBeforePeriodOpt.get().getKilometresTravelled();
                } else {
                    // Sinon, snapshot le plus ancien dans la période
                    Optional<CarSnapshot> firstInPeriodOpt = snapshotsInPeriod.stream()
                            .min(Comparator.comparing(CarSnapshot::getTimestamp));
                    kmStart = firstInPeriodOpt.map(CarSnapshot::getKilometresTravelled).orElse(lastSnapshot.getKilometresTravelled());
                }

                travelledKm = lastSnapshot.getKilometresTravelled() - kmStart;
            }


            int snapshotCount = filtered.size();

            results.add(CarStatPerPeriodDTO.builder()
                    .periodLabel(Periode.formatPeriodLabel(periodStart, granularity))
                    .usedPercentage(usedRatio * 100)
                    .averageKilometresTravelled(travelledKm)
                    .snapshotCount(snapshotCount)
                    .build());
        }

        return results;
    }
}
