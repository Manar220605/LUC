package com.luc.qa.module.mentorship.service;

import com.luc.qa.module.mentorship.dto.CreateMentorshipRequestDTO;
import com.luc.qa.module.mentorship.dto.MentorshipInboxDTO;
import com.luc.qa.module.mentorship.dto.MentorshipRequestResponseDTO;
import com.luc.qa.module.mentorship.dto.MentorshipStatusDTO;

public interface MentorshipService {

    MentorshipRequestResponseDTO create(String keycloakId, CreateMentorshipRequestDTO request);

    MentorshipInboxDTO listMine(String keycloakId);

    MentorshipStatusDTO statusWith(String keycloakId, Long alumniUserId);

    MentorshipRequestResponseDTO accept(String keycloakId, Long requestId);

    MentorshipRequestResponseDTO decline(String keycloakId, Long requestId);
}
