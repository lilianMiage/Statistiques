package fr.miage.lroux.statistique.expo;

import fr.miage.lroux.statistique.utilities.CarInconnuException;
import fr.miage.lroux.statistique.utilities.UserInconnuException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/statistique/station/")
public class StationStatistiqueController {
    @GetMapping("avgOccupancy/{id}")
    public int getAvgOccupancyStation(@PathVariable Long id){
        return 0;
    }
}