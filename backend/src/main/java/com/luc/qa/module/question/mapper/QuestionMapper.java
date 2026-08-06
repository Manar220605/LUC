package com.luc.qa.module.question.mapper;

import com.luc.qa.module.community.mapper.CommunityMapper;
import com.luc.qa.module.question.dto.QuestionResponseDTO;
import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.user.dto.PublicAuthorDTO;
import com.luc.qa.module.user.service.PublicAuthorService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = CommunityMapper.class)
public abstract class QuestionMapper {

    @Autowired
    protected PublicAuthorService publicAuthorService;

    @Mapping(target = "author", expression = "java(mapPublicAuthor(question))")
    @Mapping(target = "communityPath", source = "community.path")
    @Mapping(target = "communityName", source = "community.name")
    public abstract QuestionSummaryDTO toSummary(Question question);

    @Mapping(target = "author", expression = "java(mapPublicAuthor(question))")
    @Mapping(target = "community", source = "community")
    public abstract QuestionResponseDTO toResponse(Question question);

    protected PublicAuthorDTO mapPublicAuthor(Question question) {
        if (question.isAnonymous()) {
            return publicAuthorService.anonymous();
        }
        return publicAuthorService.fromUser(question.getAuthor());
    }
}
