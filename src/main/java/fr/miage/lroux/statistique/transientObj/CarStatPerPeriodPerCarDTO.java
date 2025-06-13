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
public class CarStatPerPeriodPerCarDTO {
    private String periodLabel;
    private double KilometresTravelled;
    private int snapshotCount;
    private double usedPercentage;
}
