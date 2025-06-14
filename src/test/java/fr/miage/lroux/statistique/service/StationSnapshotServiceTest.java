package fr.miage.lroux.statistique.service;

import fr.miage.lroux.statistique.entity.StationSnapshot;
import fr.miage.lroux.statistique.repo.StationSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class StationSnapshotServiceTest {

    @Mock
    private StationSnapshotRepository repository;

    @InjectMocks
    private StationSnapshotService service;

    private ZonedDateTime periodStart() {
        return ZonedDateTime.of(2023, 6, 1, 10, 0, 0, 0, ZoneId.systemDefault());
    }

    private ZonedDateTime periodEnd() {
        return periodStart().plusHours(1);
    }

    private StationSnapshot snap(int minutesAfterStart, int nbPlacesTaken, int capacity) {
        StationSnapshot snap = new StationSnapshot();
        snap.setTimestamp(periodStart().plusMinutes(minutesAfterStart).toInstant());
        snap.setNbPlacesTaken(nbPlacesTaken);
        snap.setNbPlaces(capacity);
        return snap;
    }

    @Test
    void testOccupationRate_NoSnapshots() {
        double result = service.getOccupationRate(List.of(), periodStart(), periodEnd());
        assertEquals(0.0, result, 0.0001);
    }

    @Test
    void testOccupationRate_AlwaysFull() {
        List<StationSnapshot> snapshots = List.of(
                snap(0, 10, 10),
                snap(30, 10, 10),
                snap(60, 10, 10)
        );
        double result = service.getOccupationRate(snapshots, periodStart(), periodEnd());
        assertEquals(1.0, result, 0.0001);
    }

    @Test
    void testOccupationRate_HalfAndFull() {
        List<StationSnapshot> snapshots = List.of(
                snap(0, 5, 10),   // 0–30 min => 50% occupation
                snap(30, 10, 10)  // 30–60 min => 100%
        );
        double result = service.getOccupationRate(snapshots, periodStart(), periodEnd());
        assertEquals(0.75, result, 0.0001);
    }

    @Test
    void testOccupationRate_VariedLoad() {
        List<StationSnapshot> snapshots = List.of(
                snap(0, 5, 10),   // 0–15 min => 50%
                snap(15, 8, 10),  // 15–30 min => 80%
                snap(30, 10, 10), // 30–45 min => 100%
                snap(45, 6, 10)   // 45–60 min => 60%
        );
        double result = service.getOccupationRate(snapshots, periodStart(), periodEnd());
        double expected = (0.5 * 900 + 0.8 * 900 + 1.0 * 900 + 0.6 * 900) / 3600.0;
        assertEquals(expected, result, 0.0001);
    }

    @Test
    void testOccupationRate_BeforeStart_NoSnapshots() {
        List<StationSnapshot> snapshots = List.of(
                snap(-10, 5, 10)
        );
        double result = service.getOccupationRate(snapshots, periodStart(), periodEnd());
        assertEquals(0.5, result, 0.0001);
    }
    @Test
    void testOccupationRate_BeforeStart_Snapshots() {
        List<StationSnapshot> snapshots = List.of(
                snap(-10, 10, 10),
                snap(30, 0, 10)
        );
        double result = service.getOccupationRate(snapshots, periodStart(), periodEnd());
        assertEquals(0.5, result, 0.0001);
    }
}
