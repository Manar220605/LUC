package com.luc.qa.module.answer.mapper;

import com.luc.qa.module.answer.dto.AnswerResponseDTO;
import com.luc.qa.module.answer.dto.AnswerTreeNodeDTO;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.user.dto.PublicAuthorDTO;
import com.luc.qa.module.user.service.PublicAuthorService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class AnswerMapper {

    @Autowired
    protected PublicAuthorService publicAuthorService;

    @Mapping(target = "body", expression = "java(mapBody(answer))")
    @Mapping(target = "author", expression = "java(mapPublicAuthor(answer))")
    @Mapping(target = "replies", ignore = true)
    public abstract AnswerTreeNodeDTO toTreeNode(Answer answer);

    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "parentAnswerId", source = "parentAnswer.id")
    @Mapping(target = "body", expression = "java(mapBody(answer))")
    @Mapping(target = "author", expression = "java(mapPublicAuthor(answer))")
    public abstract AnswerResponseDTO toResponse(Answer answer);

    protected String mapBody(Answer answer) {
        if (answer.isDeleted()) {
            return "[deleted]";
        }
        return answer.getBody();
    }

    protected PublicAuthorDTO mapPublicAuthor(Answer answer) {
        if (answer.isDeleted()) {
            return publicAuthorService.deleted();
        }
        if (answer.isAnonymous()) {
            return publicAuthorService.anonymous();
        }
        return publicAuthorService.fromUser(answer.getAuthor());
    }
}
