package com.catolicasc.agrbackend.feature.issue.mapper;

import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.issue.dto.IssueDTO;
import com.catolicasc.agrbackend.shared.mapper.MapperBase;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Component;

@Component
public class IssueMapper {

    private final MapperFactory mapperFactory = MapperBase.getInstance().getMapperFactory();

    public Issue toDomain(IssueDTO dto){
        mapperFactory.classMap(IssueDTO.class, Issue.class);
        return mapperFactory.getMapperFacade().map(dto, Issue.class);
    }

}