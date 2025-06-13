package fr.miage.lroux.statistique.transientObj;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarStatPerPeriodDTO {
    private String periodLabel;
    private double averageKilometresTravelled;
    private int snapshotCount;
    private double usedPercentage;
}
