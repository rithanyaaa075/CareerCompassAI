package careercompassai.CareerCompassAI.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JobSkillService {

    private final List<Map<String, String>> jobSkillList = new ArrayList<>();

    @PostConstruct
    public void loadJobSkills() {
        String jobFile = "src/main/resources/static/data/linkedin_job_postings.csv";
        String skillFile = "src/main/resources/static/data/job_skills.csv";

        System.out.println("📂 Loading job data and skills...");

        Map<String, String> jobTitles = new HashMap<>();
        Map<String, String> jobSkills = new HashMap<>();

        // Step 1: Load job titles
        try (BufferedReader br = new BufferedReader(new FileReader(jobFile, StandardCharsets.UTF_8))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] v = parseCsvLine(line);
                if (v.length < 6) continue;
                String link = clean(v[0]);
                String title = clean(v[5]).toLowerCase();

                // ✅ Only keep tech-related jobs
                if (title.contains("software") || title.contains("developer") || title.contains("engineer")
                        || title.contains("data") || title.contains("cyber") || title.contains("security")
                        || title.contains("cloud") || title.contains("ai") || title.contains("ml")
                        || title.contains("devops") || title.contains("analyst")) {
                    jobTitles.put(link, title);
                }
            }
            System.out.println("✅ Loaded " + jobTitles.size() + " tech job titles.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Step 2: Load job skills
        try (BufferedReader br = new BufferedReader(new FileReader(skillFile, StandardCharsets.UTF_8))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] v = parseCsvLine(line);
                if (v.length < 2) continue;
                String link = clean(v[0]);
                String skills = clean(v[1]);
                if (!skills.isBlank()) {
                    jobSkills.put(link, skills.toLowerCase());
                }
            }
            System.out.println("✅ Loaded " + jobSkills.size() + " job skills.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Step 3: Merge matched links
        int count = 0;
        for (String link : jobTitles.keySet()) {
            if (jobSkills.containsKey(link)) {
                Map<String, String> job = new HashMap<>();
                job.put("role", jobTitles.get(link));
                job.put("skills", jobSkills.get(link));
                jobSkillList.add(job);
                count++;
            }
        }

        System.out.println("✅ Linked " + count + " tech jobs with skills!");
        jobSkillList.stream().limit(3).forEach(j ->
                System.out.println("ROLE: " + j.get("role") + "\nSKILLS: " + j.get("skills") + "\n---"));
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean insideQuote = false;
        StringBuilder sb = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                insideQuote = !insideQuote;
            } else if (c == ',' && !insideQuote) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }

    private String clean(String text) {
        return text == null ? "" : text.replaceAll("\"", "").trim();
    }

    public List<String> getTopSkillsForRole(String role) {
        return jobSkillList.stream()
                .filter(j -> j.get("role").contains(role.toLowerCase()))
                .flatMap(j -> Arrays.stream(j.get("skills").split(",")))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(10)
                .toList();
    }

    public List<Map<String, String>> getAllJobs() {
        return jobSkillList;
    }
}
