package fr.miage.lroux.statistique.transientObj;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class StationStatPerPeriodDTO {
    private String periodLabel;
    private double occupationRate; // en %
    private int snapshotCount;
}
