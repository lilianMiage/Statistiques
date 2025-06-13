package fr.miage.lroux.statistique.service;

import fr.miage.lroux.statistique.entity.CarSnapshot;
import fr.miage.lroux.statistique.entity.TimeGranularity;
import fr.miage.lroux.statistique.repo.CarSnapshotRepository;
import fr.miage.lroux.statistique.transientObj.CarGlobalStatsDTO;
import fr.miage.lroux.statistique.transientObj.CarStatPerPeriodDTO;
import fr.miage.lroux.statistique.transientObj.CarStatPerPeriodPerCarDTO;
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

    public List<CarSnapshot> getSnapshotsAfter(Instant timestamp) {
        return repository.findByTimestampAfter(timestamp);
    }

    public List<CarStatPerPeriodDTO> getStatsGroupedByPeriod(String period) {
        List<CarSnapshot> allSnapshots = repository.findAll();
        TimeGranularity granularity = TimeGranularity.valueOf(period.toUpperCase());

        Map<Object, List<CarSnapshot>> grouped = groupSnapshotsByPeriod(allSnapshots, granularity);

        return grouped.entrySet().stream().map(entry -> {
            Object periodKey = entry.getKey();
            List<CarSnapshot> snapshotsInPeriod = entry.getValue();

            ZonedDateTime periodStart = getPeriodStart(periodKey, granularity);
            ZonedDateTime periodEnd = getPeriodEnd(periodStart, granularity);
            ZonedDateTime now = ZonedDateTime.now();
            if (periodEnd.isAfter(now)) {
                periodEnd = now;
            }

            Map<Long, List<CarSnapshot>> snapshotsByCar = snapshotsInPeriod.stream()
                    .collect(Collectors.groupingBy(CarSnapshot::getCarId));

            double totalUsedRatio = getStatsGroupedByPeriodByCar(snapshotsByCar, allSnapshots, periodStart, periodEnd);

            Map<Long, Double> kmByCar = getKmTravelledPerCarInPeriod(snapshotsByCar, allSnapshots, periodStart, periodEnd);
            double avgKm = getAverageKmTravelled(kmByCar);

            return CarStatPerPeriodDTO.builder()
                    .periodLabel(periodKey.toString())
                    .averageKilometresTravelled(avgKm)
                    .snapshotCount(snapshotsInPeriod.size())
                    .usedPercentage(!snapshotsByCar.isEmpty() ? (totalUsedRatio / snapshotsByCar.size()) * 100 : 0)
                    .build();
        }).sorted(Comparator.comparing(CarStatPerPeriodDTO::getPeriodLabel).reversed()).toList();
    }

    public List<CarStatPerPeriodPerCarDTO> getStatsGroupedByPeriodPerCarById(String period, Long idCar) {
        List<CarSnapshot> allSnapshots = repository.findAll();
        TimeGranularity granularity = TimeGranularity.valueOf(period.toUpperCase());

        // Filtrer uniquement les snapshots de la voiture demandée
        List<CarSnapshot> snapshotsForCar = allSnapshots.stream()
                .filter(s -> s.getCarId() == idCar)
                .toList();

        if (snapshotsForCar.isEmpty()) {
            return List.of(); // Pas de données pour cette voiture
        }

        // Regroupe par période uniquement les snapshots de la voiture
        Map<Object, List<CarSnapshot>> groupedByPeriod = groupSnapshotsByPeriod(snapshotsForCar, granularity);

        List<CarStatPerPeriodPerCarDTO> stats = new ArrayList<>();

        for (Map.Entry<Object, List<CarSnapshot>> entry : groupedByPeriod.entrySet()) {
            Object periodKey = entry.getKey();
            List<CarSnapshot> snapshotsInPeriod = entry.getValue();

            ZonedDateTime periodStart = getPeriodStart(periodKey, granularity);
            ZonedDateTime periodEnd = getPeriodEnd(periodStart, granularity);
            ZonedDateTime now = ZonedDateTime.now();
            if (periodEnd.isAfter(now)) {
                periodEnd = now;
            }
            Map<Long, List<CarSnapshot>> mapSingleCar = Map.of(idCar, snapshotsInPeriod);

            double usedRatio = getStatsGroupedByPeriodByCar(mapSingleCar, allSnapshots, periodStart, periodEnd);

            Map<Long, Double> kmByCar = getKmTravelledPerCarInPeriod(mapSingleCar, allSnapshots, periodStart, periodEnd);
            double kmTravelled = kmByCar.getOrDefault(idCar, 0.0);

            CarStatPerPeriodPerCarDTO dto = new CarStatPerPeriodPerCarDTO();
            dto.setPeriodLabel(periodKey.toString());
            dto.setKilometresTravelled(kmTravelled);
            dto.setSnapshotCount(snapshotsInPeriod.size());
            dto.setUsedPercentage(usedRatio * 100);

            stats.add(dto);
        }

        // Tri par période décroissant
        stats.sort(Comparator.comparing(CarStatPerPeriodPerCarDTO::getPeriodLabel).reversed());

        return stats;
    }

    //USED
    public double getStatsGroupedByPeriodByCar(Map<Long, List<CarSnapshot>> snapshotsByCar,
                                               List<CarSnapshot> allSnapshots,
                                               ZonedDateTime periodStart,
                                               ZonedDateTime periodEnd) {

        double totalUsedRatio = 0.0;

        for (Map.Entry<Long, List<CarSnapshot>> carEntry : snapshotsByCar.entrySet()) {
            long carId = carEntry.getKey();
            List<CarSnapshot> snapshotsForCar = carEntry.getValue().stream()
                    .sorted(Comparator.comparing(CarSnapshot::getTimestamp))
                    .toList();

            Optional<CarSnapshot> previousSnapshotOpt = allSnapshots.stream()
                    .filter(s -> s.getCarId() == carId && s.getTimestamp().isBefore(periodStart.toInstant()))
                    .max(Comparator.comparing(CarSnapshot::getTimestamp));

            boolean currentUsed = previousSnapshotOpt.map(CarSnapshot::isUsed).orElse(false);
            double usedRatio = getUsedPercentage(snapshotsForCar, currentUsed, periodStart, periodEnd);

            totalUsedRatio += usedRatio;
        }

        return totalUsedRatio;
    }

    public double getUsedPercentage(List<CarSnapshot> snapshots, boolean initialUsed, ZonedDateTime periodStart, ZonedDateTime periodEnd) {
        snapshots = snapshots.stream()
                .sorted(Comparator.comparing(CarSnapshot::getTimestamp))
                .toList();

        Instant start = periodStart.toInstant();
        Instant end = periodEnd.toInstant();
        Instant currentTime = start;
        boolean isUsed = initialUsed;
        long usedSeconds = 0;

        for (CarSnapshot snapshot : snapshots) {
            Instant snapshotTime = snapshot.getTimestamp();

            if (snapshotTime.isAfter(end)) break; // Ne pas dépasser la période

            long duration = Duration.between(currentTime, snapshotTime).getSeconds();
            if (isUsed) {
                usedSeconds += duration;
            }

            isUsed = snapshot.isUsed(); // mise à jour de l'état
            currentTime = snapshotTime;
        }

        // Ajouter la fin de période si toujours utilisé
        if (currentTime.isBefore(end) && isUsed) {
            usedSeconds += Duration.between(currentTime, end).getSeconds();
        }

        long totalSeconds = Duration.between(start, end).getSeconds();
        return totalSeconds > 0 ? (double) usedSeconds / totalSeconds : 0.0;
    }

    private Map<Object, List<CarSnapshot>> groupSnapshotsByPeriod(List<CarSnapshot> allSnapshots, TimeGranularity granularity) {
        return switch (granularity) {
            case HOUR -> allSnapshots.stream().collect(Collectors.groupingBy(s ->
                    s.getTimestamp().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.HOURS)));
            case DAY -> allSnapshots.stream().collect(Collectors.groupingBy(s ->
                    s.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate()));
            case WEEK -> allSnapshots.stream().collect(Collectors.groupingBy(s ->
                    YearWeek.from(s.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate())));
            case MONTH -> allSnapshots.stream().collect(Collectors.groupingBy(s ->
                    YearMonth.from(s.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate())));
            case YEAR -> allSnapshots.stream().collect(Collectors.groupingBy(s ->
                    Year.from(s.getTimestamp().atZone(ZoneId.systemDefault()))));
        };
    }

    private ZonedDateTime getPeriodStart(Object periodKey, TimeGranularity granularity) {
        return switch (granularity) {
            case HOUR -> ((ZonedDateTime) periodKey);
            case DAY -> ((LocalDate) periodKey).atStartOfDay(ZoneId.systemDefault());
            case WEEK -> ((YearWeek) periodKey).atDay(DayOfWeek.MONDAY).atStartOfDay(ZoneId.systemDefault());
            case MONTH -> ((YearMonth) periodKey).atDay(1).atStartOfDay(ZoneId.systemDefault());
            case YEAR -> ((Year) periodKey).atDay(1).atStartOfDay(ZoneId.systemDefault());
        };
    }

    private ZonedDateTime getPeriodEnd(ZonedDateTime periodStart, TimeGranularity granularity) {
        return switch (granularity) {
            case HOUR -> periodStart.plusHours(1);
            case DAY -> periodStart.plusDays(1);
            case WEEK -> periodStart.plusWeeks(1);
            case MONTH -> periodStart.plusMonths(1);
            case YEAR -> periodStart.plusYears(1);
        };
    }

    //KM TRAVELLED
    public Map<Long, Double> getKmTravelledPerCarInPeriod(
            Map<Long, List<CarSnapshot>> snapshotsByCar,
            List<CarSnapshot> allSnapshots,
            ZonedDateTime periodStart,
            ZonedDateTime periodEnd) {

        Map<Long, Double> kmByCar = new HashMap<>();

        for (Map.Entry<Long, List<CarSnapshot>> entry : snapshotsByCar.entrySet()) {
            long carId = entry.getKey();
            List<CarSnapshot> carSnapsInPeriod = entry.getValue();

            // Dernier snapshot avant la période (pour km initial)
            Optional<CarSnapshot> prevSnapshotOpt = allSnapshots.stream()
                    .filter(s -> s.getCarId() == carId && s.getTimestamp().isBefore(periodStart.toInstant()))
                    .max(Comparator.comparing(CarSnapshot::getTimestamp));

            double kmStart = prevSnapshotOpt.map(CarSnapshot::getKilometresTravelled).orElse(0.0);

            // Dernier snapshot dans la période
            Optional<CarSnapshot> lastInPeriodOpt = carSnapsInPeriod.stream()
                    .max(Comparator.comparing(CarSnapshot::getTimestamp));

            double kmEnd = lastInPeriodOpt.map(CarSnapshot::getKilometresTravelled).orElse(kmStart);

            kmByCar.put(carId, Math.max(0, kmEnd - kmStart));
        }

        return kmByCar;
    }

    public double getAverageKmTravelled(Map<Long, Double> kmByCar) {
        if (kmByCar.isEmpty()) return 0;
        return kmByCar.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
