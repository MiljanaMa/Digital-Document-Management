package com.ddm.ddm_backend.indexmodel;

import com.ddm.ddm_backend.model.enums.IncidentSeverityLevel;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "dummy_index")
@Setting(settingPath = "/configuration/serbian-analyzer-config.json")
public class IndexUnit {

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

    @Field(type = FieldType.Text, store = true, analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String employeeName;

    @Field(type = FieldType.Text, store = true, analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String employeeSurname;

    @Field(type = FieldType.Text, store = true, analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String securityOrganizationName;
    @Field(type = FieldType.Text, store = true, analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String affectedOrganizationName;

    @Field(type = FieldType.Text, store = true, analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private IncidentSeverityLevel incidentSeverity;

    @Field(type = FieldType.Text, store = true, analyzer = "serbian_custom", searchAnalyzer = "serbian_custom")
    private String address;

    @Field(store = true)
    private GeoPoint location;
}
