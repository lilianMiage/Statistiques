package fr.miage.lroux.statistique.repo;

import fr.miage.lroux.statistique.entity.LocationSnapshot;
import fr.miage.lroux.statistique.entity.StationSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface LocationSnapshotRepository extends MongoRepository<LocationSnapshot, String> {
    List<LocationSnapshot> findByLocationIdOrderByTimestampDesc(long locationId);
    List<LocationSnapshot> findByTimestampBetween(Instant start, Instant end);
    List<LocationSnapshot> findByUserId(Long userId);
    List<LocationSnapshot> findByCarId(Long carId);
}
