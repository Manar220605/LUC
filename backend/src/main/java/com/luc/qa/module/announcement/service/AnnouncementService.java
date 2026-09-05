package com.luc.qa.module.announcement.service;

import com.luc.qa.module.announcement.entity.Announcement;
import com.luc.qa.module.announcement.repository.AnnouncementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public List<Announcement> listPublished() {
        return announcementRepository.findAllByPublishedTrueOrderByCreatedAtDesc();
    }
}
