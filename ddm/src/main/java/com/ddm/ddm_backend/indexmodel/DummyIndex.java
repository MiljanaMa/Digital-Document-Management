package com.ddm.ddm_backend.indexmodel;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "dummy_index")
@Setting(settingPath = "/configuration/serbian-analyzer-config.json")
public class DummyIndex {

    @Id
    private String id;

    @Field(type = FieldType.Text, store = true, name = "title")
    private String title;

    @Field(type = FieldType.Text, store = true, name = "content", analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String content;

    @Field(type = FieldType.Text, store = true, name = "server_filename", index = false)
    private String serverFilename;

    @Field(type = FieldType.Integer, store = true, name = "database_id")
    private Integer databaseId;

    @Field(type = FieldType.Dense_Vector, dims = 384, similarity = "cosine")
    private float[] vectorizedContent;

    @Field(type = FieldType.Text, store = true, name = "employeeFullName", analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String employeeFullName;

    @Field(type = FieldType.Text, store = true, name = "securityOrganizationName", analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String securityOrganizationName;

    @Field(type = FieldType.Text, store = true, name = "affectedOrganizationName", analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String affectedOrganizationName;

    @Field(type = FieldType.Text, store = true, name = "incidentSeverity", analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String incidentSeverity;

    @Field(type = FieldType.Text, store = true, name = "affectedOrganizationAddress", analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String affectedOrganizationAddress;

    @GeoPointField
    @Field(store = true, name = "location")
    private GeoPoint location;
}
