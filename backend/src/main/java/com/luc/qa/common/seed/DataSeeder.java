package com.luc.qa.common.seed;

import com.luc.qa.module.community.dto.CreateCommunityRequestDTO;
import com.luc.qa.module.community.repository.CommunityRepository;
import com.luc.qa.module.community.service.AdminCommunityService;
import com.luc.qa.module.registration.entity.StudentDirectory;
import com.luc.qa.module.registration.repository.StudentDirectoryRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final String STUDENT_CSV = "seed/student-directory.csv";

    private final CommunityRepository communityRepository;
    private final AdminCommunityService adminCommunityService;
    private final StudentDirectoryRepository studentDirectoryRepository;

    private record CommunitySeed(String slug, String name, String parentPath) {}

    private static final CommunitySeed[] CS_COMMUNITIES = {
        new CommunitySeed("cs", "Computer Science", null),
        new CommunitySeed("general", "General", "cs"),
        new CommunitySeed("study_abroad", "Study Abroad", "cs"),
        new CommunitySeed("france", "France", "cs/study_abroad"),
        new CommunitySeed("germany", "Germany", "cs/study_abroad"),
        new CommunitySeed("uk", "United Kingdom", "cs/study_abroad"),
        new CommunitySeed("usa", "United States", "cs/study_abroad"),
        new CommunitySeed("canada", "Canada", "cs/study_abroad"),
        new CommunitySeed("masters", "Masters", "cs"),
        new CommunitySeed("france", "France", "cs/masters"),
        new CommunitySeed("germany", "Germany", "cs/masters"),
        new CommunitySeed("funding", "Funding", "cs/masters"),
        new CommunitySeed("jobs", "Jobs", "cs"),
        new CommunitySeed("backend_skills", "Backend Skills", "cs/jobs"),
        new CommunitySeed("frontend_skills", "Frontend Skills", "cs/jobs"),
        new CommunitySeed("devops", "DevOps", "cs/jobs"),
        new CommunitySeed("data_science", "Data Science", "cs/jobs"),
        new CommunitySeed("interviews", "Interviews", "cs/jobs"),
        new CommunitySeed("salaries", "Salaries", "cs/jobs"),
        new CommunitySeed("internships", "Internships", "cs"),
        new CommunitySeed("courses", "Courses", "cs"),
    };

    @Override
    public void run(String... args) {
        seedCommunities();
        seedStudents();
    }

    private void seedCommunities() {
        if (communityRepository.count() > 0) {
            return;
        }
        log.info("Seeding CS community tree...");
        for (CommunitySeed entry : CS_COMMUNITIES) {
            CreateCommunityRequestDTO request = new CreateCommunityRequestDTO();
            request.setSlug(entry.slug());
            request.setName(entry.name());
            request.setParentPath(entry.parentPath());
            adminCommunityService.create(request);
        }
        log.info("Seeded {} communities", CS_COMMUNITIES.length);
    }

    private void seedStudents() {
        // Drop unregistered mock rows so the CSV can redefine file numbers and emails.
        var stale = studentDirectoryRepository.findAll().stream()
            .filter(row -> !row.isRegistered())
            .toList();
        if (!stale.isEmpty()) {
            studentDirectoryRepository.deleteAll(stale);
            studentDirectoryRepository.flush();
            log.info("Cleared {} unregistered mock student_directory rows before reseed", stale.size());
        }

        ClassPathResource resource = new ClassPathResource(STUDENT_CSV);
        int inserted = 0;
        int updated = 0;

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String header = reader.readLine();
            if (header == null) {
                throw new IllegalStateException(STUDENT_CSV + " is empty");
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String[] cols = line.split(",", -1);
                if (cols.length != 7) {
                    throw new IllegalStateException(
                        STUDENT_CSV + " line " + lineNumber + " must have 7 columns"
                    );
                }

                String fileNumber = cols[0].trim();
                StudentDirectory row = studentDirectoryRepository.findById(fileNumber).orElse(null);
                if (row != null && row.isRegistered()) {
                    continue;
                }

                boolean exists = row != null;
                if (!exists) {
                    row = new StudentDirectory();
                    row.setFileNumber(fileNumber);
                }
                row.setEmail(cols[1].trim());
                row.setFirstName(cols[2].trim());
                row.setLastName(cols[3].trim());
                row.setEnrollmentYear(Integer.parseInt(cols[4].trim()));
                row.setFaculty(cols[5].trim());
                row.setMajor(cols[6].trim());
                studentDirectoryRepository.save(row);
                if (exists) {
                    updated++;
                } else {
                    inserted++;
                }
            }
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load mock students from " + STUDENT_CSV, ex);
        }

        if (inserted > 0 || updated > 0) {
            log.info("Mock students from {}: {} inserted, {} updated", STUDENT_CSV, inserted, updated);
        }
    }
}
