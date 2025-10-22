package careercompassai.CareerCompassAI.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ AIResumeEnhancerService
 * - Matches extracted resume skills with job skill dataset (via JobSkillService)
 * - Computes match percentage and recommends certifications
 */
@Service
public class AIResumeEnhancerService {

    private final JobSkillService jobSkillService;

    public AIResumeEnhancerService(JobSkillService jobSkillService) {
        this.jobSkillService = jobSkillService;
    }

    /**
     * ✅ Compare resume skills against dataset
     */
    public Map<String, Object> analyzeResumeSkills(List<String> resumeSkills) {
        Map<String, Double> matchScores = new LinkedHashMap<>();

        // Define top roles we want to analyze
        String[] rolesToCheck = {
                "Software Engineer", "Data Analyst", "AI Engineer", "Cybersecurity Analyst",
                "Cloud Engineer", "DevOps Engineer", "Web Developer"
        };

        for (String role : rolesToCheck) {
            List<String> jobSkills = jobSkillService.getTopSkillsForRole(role);
            if (jobSkills.isEmpty()) continue;

            long matches = jobSkills.stream()
                    .map(String::toLowerCase)
                    .filter(resumeSkills::contains)
                    .count();

            double score = (matches / (double) jobSkills.size()) * 100;
            matchScores.put(role, score);
        }

        // Get best-fit role
        String bestFitRole = matchScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No matching role found");

        double bestScore = matchScores.getOrDefault(bestFitRole, 0.0);

        // Recommend certifications
        List<String> certs = recommendCertifications(bestFitRole);

        Map<String, Object> result = new HashMap<>();
        result.put("matchScores", sortByValueDescending(matchScores));
        result.put("bestFitRole", bestFitRole);
        result.put("bestScore", bestScore);
        result.put("certifications", certs);

        return result;
    }

    /**
     * ✅ Recommend certifications based on role
     */
    private List<String> recommendCertifications(String role) {
        Map<String, List<String>> certMap = Map.of(
                "Software Engineer", List.of("Oracle Java SE", "Spring Professional", "AWS Developer"),
                "Data Analyst", List.of("Google Data Analytics", "Tableau Specialist", "Microsoft Power BI"),
                "AI Engineer", List.of("TensorFlow Developer", "Azure AI Engineer", "DeepLearning.AI"),
                "Cybersecurity Analyst", List.of("CompTIA Security+", "CEH", "Cisco CyberOps Associate"),
                "Cloud Engineer", List.of("AWS Cloud Practitioner", "Azure Fundamentals", "Google Cloud Associate"),
                "DevOps Engineer", List.of("Docker Certified Associate", "Kubernetes CKA", "AWS DevOps Engineer"),
                "Web Developer", List.of("FreeCodeCamp Frontend Cert", "Meta React Developer", "Google Web Dev")
        );

        return certMap.getOrDefault(role, List.of("Explore general tech certifications on Coursera or Udemy"));
    }

    /**
     * Helper: Sort a map by value descending
     */
    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
