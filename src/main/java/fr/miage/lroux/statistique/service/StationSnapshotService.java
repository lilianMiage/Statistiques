package fr.miage.lroux.statistique.service;

import fr.miage.lroux.statistique.entity.CarSnapshot;
import fr.miage.lroux.statistique.entity.StationSnapshot;
import fr.miage.lroux.statistique.entity.TimeGranularity;
import fr.miage.lroux.statistique.repo.StationSnapshotRepository;
import fr.miage.lroux.statistique.transientObj.StationStatPerPeriodDTO;
import fr.miage.lroux.statistique.transientObj.StationUsageDTO;
import fr.miage.lroux.statistique.utilities.Periode;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StationSnapshotService {
    private final StationSnapshotRepository repository;
    private final Periode periode = new Periode();
    public StationSnapshotService(StationSnapshotRepository repository) {
        this.repository = repository;
    }

    public StationSnapshot createSnapshot(StationSnapshot snapshot) {
        snapshot.setTimestamp(Instant.now());
        return repository.save(snapshot);
    }

    public List<StationSnapshot> getSnapshotsByStationId(long carId) {
        return repository.findByStationIdOrderByTimestampDesc(carId);
    }

    //methode de stat
    public double getOccupationRate(List<StationSnapshot> snapshots, ZonedDateTime periodStart, ZonedDateTime periodEnd) {
        Instant start = periodStart.toInstant();
        Instant end = periodEnd.toInstant();
        long totalTimeSeconds = Duration.between(start, end).getSeconds();

        if (totalTimeSeconds <= 0) return 0.0;

        // Trier les snapshots par timestamp
        snapshots = snapshots.stream()
                .sorted(Comparator.comparing(StationSnapshot::getTimestamp))
                .toList();

        Instant currentTime = start;
        double totalOccupiedRatioTime = 0.0;

        // Déterminer l'état initial à t0 (avant la période)
        double previousRatio = snapshots.stream()
                .filter(s -> s.getTimestamp().isBefore(start))
                .max(Comparator.comparing(StationSnapshot::getTimestamp))
                .map(s -> s.getNbPlaces() > 0 ? (double) s.getNbPlacesTaken() / s.getNbPlaces() : 0.0)
                .orElse(0.0);

        for (StationSnapshot snapshot : snapshots) {
            Instant snapshotTime = snapshot.getTimestamp();

            // Si snapshot avant la période, ignorer
            if (snapshotTime.isBefore(start)) continue;

            // Si snapshot après la fin, on sort
            if (snapshotTime.isAfter(end)) break;

            long duration = Duration.between(currentTime, snapshotTime).getSeconds();
            totalOccupiedRatioTime += duration * previousRatio;

            // Mettre à jour ratio pour le prochain intervalle
            previousRatio = snapshot.getNbPlaces() > 0 ? (double) snapshot.getNbPlacesTaken() / snapshot.getNbPlaces() : 0.0;
            currentTime = snapshotTime;
        }

        // Dernier segment : de currentTime jusqu'à la fin de la période
        if (currentTime.isBefore(end)) {
            long duration = Duration.between(currentTime, end).getSeconds();
            totalOccupiedRatioTime += duration * previousRatio;
        }

        return totalOccupiedRatioTime / totalTimeSeconds;
    }

    //pour alerte avec seuil
    public List<StationUsageDTO> getMostUsedStations(double thresholdPercent) {
        return getStationsByUsageThreshold(thresholdPercent, true);
    }

    public List<StationUsageDTO> getLessUsedStations(double thresholdPercent) {
        return getStationsByUsageThreshold(thresholdPercent, false);
    }

    private List<StationUsageDTO> getStationsByUsageThreshold(double thresholdPercent, boolean moreThan) {
        Instant now = Instant.now();
        Instant weekAgo = now.minus(7, ChronoUnit.DAYS);

        List<StationSnapshot> snapshots = repository.findByTimestampBetween(weekAgo.minus(30, ChronoUnit.DAYS), now);
        // 30 jours de marge pour couvrir les stations récentes

        // Grouper les snapshots par station
        Map<Long, List<StationSnapshot>> snapshotsByStation = snapshots.stream()
                .collect(Collectors.groupingBy(StationSnapshot::getStationId));

        List<StationUsageDTO> result = new ArrayList<>();

        for (Map.Entry<Long, List<StationSnapshot>> entry : snapshotsByStation.entrySet()) {
            Long stationId = entry.getKey();
            List<StationSnapshot> stationSnaps = entry.getValue();

            // Trouver la date du premier snapshot
            Instant firstSeen = stationSnaps.stream()
                    .map(StationSnapshot::getTimestamp)
                    .min(Instant::compareTo)
                    .orElse(weekAgo);

            // Prendre le max entre "il y a 7 jours" et la date de création
            Instant start = firstSeen.isAfter(weekAgo) ? firstSeen : weekAgo;

            double avgOccupation = computeOccupationRateOverTime(stationSnaps, start, now) * 100;

            if ((moreThan && avgOccupation > thresholdPercent) || (!moreThan && avgOccupation < thresholdPercent)) {
                result.add(new StationUsageDTO(stationId, avgOccupation));
            }
        }

        return result;
    }


    private double computeOccupationRateOverTime(List<StationSnapshot> snapshots, Instant start, Instant end) {
        snapshots = snapshots.stream()
                .sorted(Comparator.comparing(StationSnapshot::getTimestamp))
                .toList();

        Instant currentTime = start;
        long totalTimeSeconds = Duration.between(start, end).getSeconds();

        if (totalTimeSeconds <= 0) return 0.0;

        double totalOccupiedRatioTime = 0.0;

        // État initial
        double previousRatio = snapshots.stream()
                .filter(s -> s.getTimestamp().isBefore(start))
                .max(Comparator.comparing(StationSnapshot::getTimestamp))
                .map(s -> s.getNbPlaces() > 0 ? (double) s.getNbPlacesTaken() / s.getNbPlaces() : 0.0)
                .orElse(0.0);

        for (StationSnapshot snap : snapshots) {
            Instant snapTime = snap.getTimestamp();
            if (snapTime.isBefore(start)) continue;
            if (snapTime.isAfter(end)) break;

            long duration = Duration.between(currentTime, snapTime).getSeconds();
            totalOccupiedRatioTime += duration * previousRatio;

            previousRatio = snap.getNbPlaces() > 0 ? (double) snap.getNbPlacesTaken() / snap.getNbPlaces() : 0.0;
            currentTime = snapTime;
        }

        if (currentTime.isBefore(end)) {
            long duration = Duration.between(currentTime, end).getSeconds();
            totalOccupiedRatioTime += duration * previousRatio;
        }

        return totalOccupiedRatioTime / totalTimeSeconds;
    }

    //Station stat
    public List<StationStatPerPeriodDTO> computeStationStatsPerPeriodService(String period) {
        List<StationSnapshot> allSnapshots = repository.findAll();
        TimeGranularity granularity = TimeGranularity.valueOf(period.toUpperCase());

        List<StationStatPerPeriodDTO> stats = computeAverageOccupationStatsPerPeriod(
                allSnapshots,
                granularity
        );

        // Trier du plus récent au plus ancien si besoin
        Collections.reverse(stats);
        return stats;
    }
    public List<StationStatPerPeriodDTO> computeStationStatsPerPeriodServiceByStationId(String period, Long stationId) {
        List<StationSnapshot> allSnapshots = repository.findAll(); // à adapter à ton repository
        TimeGranularity granularity = TimeGranularity.valueOf(period.toUpperCase());
        List<StationStatPerPeriodDTO> reversed = computeStationStatsPerPeriodByStationId(
                allSnapshots,
                stationId,
                granularity
        );
        Collections.reverse(reversed);

        return reversed;
    }

    public List<StationStatPerPeriodDTO> computeAverageOccupationStatsPerPeriod(
            List<StationSnapshot> allSnapshots,
            TimeGranularity granularity
    ) {
        if (allSnapshots == null || allSnapshots.isEmpty()) return Collections.emptyList();

        ZonedDateTime now = ZonedDateTime.now();
        List<ZonedDateTime> periods = Periode.generatePeriodsAfterFirstSnapshot(allSnapshots, now, granularity, StationSnapshot::getTimestamp);

        Map<Long, List<StationSnapshot>> snapshotsByStation = allSnapshots.stream()
                .collect(Collectors.groupingBy(StationSnapshot::getStationId));

        List<StationStatPerPeriodDTO> results = new ArrayList<>();

        for (int i = 0; i < periods.size() - 1; i++) {
            ZonedDateTime periodStart = periods.get(i);
            ZonedDateTime periodEnd = periods.get(i + 1);

            double totalRate = 0.0;
            int stationCount = 0;
            int totalSnapshots = 0;

            for (Map.Entry<Long, List<StationSnapshot>> entry : snapshotsByStation.entrySet()) {
                long stationId = entry.getKey();
                List<StationSnapshot> stationSnapshots = entry.getValue().stream()
                        .sorted(Comparator.comparing(StationSnapshot::getTimestamp))
                        .toList();

                ZonedDateTime adjustedStart = adjustPeriodStartWithFallback(
                        stationSnapshots,
                        allSnapshots,
                        stationId,
                        periodStart
                );

                // Snapshots dans cette période pour cette station
                List<StationSnapshot> snapshotsInPeriod = stationSnapshots.stream()
                        .filter(s -> {
                            Instant ts = s.getTimestamp();
                            return !ts.isBefore(adjustedStart.toInstant()) && ts.isBefore(periodEnd.toInstant());
                        })
                        .toList();

                double rate = getOccupationRate(stationSnapshots, adjustedStart, periodEnd);

                boolean hasPreviousSnapshot = allSnapshots.stream()
                        .anyMatch(s -> s.getStationId() == stationId && s.getTimestamp().isBefore(periodStart.toInstant()));

                boolean hasSnapshotInPeriod = stationSnapshots.stream()
                        .anyMatch(s -> {
                            Instant ts = s.getTimestamp();
                            return !ts.isBefore(adjustedStart.toInstant()) && ts.isBefore(periodEnd.toInstant());
                        });

                if (hasPreviousSnapshot || hasSnapshotInPeriod) {
                    totalRate += rate;
                    totalSnapshots += snapshotsInPeriod.size();
                    stationCount++;
                }
            }

            double averageRate = stationCount > 0 ? totalRate / stationCount : 0.0;

            results.add(StationStatPerPeriodDTO.builder()
                    .periodLabel(Periode.formatPeriodLabel(periodStart, granularity))
                    .occupationRate(averageRate * 100)
                    .snapshotCount(totalSnapshots)
                    .build());
        }

        return results;
    }

    public List<StationStatPerPeriodDTO> computeStationStatsPerPeriodByStationId(
            List<StationSnapshot> allSnapshots,
            long stationId,
            TimeGranularity granularity
    ) {
        if (allSnapshots == null || allSnapshots.isEmpty()) return Collections.emptyList();



        // Filtrer les snapshots pour la station
        List<StationSnapshot> stationSnapshots = allSnapshots.stream()
                .filter(s -> s.getStationId() == stationId)
                .sorted(Comparator.comparing(StationSnapshot::getTimestamp))
                .toList();

        if (stationSnapshots.isEmpty()) return Collections.emptyList();
        ZonedDateTime now = ZonedDateTime.now();
        List<ZonedDateTime> periods = Periode.generatePeriodsAfterFirstSnapshot(stationSnapshots, now, granularity, StationSnapshot::getTimestamp);


        List<StationStatPerPeriodDTO> result = new ArrayList<>();

        for (int i = 0; i < periods.size() - 1; i++) {
            ZonedDateTime periodStart = periods.get(i);
            ZonedDateTime periodEnd = periods.get(i + 1);

            ZonedDateTime adjustedStart = adjustPeriodStartWithFallback(
                    stationSnapshots,
                    allSnapshots,
                    stationId,
                    periodStart
            );

            // Snapshots dans cette période
            List<StationSnapshot> snapshotsInPeriod = stationSnapshots.stream()
                    .filter(s -> {
                        Instant ts = s.getTimestamp();
                        return !ts.isBefore(adjustedStart.toInstant()) && ts.isBefore(periodEnd.toInstant());
                    })
                    .toList();

            double rate = getOccupationRate(stationSnapshots, adjustedStart, periodEnd);

            result.add(StationStatPerPeriodDTO.builder()
                    .periodLabel(Periode.formatPeriodLabel(periodStart, granularity))
                    .occupationRate(rate * 100) // en %
                    .snapshotCount(snapshotsInPeriod.size())
                    .build());
        }

        return result;
    }

    private ZonedDateTime adjustPeriodStartWithFallback(
            List<StationSnapshot> stationSnapshots,
            List<StationSnapshot> allSnapshots,
            long stationId,
            ZonedDateTime globalPeriodStart
    ) {
        Optional<StationSnapshot> previousSnapshotOpt = allSnapshots.stream()
                .filter(s -> s.getStationId() == stationId && s.getTimestamp().isBefore(globalPeriodStart.toInstant()))
                .max(Comparator.comparing(StationSnapshot::getTimestamp));

        if (previousSnapshotOpt.isPresent()) {
            return globalPeriodStart;
        }

        return stationSnapshots.stream()
                .map(StationSnapshot::getTimestamp)
                .filter(ts -> !ts.isBefore(globalPeriodStart.toInstant()))
                .map(ts -> ZonedDateTime.ofInstant(ts, globalPeriodStart.getZone()))
                .min(Comparator.naturalOrder())
                .orElse(globalPeriodStart); // fallback
    }

}
