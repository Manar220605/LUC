package com.luc.qa.module.question.mapper;

import com.luc.qa.module.community.mapper.CommunityMapper;
import com.luc.qa.module.question.dto.QuestionResponseDTO;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.user.dto.PublicAuthorDTO;
import com.luc.qa.module.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CommunityMapper.class)
public interface QuestionMapper {

    @Mapping(target = "author", expression = "java(mapPublicAuthor(question))")
    @Mapping(target = "communityPath", source = "community.path")
    @Mapping(target = "communityName", source = "community.name")
    QuestionSummaryDTO toSummary(Question question);

    @Mapping(target = "author", expression = "java(mapPublicAuthor(question))")
    @Mapping(target = "community", source = "community")
    QuestionResponseDTO toResponse(Question question);

    default PublicAuthorDTO mapPublicAuthor(Question question) {
        if (question.isAnonymous()) {
            return PublicAuthorDTO.builder()
                .displayName("Anonymous")
                .build();
        }
        User author = question.getAuthor();
        return PublicAuthorDTO.builder()
            .id(author.getId())
            .displayName(author.getDisplayName())
            .role(author.getRole())
            .avatarUrl(author.getAvatarUrl())
            .build();
    }
}
