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
@Document(collection = "car_snapshots")
@CompoundIndexes({
        @CompoundIndex(name = "car_ts_idx", def = "{'carId': 1, 'timestamp': -1}")
})
public class CarSnapshot {
    @Id
    private String id;
    private Instant timestamp;
    private long carId;
    private String brand;
    private String model;
    private double batteryLevel; // niveau de batterie
    private double kilometresTravelled; // km parcourus
    private int numberOfSeats; // nombre de places
    private boolean used; // savoir s'il est dispo ou non
    private List<Double> localisation; // localisation GPS
    private long stationId;
}
