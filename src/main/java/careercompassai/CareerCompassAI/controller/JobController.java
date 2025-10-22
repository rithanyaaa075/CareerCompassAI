package careercompassai.CareerCompassAI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

@Controller
public class JobController {

    // ✅ Adzuna API credentials
    private final String API_URL = "https://api.adzuna.com/v1/api/jobs/in/search/1";
    private final String APP_ID = "5172f6e9"; 
    private final String APP_KEY = "1a50ae704c49b923527083d265c94e96"; 

    /**
     * ✅ Show trending jobs (default)
     */
    @GetMapping("/jobs")
    public String showJobsPage(Model model) {
        // Trending tech roles (default)
        List<String> trendingRoles = Arrays.asList(
                "Cybersecurity", "Data Analyst", "AI Engineer", "Cloud Engineer", "DevOps Engineer", "Software Developer"
        );

        // Fetch jobs for each trending role
        Map<String, List<Map<String, String>>> trendingJobs = new LinkedHashMap<>();

        for (String role : trendingRoles) {
            List<Map<String, String>> jobsForRole = fetchJobs(role, 3); // limit 3 per role
            trendingJobs.put(role, jobsForRole);
        }

        model.addAttribute("trendingJobs", trendingJobs);
        model.addAttribute("showTrending", true); // flag to show trending layout
        return "jobs";
    }

    /**
     * ✅ Fetch jobs for a user-searched role
     */
    @GetMapping("/getJobs")
    public String getJobs(@RequestParam(name = "role", required = false) String role, Model model) {
        try {
            if (role == null || role.trim().isEmpty()) {
                model.addAttribute("error", "Please enter a job role.");
                return "jobs";
            }

            List<Map<String, String>> jobList = fetchJobs(role, 10);

            model.addAttribute("role", role);
            model.addAttribute("jobs", jobList);
            model.addAttribute("showTrending", false);

            return "jobs";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "❌ Error fetching jobs: " + e.getMessage());
            return "jobs";
        }
    }

    /**
     * ✅ Helper function to fetch jobs using Adzuna API
     */
    private List<Map<String, String>> fetchJobs(String role, int limit) {
    List<Map<String, String>> jobList = new ArrayList<>();

    try {
        String url = String.format("%s?app_id=%s&app_key=%s&what=%s&results_per_page=%d",
                API_URL, APP_ID, APP_KEY, role, limit);

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        System.out.println("Fetching jobs for: " + role);
        System.out.println("Response: " + response);

        JSONObject json = new JSONObject(response);
        JSONArray results = json.getJSONArray("results");

        for (int i = 0; i < results.length(); i++) {
            JSONObject job = results.getJSONObject(i);
            Map<String, String> jobData = new HashMap<>();

            jobData.put("title", job.optString("title", "No title"));
            jobData.put("company", job.optJSONObject("company") != null ?
                    job.getJSONObject("company").optString("display_name", "N/A") : "N/A");
            jobData.put("location", job.optJSONObject("location") != null ?
                    job.getJSONObject("location").optString("display_name", "N/A") : "N/A");
            jobData.put("desc", job.optString("description", "No description available"));
            jobData.put("link", job.optString("redirect_url", "#"));

            jobList.add(jobData);
        }

    } catch (Exception e) {
        System.out.println("⚠️ Error fetching real jobs for '" + role + "': " + e.getMessage());
        System.out.println("➡️ Loading mock jobs instead.");

        // ✅ Fallback mock data
        for (int i = 1; i <= 3; i++) {
            Map<String, String> mock = new HashMap<>();
            mock.put("title", role + " Role Example " + i);
            mock.put("company", "CareerCompassAI Inc");
            mock.put("location", "Remote / India");
            mock.put("desc", "This is a mock job listing generated locally because API data was not available.");
            mock.put("link", "#");
            jobList.add(mock);
        }
    }

    return jobList;
    }
}
