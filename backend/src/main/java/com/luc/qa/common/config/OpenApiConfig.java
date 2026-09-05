package com.luc.qa.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI lucOpenApi() {
        final String bearerAuth = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("Lebanese University Connect API")
                .description("""
                    REST API for Lebanese University Connect (LUC): Q&A feed, communities, \
                    alumni verification, mentorship, courses, moderation, and admin tools.

                    Most write endpoints require a Keycloak JWT (`Authorization: Bearer <token>`). \
                    Admin routes under `/api/admin/**` require the `ADMIN` role. \
                    Public GETs (feed, questions, communities, courses, alumni directory, health) \
                    do not require auth; sending a token may still enrich responses (e.g. viewer vote).
                    """)
                .version("0.0.1"))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development")
            ))
            .components(new Components()
                .addSecuritySchemes(bearerAuth, new SecurityScheme()
                    .name(bearerAuth)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Keycloak access token for the `luc` realm")))
            .addSecurityItem(new SecurityRequirement().addList(bearerAuth))
            .tags(List.of(
                new Tag().name("Health").description("Service health checks"),
                new Tag().name("Feed").description("Paginated question feed"),
                new Tag().name("Questions").description("Question CRUD and accepted answers"),
                new Tag().name("Answers").description("Answers and nested replies"),
                new Tag().name("Question Saves").description("Save / unsave questions"),
                new Tag().name("Votes").description("Upvote / downvote questions and answers"),
                new Tag().name("Current User").description("Authenticated user profile and onboarding"),
                new Tag().name("Users").description("Public user profiles"),
                new Tag().name("Avatars").description("Avatar image uploads and serving"),
                new Tag().name("Registration").description("Student registration and email recovery"),
                new Tag().name("Mentorship").description("Mentorship requests between students and alumni"),
                new Tag().name("Alumni").description("Alumni verification and profile"),
                new Tag().name("Admin Verifications").description("Admin review of alumni verifications"),
                new Tag().name("Communities").description("Community tree and lookup"),
                new Tag().name("Community Follows").description("Follow / unfollow communities"),
                new Tag().name("Admin Communities").description("Admin community management"),
                new Tag().name("Reports").description("User content reports"),
                new Tag().name("Admin Reports").description("Admin report moderation"),
                new Tag().name("Notifications").description("User notifications"),
                new Tag().name("Courses").description("Course catalog and detail"),
                new Tag().name("Admin Dashboard").description("Admin metrics overview"),
                new Tag().name("Admin Users").description("Admin user ban and role management")
            ));
    }
}
