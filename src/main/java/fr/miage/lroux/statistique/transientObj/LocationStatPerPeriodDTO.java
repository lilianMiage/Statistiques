package fr.miage.lroux.statistique.transientObj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationStatPerPeriodDTO {
    private String periodLabel;
    private double totalDistanceTravelled;
    private int snapshotCount;
}
