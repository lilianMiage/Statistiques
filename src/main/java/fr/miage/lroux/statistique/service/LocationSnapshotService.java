package fr.miage.lroux.statistique.service;

import fr.miage.lroux.statistique.entity.LocationSnapshot;
import fr.miage.lroux.statistique.entity.StationSnapshot;
import fr.miage.lroux.statistique.repo.CarSnapshotRepository;
import fr.miage.lroux.statistique.repo.LocationSnapshotRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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
}
