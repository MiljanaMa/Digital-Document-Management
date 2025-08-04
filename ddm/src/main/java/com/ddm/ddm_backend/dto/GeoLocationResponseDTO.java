package com.ddm.ddm_backend.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationResponseDTO {
    private Double lat;

    private Double lon;
}
