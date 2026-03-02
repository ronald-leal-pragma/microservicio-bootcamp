package com.pragma.bootcamp.application.mappers;

import com.pragma.bootcamp.application.dtos.requests.BootcampRequest;
import com.pragma.bootcamp.application.dtos.responses.BootcampResponse;
import com.pragma.bootcamp.domain.models.Bootcamp;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BootcampMapper {
    Bootcamp toDomain(BootcampRequest request);
    BootcampResponse toResponse(Bootcamp bootcamp);
}
