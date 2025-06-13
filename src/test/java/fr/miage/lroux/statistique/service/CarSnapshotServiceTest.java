package fr.miage.lroux.statistique.service;

import fr.miage.lroux.statistique.entity.CarSnapshot;
import fr.miage.lroux.statistique.repo.CarSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CarSnapshotServiceTest {

    @Mock
    private CarSnapshotRepository repository;

    @InjectMocks
    private CarSnapshotService service;

    private ZonedDateTime periodStart() {
        return ZonedDateTime.of(2023, 6, 1, 10, 0, 0, 0, ZoneId.systemDefault());
    }

    private ZonedDateTime periodEnd() {
        return periodStart().plusHours(1);
    }

    private CarSnapshot snap(int minutesAfterStart, boolean used) {
        CarSnapshot snap = new CarSnapshot();
        snap.setTimestamp(periodStart().plusMinutes(minutesAfterStart).toInstant());
        snap.setUsed(used);
        return snap;
    }

    private CarSnapshot snap(ZonedDateTime dateTime, boolean used, long carId) {
        CarSnapshot snap = new CarSnapshot();
        snap.setTimestamp(dateTime.toInstant());
        snap.setUsed(used);
        snap.setCarId(carId);
        return snap;
    }

    private CarSnapshot snap(ZonedDateTime dateTime, boolean used, long carId, double batteryLevel, double km) {
        CarSnapshot snap = new CarSnapshot();
        snap.setTimestamp(dateTime.toInstant());
        snap.setUsed(used);
        snap.setCarId(carId);
        snap.setBatteryLevel(batteryLevel);
        snap.setKilometresTravelled(km);
        return snap;
    }

    @Test
    void testUsedPercentage_NoSnapshots_NotUsedBefore() {
        double result = service.getUsedPercentage(List.of(), false, periodStart(), periodEnd());
        assertEquals(0.0, result, 0.0001);
    }

    @Test
    void testUsedPercentage_NoSnapshots_UsedBefore() {
        double result = service.getUsedPercentage(List.of(), true, periodStart(), periodEnd());
        assertEquals(1.0, result, 0.0001);
    }

    @Test
    void testUsedPercentage_UsedThenNotUsed() {
        List<CarSnapshot> snapshots = List.of(
                snap(10, true),
                snap(40, false)
        );
        double result = service.getUsedPercentage(snapshots, false, periodStart(), periodEnd());
        assertEquals(0.5, result, 0.0001); // 30 min used
    }

    @Test
    void testUsedPercentage_NotUsedThenUsed_NoEnd() {
        List<CarSnapshot> snapshots = List.of(
                snap(10, false),
                snap(30, true)
        );
        double result = service.getUsedPercentage(snapshots, false, periodStart(), periodEnd());
        assertEquals(0.5, result, 0.0001); // 30 min used
    }

    @Test
    void testUsedPercentage_UsedAtStart_ThenNotUsed() {
        List<CarSnapshot> snapshots = List.of(
                snap(20, false)
        );
        double result = service.getUsedPercentage(snapshots, true, periodStart(), periodEnd());
        assertEquals(20.0 / 60.0, result, 0.0001);
    }

    @Test
    void testUsedPercentage_UsedAllTime_NoStop() {
        List<CarSnapshot> snapshots = List.of(
                snap(20, true)
        );
        double result = service.getUsedPercentage(snapshots, true, periodStart(), periodEnd());
        assertEquals(1.0, result, 0.0001);
    }

    @Test
    void testUsedPercentage_ComplexSequence() {
        List<CarSnapshot> snapshots = List.of(
                snap(5, true),
                snap(15, false),
                snap(30, true),
                snap(50, false)
        );
        double result = service.getUsedPercentage(snapshots, false, periodStart(), periodEnd());
        assertEquals(30.0 / 60.0, result, 0.0001);
    }

    @Test
    void testGetStatsGroupedByPeriodByCar_MultipleCars() {
        ZonedDateTime periodStart = periodStart();
        ZonedDateTime periodEnd = periodEnd();

        long carA = 1;
        long carB = 2;

        // === Car A ===
        // Was unused before the period
        // Used from 10:15 to 10:45 => 30 min
        List<CarSnapshot> carASnaps = List.of(
                snap(periodStart.minusMinutes(5), false, carA),  // état initial
                snap(periodStart.plusMinutes(15), true, carA),
                snap(periodStart.plusMinutes(45), false, carA)
        );

        // === Car B ===
        // Was used before the period and never marked unused, so full 1h
        List<CarSnapshot> carBSnaps = List.of(
                snap(periodStart.minusMinutes(10), true, carB), // état initial : utilisé
                snap(periodStart.plusMinutes(30), true, carB)
        );

        // Grouped by car
        Map<Long, List<CarSnapshot>> snapshotsByCar = Map.of(
                carA, carASnaps.stream().filter(s -> !s.getTimestamp().isBefore(periodStart.toInstant())).toList(),
                carB, carBSnaps.stream().filter(s -> !s.getTimestamp().isBefore(periodStart.toInstant())).toList()
        );

        // All snapshots (for état initial)
        List<CarSnapshot> allSnapshots = new ArrayList<>();
        allSnapshots.addAll(carASnaps);
        allSnapshots.addAll(carBSnaps);

        double result = service.getStatsGroupedByPeriodByCar(snapshotsByCar, allSnapshots, periodStart, periodEnd);

        // carA => 30min = 1800s / 3600s = 0.5
        // carB => 60min = 3600s / 3600s = 1.0
        // Moyenne des ratios = 0.5 + 1.0 = 1.5
        assertEquals(1.5, result, 0.0001);
    }

    @Test
    void testGetAverageKmTravelled_EmptyMap() {
        Map<Long, Double> emptyMap = Map.of();

        double average = service.getAverageKmTravelled(emptyMap);

        assertEquals(0.0, average, 0.0001);
    }

    @Test
    void testGetAverageKmTravelled_MultipleCars() {
        Map<Long, Double> kmByCar = Map.of(
                1L, 50.0,
                2L, 70.0
        );

        double average = service.getAverageKmTravelled(kmByCar);

        assertEquals(60.0, average, 0.0001); // (50 + 70) / 2
    }
    @Test
    void testGetKmTravelledPerCarInPeriod_MultipleCars() {
        ZonedDateTime periodStart = periodStart();
        ZonedDateTime periodEnd = periodEnd();

        long carA = 1;
        long carB = 2;

        // Car A: km initial = 1000, dernier dans période = 1050 => 50km
        List<CarSnapshot> carASnaps = List.of(
                snap(periodStart.minusMinutes(10), false, carA, 80.0, 1000.0), // hors période
                snap(periodStart.plusMinutes(20), false, carA, 82.0, 1030.0),
                snap(periodEnd.minusMinutes(5), false, carA, 84.0, 1050.0)
        );

        // Car B: pas de snapshot avant → km initial = 0, dernier = 70 => 70km
        List<CarSnapshot> carBSnaps = List.of(
                snap(periodStart.plusMinutes(15), false, carB, 90.0, 40.0),
                snap(periodEnd.minusMinutes(10), false, carB, 92.0, 70.0)
        );

        Map<Long, List<CarSnapshot>> snapshotsByCar = Map.of(
                carA, carASnaps.stream().filter(s -> !s.getTimestamp().isBefore(periodStart.toInstant())).toList(),
                carB, carBSnaps
        );

        List<CarSnapshot> allSnapshots = new ArrayList<>();
        allSnapshots.addAll(carASnaps);
        allSnapshots.addAll(carBSnaps);

        // === km parcourus par voiture ===
        Map<Long, Double> result = service.getKmTravelledPerCarInPeriod(
                snapshotsByCar, allSnapshots, periodStart, periodEnd
        );

        assertEquals(50.0, result.get(carA), 0.0001);
        assertEquals(70.0, result.get(carB), 0.0001);
    }
}
