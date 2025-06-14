package fr.miage.lroux.statistique.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
@Getter
@Setter
@Document(collection = "station_snapshots")
@CompoundIndexes({
        @CompoundIndex(name = "station_ts_idx", def = "{'stationId': 1, 'timestamp': -1}")
})
public class StationSnapshot {
    @Id
    private String id;
    private Instant timestamp;
    private long stationId;
    private List<Double> localisation;
    private int nbPlaces;
    private int nbPlacesTaken;
    private int nbPlacesFree;
}
