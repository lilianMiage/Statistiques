package fr.miage.lroux.statistique.expo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/statistique/")
public class GlobalStatistiqueController {
    @GetMapping("car/ready/")
    public int getOperationalCar(){
        return 0;
    }

    @GetMapping("car/avgKm/")
    public int getAvgKmCar(){
        return 0;
    }

    @GetMapping("station/avgOccupancy/")
    public int getAvgOccupancyStation(){
        return 0;
    }

    @GetMapping("location/sumHour/")
    public int getSumHourLocation(){
        return 0;
    }
}
