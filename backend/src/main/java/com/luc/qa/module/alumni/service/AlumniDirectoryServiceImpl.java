package com.luc.qa.module.alumni.service;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.alumni.dto.AlumniDirectoryEntryDTO;
import com.luc.qa.module.alumni.dto.AlumniDirectoryFilterDTO;
import com.luc.qa.module.alumni.entity.AlumniProfile;
import com.luc.qa.module.alumni.repository.AlumniProfileRepository;
import com.luc.qa.module.alumni.specification.AlumniDirectorySpecifications;
import com.luc.qa.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlumniDirectoryServiceImpl implements AlumniDirectoryService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final AlumniProfileRepository alumniProfileRepository;

    @Override
    public PageResponseDTO<AlumniDirectoryEntryDTO> list(AlumniDirectoryFilterDTO filter) {
        int page = Math.max(filter.getPage(), 0);
        int size = filter.getSize() <= 0 ? DEFAULT_SIZE : Math.min(filter.getSize(), MAX_SIZE);
        PageRequest pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "gradYear").and(Sort.by("user.displayName"))
        );

        Page<AlumniProfile> result = alumniProfileRepository.findAll(
            AlumniDirectorySpecifications.fromFilter(filter),
            pageable
        );

        return PageResponseDTO.<AlumniDirectoryEntryDTO>builder()
            .content(result.getContent().stream().map(this::toEntry).toList())
            .page(result.getNumber())
            .size(result.getSize())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .build();
    }

    private AlumniDirectoryEntryDTO toEntry(AlumniProfile profile) {
        User user = profile.getUser();
        return AlumniDirectoryEntryDTO.builder()
            .userId(user.getId())
            .displayName(user.getDisplayName())
            .avatarUrl(blankToNull(user.getAvatarUrl()))
            .gradYear(profile.getGradYear())
            .faculty(profile.getFaculty())
            .degree(profile.getDegree())
            .major(blankToNull(profile.getMajor()))
            .currentPosition(blankToNull(profile.getCurrentPosition()))
            .currentCompany(blankToNull(profile.getCurrentCompany()))
            .linkedinUrl(blankToNull(profile.getLinkedinUrl()))
            .build();
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
