package fr.miage.lroux.statistique.transientObj;

public class StationUsageDTO {
    private Long stationId;
    private double occupationRate;

    public StationUsageDTO(Long stationId, double occupationRate) {
        this.stationId = stationId;
        this.occupationRate = occupationRate;
    }

    public Long getStationId() { return stationId; }
    public double getOccupationRate() { return occupationRate; }
}