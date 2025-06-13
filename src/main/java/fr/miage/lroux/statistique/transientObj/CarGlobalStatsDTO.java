package fr.miage.lroux.statistique.transientObj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarGlobalStatsDTO {
    private double averageBatteryLevel;
    private double averageKilometresTravelled;
    private int totalSnapshots;
    private int usedCarCount;
    private int uniqueCarCount;
    private double usedPercentage;
}
