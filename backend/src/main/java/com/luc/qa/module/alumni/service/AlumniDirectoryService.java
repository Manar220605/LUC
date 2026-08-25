package com.luc.qa.module.alumni.service;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.alumni.dto.AlumniDirectoryEntryDTO;
import com.luc.qa.module.alumni.dto.AlumniDirectoryFilterDTO;

public interface AlumniDirectoryService {

    PageResponseDTO<AlumniDirectoryEntryDTO> list(AlumniDirectoryFilterDTO filter);
}
