package com.pragma.bootcamp.infrastructure.webclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateBatchRequest {
    private Set<Long> ids;
}
