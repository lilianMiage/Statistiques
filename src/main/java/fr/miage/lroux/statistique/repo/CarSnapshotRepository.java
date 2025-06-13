package fr.miage.lroux.statistique.repo;

import fr.miage.lroux.statistique.entity.CarSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface CarSnapshotRepository extends MongoRepository<CarSnapshot, String> {
    List<CarSnapshot> findByCarIdOrderByTimestampDesc(long carId);
    List<CarSnapshot> findByTimestampAfter(Instant timestamp);
}
