package com.ddm.ddm_backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchDTO {
    private String employeeFullName;
    private String incidentSeverity;
    private String affectedOrganizationName;
    private String securityOrganizationName;
    private String text;
    private boolean knn;
    private String address;
    private String radius;
    @NotEmpty(message = "Unit is required")
    private String unit;
}
