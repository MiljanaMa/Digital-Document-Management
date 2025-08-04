package com.ddm.ddm_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationDTO {
        private double lon;
        private double lat;
        private String address;
        private boolean success;
}
