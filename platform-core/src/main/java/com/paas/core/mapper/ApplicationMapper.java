package com.paas.core.mapper;

import com.paas.common.dto.ApplicationDTO;
import com.paas.common.dto.ResourceRequirementsDTO;
import com.paas.common.dto.HealthCheckDTO;
import com.paas.core.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "applicationType", source = "applicationType")
    @Mapping(target = "sourceType", source = "sourceType")
    @Mapping(target = "environmentId", source = "environmentId", qualifiedByName = "longToString")
    @Mapping(target = "resources", source = ".", qualifiedByName = "toResourceRequirements")
    @Mapping(target = "healthCheck", source = ".", qualifiedByName = "toHealthCheck")
    ApplicationDTO toDTO(Application application);

    @Mapping(target = "applicationType", source = "applicationType")
    @Mapping(target = "sourceType", source = "sourceType")
    @Mapping(target = "environmentId", source = "environmentId", qualifiedByName = "stringToLong")
    @Mapping(target = "cpuRequest", source = "resources.cpuRequest")
    @Mapping(target = "cpuLimit", source = "resources.cpuLimit")
    @Mapping(target = "memoryRequest", source = "resources.memoryRequest")
    @Mapping(target = "memoryLimit", source = "resources.memoryLimit")
    @Mapping(target = "healthCheckPath", source = "healthCheck.path")
    @Mapping(target = "healthCheckPort", source = "healthCheck.port")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Application toEntity(ApplicationDTO dto);

    @Named("longToString")
    default String longToString(Long value) {
        return value != null ? value.toString() : null;
    }

    @Named("stringToLong")
    default Long stringToLong(String value) {
        return value != null && !value.isEmpty() ? Long.parseLong(value) : null;
    }

    @Named("toResourceRequirements")
    default ResourceRequirementsDTO toResourceRequirements(Application application) {
        if (application == null) {
            return null;
        }
        return ResourceRequirementsDTO.builder()
                .cpuRequest(application.getCpuRequest())
                .cpuLimit(application.getCpuLimit())
                .memoryRequest(application.getMemoryRequest())
                .memoryLimit(application.getMemoryLimit())
                .build();
    }

    @Named("toHealthCheck")
    default HealthCheckDTO toHealthCheck(Application application) {
        if (application == null || application.getHealthCheckPath() == null) {
            return null;
        }
        return HealthCheckDTO.builder()
                .path(application.getHealthCheckPath())
                .port(application.getHealthCheckPort())
                .build();
    }
}
