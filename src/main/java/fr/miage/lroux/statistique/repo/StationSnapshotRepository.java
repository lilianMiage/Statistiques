package fr.miage.lroux.statistique.repo;

import fr.miage.lroux.statistique.entity.StationSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface StationSnapshotRepository extends MongoRepository<StationSnapshot, String> {
    List<StationSnapshot> findByStationIdOrderByTimestampDesc(long stationId);
    List<StationSnapshot> findByTimestampBetween(Instant start, Instant end);
}