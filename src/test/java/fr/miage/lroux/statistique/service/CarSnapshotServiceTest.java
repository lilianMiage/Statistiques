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
}
