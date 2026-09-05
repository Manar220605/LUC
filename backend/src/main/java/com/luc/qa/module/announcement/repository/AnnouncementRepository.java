package com.luc.qa.module.announcement.repository;

import com.luc.qa.module.announcement.entity.Announcement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByPublishedTrueOrderByCreatedAtDesc();

    List<Announcement> findAllByOrderByCreatedAtDesc();
}
