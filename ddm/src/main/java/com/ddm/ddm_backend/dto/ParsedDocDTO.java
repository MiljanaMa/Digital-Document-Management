package com.ddm.ddm_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParsedDocDTO {
    private String title;
    private String employeeFullName;
    private String securityOrganizationName;
    private String affectedOrganizationName;
    private String incidentSeverity;
    private String affectedOrganizationAddress;
    private String documentId;
}
