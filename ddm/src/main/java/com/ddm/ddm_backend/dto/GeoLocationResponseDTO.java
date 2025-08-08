package com.ddm.ddm_backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
public class GeoLocationResponseDTO {
    @JsonProperty("place_id")
    private String placeId;
    private String lat;
    private String lon;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("class")
    private String clazz; // can't name it "class" in Java
    private String type;
    private Double importance;
}