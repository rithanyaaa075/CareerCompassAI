package careercompassai.CareerCompassAI.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkillGapService {

    /**
     * ✅ Compare user's skills with job-required skills.
     * Returns matched, missing, and suggested skills.
     */
    public Map<String, List<String>> analyzeSkillGap(List<String> userSkills, List<String> jobSkills) {
        Set<String> userSet = new HashSet<>(userSkills.stream().map(String::toLowerCase).collect(Collectors.toSet()));
        Set<String> jobSet = new HashSet<>(jobSkills.stream().map(String::toLowerCase).collect(Collectors.toSet()));

        // Matched and missing skills
        List<String> matched = jobSet.stream()
                .filter(userSet::contains)
                .collect(Collectors.toList());

        List<String> missing = jobSet.stream()
                .filter(skill -> !userSet.contains(skill))
                .collect(Collectors.toList());

        // Suggested new trending skills
        List<String> suggested = suggestNewSkills(userSet);

        // Build response map
        Map<String, List<String>> result = new HashMap<>();
        result.put("matched", matched);
        result.put("missing", missing);
        result.put("suggested", suggested);

        return result;
    }

    /**
     * ✅ Suggest trending skills the user doesn't already have
     */
    private List<String> suggestNewSkills(Set<String> userSkills) {
        List<String> trending = List.of(
            "kubernetes", "terraform", "azure", "gcp", "prompt engineering",
            "data visualization", "microservices", "system design", "flask",
            "rest api", "cyber defense", "penetration testing", "ai ethics"
        );

        // Always suggest at least 3-5 trending skills the user doesn't have
        return trending.stream()
                .filter(skill -> !userSkills.contains(skill))
                .limit(5)
                .collect(Collectors.toList());
    }
}
