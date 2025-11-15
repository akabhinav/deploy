package com.paas.core.mapper;

import com.paas.common.dto.EnvironmentDTO;
import com.paas.core.entity.Environment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnvironmentMapper {

    EnvironmentDTO toDTO(Environment environment);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Environment toEntity(EnvironmentDTO dto);
}
