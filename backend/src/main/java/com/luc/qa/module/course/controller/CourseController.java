package com.luc.qa.module.course.controller;

import com.luc.qa.module.course.dto.CourseDetailDTO;
import com.luc.qa.module.course.dto.CourseSummaryDTO;
import com.luc.qa.module.course.service.CourseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public List<CourseSummaryDTO> list() {
        return courseService.listAll();
    }

    @GetMapping("/{code}")
    public CourseDetailDTO get(@PathVariable String code) {
        return courseService.getByCode(code, resolveKeycloakId());
    }

    private String resolveKeycloakId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getSubject();
        }
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null;
    }
}
