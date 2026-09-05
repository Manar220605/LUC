package com.luc.qa.module.announcement.service;

import com.luc.qa.common.exception.AnnouncementNotFoundException;
import com.luc.qa.module.announcement.dto.CreateAnnouncementRequestDTO;
import com.luc.qa.module.announcement.dto.UpdateAnnouncementRequestDTO;
import com.luc.qa.module.announcement.entity.Announcement;
import com.luc.qa.module.announcement.repository.AnnouncementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Transactional(readOnly = true)
    public List<Announcement> listAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    public Announcement create(CreateAnnouncementRequestDTO request) {
        Announcement announcement = Announcement.builder()
            .title(request.getTitle().trim())
            .body(request.getBody())
            .published(request.getPublished() == null ? true : request.getPublished())
            .build();
        return announcementRepository.save(announcement);
    }

    public Announcement update(Long id, UpdateAnnouncementRequestDTO request) {
        Announcement announcement = announcementRepository.findById(id)
            .orElseThrow(() -> new AnnouncementNotFoundException(id));
        announcement.setTitle(request.getTitle().trim());
        announcement.setBody(request.getBody());
        if (request.getPublished() != null) {
            announcement.setPublished(request.getPublished());
        }
        return announcementRepository.save(announcement);
    }

    public void delete(Long id) {
        Announcement announcement = announcementRepository.findById(id)
            .orElseThrow(() -> new AnnouncementNotFoundException(id));
        announcementRepository.delete(announcement);
    }
}
