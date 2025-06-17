package fr.miage.lroux.statistique.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "location_snapshots")
@CompoundIndexes({
        @CompoundIndex(name = "location_ts_idx", def = "{'locationId': 1, 'timestamp': -1}")
})
public class LocationSnapshot {
    @Id
    private String id;
    private Instant timestamp;
    private long idLocation;
    private long idVoiture;
    private long idUser;
    private long idAccessCard;
    private long idStation;
    private boolean active;
    private double distanceTravelled;
}
