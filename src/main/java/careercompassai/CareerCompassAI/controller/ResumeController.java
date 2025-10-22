package careercompassai.CareerCompassAI.controller;

import careercompassai.CareerCompassAI.service.JobSkillService;
import careercompassai.CareerCompassAI.service.SkillGapService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ResumeController {

    @Autowired
    private JobSkillService jobSkillService;

    @Autowired
    private SkillGapService skillGapService; // ✅ Newly added dependency

    @GetMapping("/upload")
    public String showUploadPage() {
        return "resume";
    }

    @PostMapping("/uploadResume")
    public String uploadResume(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "⚠️ Please upload a resume file!");
            return "resume";
        }

        try {
            // ✅ Step 1: Extract text from resume
            Tika tika = new Tika();
            InputStream inputStream = file.getInputStream();
            String resumeText = tika.parseToString(inputStream);

            // ✅ Step 2: Extract detected skills
            List<String> extractedSkills = extractSkillsFromResume(resumeText);
            System.out.println("🧠 Extracted Skills: " + extractedSkills);

            // ✅ Step 3: Compare with job dataset
            Map<String, Double> matchScores = calculateMatchScores(extractedSkills);

            // ✅ Step 4: Pick the best-fit role
            String bestFitRole = matchScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("No matching role found");

            double bestScore = matchScores.getOrDefault(bestFitRole, 0.0);

            // ✅ Step 5: Skill Gap Analysis
            List<Map<String, String>> allJobs = jobSkillService.getAllJobs();
            List<String> jobSkills = new ArrayList<>();

            for (Map<String, String> job : allJobs) {
                if (job.get("role").equalsIgnoreCase(bestFitRole)) {
                    String skills = job.get("skills");
                    if (skills != null) {
                        jobSkills = Arrays.stream(skills.split(","))
                                .map(String::trim)
                                .map(String::toLowerCase)
                                .collect(Collectors.toList());
                    }
                    break;
                }
            }

            Map<String, List<String>> gapAnalysis = skillGapService.analyzeSkillGap(extractedSkills, jobSkills);
            List<String> matchedSkills = gapAnalysis.get("matched");
            List<String> missingSkills = gapAnalysis.get("missing");
            List<String> suggestedSkills = gapAnalysis.get("suggested");

            // ✅ Step 6: Recommend certifications
            List<String> certifications = recommendCertifications(bestFitRole);

            // ✅ Step 7: Send data to frontend
            model.addAttribute("resumeTextPreview",
                    resumeText.length() > 400 ? resumeText.substring(0, 400) + "..." : resumeText);
            model.addAttribute("extractedSkills", extractedSkills);
            model.addAttribute("matchScores", matchScores);
            model.addAttribute("bestFitRole", bestFitRole);
            model.addAttribute("bestScore", String.format("%.2f", bestScore));
            model.addAttribute("certifications", certifications);

            // ✅ Add skill gap results
            model.addAttribute("matchedSkills", matchedSkills);
            model.addAttribute("missingSkills", missingSkills);
            model.addAttribute("suggestedSkills", suggestedSkills);

            System.out.println("🎯 Best Fit Role: " + bestFitRole + " (Score: " + bestScore + "%)");
            System.out.println("✅ Matched: " + matchedSkills);
            System.out.println("⚠️ Missing: " + missingSkills);
            System.out.println("🚀 Suggested: " + suggestedSkills);

            return "result";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "❌ Error processing resume: " + e.getMessage());
            return "resume";
        }
    }

    /**
     * ✅ Extract common technical keywords from resume
     */
    private List<String> extractSkillsFromResume(String text) {
        String[] knownSkills = {
                "python", "java", "c", "c++", "c#", "sql", "aws", "docker", "kubernetes", "linux",
                "cybersecurity", "networking", "data analysis", "machine learning", "ai", "cloud",
                "devops", "react", "javascript", "spring", "git", "html", "css", "nodejs", "api",
                "powerbi", "excel", "tableau", "mongodb"
        };

        List<String> foundSkills = new ArrayList<>();
        String lowerText = text.toLowerCase();

        for (String skill : knownSkills) {
            if (lowerText.contains(skill)) {
                foundSkills.add(skill);
            }
        }

        return foundSkills.isEmpty() ? List.of("No clear skills detected") : foundSkills;
    }

    /**
     * ✅ Compare resume skills with tech job dataset
     */
    private Map<String, Double> calculateMatchScores(List<String> resumeSkills) {
        Map<String, Double> matchScores = new LinkedHashMap<>();

        List<Map<String, String>> allJobs = jobSkillService.getAllJobs();
        if (allJobs.isEmpty()) {
            System.out.println("⚠️ No jobs found in dataset!");
            return Map.of("No Data", 0.0);
        }

        for (Map<String, String> job : allJobs) {
            String role = job.get("role");
            String skills = job.get("skills");
            if (skills == null || skills.isBlank()) continue;

            List<String> jobSkills = Arrays.stream(skills.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();

            long matches = jobSkills.stream().filter(resumeSkills::contains).count();

            if (matches > 0) {
                double score = (matches / (double) jobSkills.size()) * 100;
                matchScores.put(role, score);
            }
        }

        if (matchScores.isEmpty()) {
            System.out.println("⚠️ No matching roles found — possible skill mismatch.");
            return Map.of("No matching role found", 0.0);
        }

        return matchScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * ✅ Recommend relevant certifications based on role
     */
    private List<String> recommendCertifications(String role) {
        Map<String, List<String>> certMap = Map.of(
                "software engineer", List.of("Oracle Java SE", "Spring Professional", "AWS Developer"),
                "data analyst", List.of("Google Data Analytics", "Tableau Specialist", "Power BI Analyst"),
                "ai engineer", List.of("TensorFlow Developer", "Azure AI Engineer", "DeepLearning.AI"),
                "cybersecurity", List.of("CompTIA Security+", "CEH", "Cisco CyberOps Associate"),
                "cloud engineer", List.of("AWS Cloud Practitioner", "Azure Fundamentals", "Google Cloud Associate"),
                "devops", List.of("Docker Certified Associate", "Kubernetes CKA", "AWS DevOps Engineer"),
                "web developer", List.of("Meta Front-End Developer", "FreeCodeCamp HTML/CSS", "Google Web Dev")
        );

        for (String key : certMap.keySet()) {
            if (role.toLowerCase().contains(key)) {
                return certMap.get(key);
            }
        }
        return List.of("Explore general tech certifications on Coursera or Udemy");
    }
}
