package com.luc.qa.module.answer.mapper;

import com.luc.qa.module.answer.dto.AnswerResponseDTO;
import com.luc.qa.module.answer.dto.AnswerTreeNodeDTO;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.user.dto.PublicAuthorDTO;
import com.luc.qa.module.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    @Mapping(target = "body", expression = "java(mapBody(answer))")
    @Mapping(target = "author", expression = "java(mapPublicAuthor(answer))")
    @Mapping(target = "replies", ignore = true)
    AnswerTreeNodeDTO toTreeNode(Answer answer);

    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "parentAnswerId", source = "parentAnswer.id")
    @Mapping(target = "body", expression = "java(mapBody(answer))")
    @Mapping(target = "author", expression = "java(mapPublicAuthor(answer))")
    AnswerResponseDTO toResponse(Answer answer);

    default String mapBody(Answer answer) {
        if (answer.isDeleted()) {
            return "[deleted]";
        }
        return answer.getBody();
    }

    default PublicAuthorDTO mapPublicAuthor(Answer answer) {
        if (answer.isDeleted()) {
            return PublicAuthorDTO.builder()
                .displayName("[deleted]")
                .build();
        }
        if (answer.isAnonymous()) {
            return PublicAuthorDTO.builder()
                .displayName("Anonymous")
                .build();
        }
        User author = answer.getAuthor();
        return PublicAuthorDTO.builder()
            .id(author.getId())
            .displayName(author.getDisplayName())
            .role(author.getRole())
            .avatarUrl(author.getAvatarUrl())
            .build();
    }
}
