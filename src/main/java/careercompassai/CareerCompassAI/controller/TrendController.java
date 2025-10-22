package careercompassai.CareerCompassAI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class TrendController {

    @GetMapping("/trends")
    public String showTrends(Model model) {
        try {
            // Path where your JSON file is located
            String jsonPath = "src/main/resources/static/data/trends.json";
            
            // Read file content
            String trendsJson = new String(Files.readAllBytes(Paths.get(jsonPath)));

            // Add JSON to model (for Thymeleaf to access)
            model.addAttribute("trends", trendsJson);

            return "trends"; // Loads templates/trends.html
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "⚠️ Error loading trends data");
            return "error";
        }
    }
}
