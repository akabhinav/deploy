package com.paas.core.mapper;

import com.paas.common.dto.DeploymentDTO;
import com.paas.core.entity.Deployment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeploymentMapper {

    @Mapping(target = "applicationName", ignore = true)
    DeploymentDTO toDTO(Deployment deployment);

    @Mapping(target = "createdAt", ignore = true)
    Deployment toEntity(DeploymentDTO dto);
}
