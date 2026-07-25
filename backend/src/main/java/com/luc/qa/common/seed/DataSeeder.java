package com.luc.qa.common.seed;

import com.luc.qa.module.community.dto.CreateCommunityRequestDTO;
import com.luc.qa.module.community.repository.CommunityRepository;
import com.luc.qa.module.community.service.AdminCommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CommunityRepository communityRepository;
    private final AdminCommunityService adminCommunityService;

    private record SeedEntry(String slug, String name, String parentPath) {}

    private static final SeedEntry[] CS_COMMUNITIES = {
        new SeedEntry("cs", "Computer Science", null),
        new SeedEntry("general", "General", "cs"),
        new SeedEntry("study_abroad", "Study Abroad", "cs"),
        new SeedEntry("france", "France", "cs/study_abroad"),
        new SeedEntry("germany", "Germany", "cs/study_abroad"),
        new SeedEntry("uk", "United Kingdom", "cs/study_abroad"),
        new SeedEntry("usa", "United States", "cs/study_abroad"),
        new SeedEntry("canada", "Canada", "cs/study_abroad"),
        new SeedEntry("masters", "Masters", "cs"),
        new SeedEntry("france", "France", "cs/masters"),
        new SeedEntry("germany", "Germany", "cs/masters"),
        new SeedEntry("funding", "Funding", "cs/masters"),
        new SeedEntry("jobs", "Jobs", "cs"),
        new SeedEntry("backend_skills", "Backend Skills", "cs/jobs"),
        new SeedEntry("frontend_skills", "Frontend Skills", "cs/jobs"),
        new SeedEntry("devops", "DevOps", "cs/jobs"),
        new SeedEntry("data_science", "Data Science", "cs/jobs"),
        new SeedEntry("interviews", "Interviews", "cs/jobs"),
        new SeedEntry("salaries", "Salaries", "cs/jobs"),
        new SeedEntry("internships", "Internships", "cs"),
        new SeedEntry("courses", "Courses", "cs"),
    };

    @Override
    public void run(String... args) {
        if (communityRepository.count() > 0) {
            return;
        }

        log.info("Seeding CS community tree...");
        for (SeedEntry entry : CS_COMMUNITIES) {
            CreateCommunityRequestDTO request = new CreateCommunityRequestDTO();
            request.setSlug(entry.slug());
            request.setName(entry.name());
            request.setParentPath(entry.parentPath());
            adminCommunityService.create(request);
        }
        log.info("Seeded {} communities", CS_COMMUNITIES.length);
    }
}
