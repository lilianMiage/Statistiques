package fr.miage.lroux.statistique.transientObj;

public class StationUsageDTO {
    private Long id;
    private double occupationRate;

    public StationUsageDTO(Long id, double occupationRate) {
        this.id = id;
        this.occupationRate = occupationRate;
    }

    public Long getId() { return id; }
    public double getOccupationRate() { return occupationRate; }
}