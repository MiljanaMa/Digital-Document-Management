package com.ddm.ddm_backend.service;

import ai.djl.translate.TranslateException;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.ddm.ddm_backend.dto.SearchDTO;
import com.ddm.ddm_backend.dto.SearchQueryDTO;
import com.ddm.ddm_backend.dto.SearchResultDTO;
import com.ddm.ddm_backend.indexmodel.DummyIndex;
import com.ddm.ddm_backend.exceptionhandling.exception.MalformedQueryException;
import com.ddm.ddm_backend.util.AdvancedQueryUtil;
import com.ddm.ddm_backend.util.LocationIqClient;
import com.ddm.ddm_backend.util.VectorizationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ElasticsearchOperations elasticsearchTemplate;
    private final LocationIqClient locationIqClient;
    @Value("${location.api.key}")
    private String apiKey;
    @Autowired
    private AdvancedQueryUtil advancedQueryUtil;
    private static final Highlight highlighter =
            new Highlight(List.of(new HighlightField("content")/*, new HighlightField("incidentSeverity"),
                    new HighlightField("employeeFullName"), new HighlightField("affectedOrganizationName"),
                    new HighlightField("securityOrganizationName")*/));

    public Page<SearchResultDTO> simpleSearch(SearchDTO searchDTO, Pageable pageable) throws TranslateException {
        List<Query> queries = new ArrayList<>();

        if (searchDTO.getEmployeeFullName() != null && !searchDTO.getEmployeeFullName().isEmpty()) {
            queries.add(getQuery("employeeFullName", searchDTO.getEmployeeFullName()));
        }
        if (searchDTO.getIncidentSeverity() != null && !searchDTO.getIncidentSeverity().isEmpty()) {
            queries.add(getQuery("incidentSeverity", searchDTO.getIncidentSeverity()));
        }
        if (searchDTO.getSecurityOrganizationName() != null && !searchDTO.getSecurityOrganizationName().isEmpty()) {
            queries.add(getQuery("securityOrganizationName", searchDTO.getSecurityOrganizationName()));
        }
        if (searchDTO.getAffectedOrganizationName() != null && !searchDTO.getAffectedOrganizationName().isEmpty()) {
            queries.add(getQuery("affectedOrganizationName", searchDTO.getAffectedOrganizationName()));
        }

        var address = searchDTO.getAddress();
        var radius = searchDTO.getRadius();
        String distance = radius + searchDTO.getUnit();
        if (address != null && !address.isEmpty() && radius != null && !radius.isEmpty()) {
            Query query = getLocationQuery(address, distance);
            queries.add(query);
        }

        if (searchDTO.getText() != null && !searchDTO.getText().isEmpty()) {
            if (searchDTO.isKnn()) {
                NativeQuery searchQuery = getKnnQuery(searchDTO, pageable, queries);
                return mapResults(searchQuery);
            } else {
                queries.add(getQuery("content", searchDTO.getText()));
            }
        }

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b.must(queries)))
                .withHighlightQuery(new HighlightQuery(highlighter, null))
                .withPageable(pageable)
                .build();

        return mapResults(searchQuery);
        }

    @NotNull
    private static NativeQuery getKnnQuery(SearchDTO searchDTO, Pageable pageable, List<Query> queries) throws TranslateException {
        float[] queryVector = VectorizationUtil.getEmbedding(searchDTO.getText());

        List<Float> queryVectorList = new ArrayList<>();
        for (float f : queryVector) {
            queryVectorList.add(f);
        }

        KnnQuery  knnQuery = new KnnQuery.Builder()
                .field("vectorizedContent")
                .queryVector(queryVectorList)
                .numCandidates(100L)
                .boost(10.0f)
                .build();

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b.must(queries)))
                .withHighlightQuery(new HighlightQuery(highlighter, null))
                .withKnnQuery(knnQuery)
                .withPageable(pageable)
                .build();
        //.withMaxResults(5)
        //.withSearchType(null)
        return searchQuery;
    }

    private Query getLocationQuery(String address, String distance) {
        var location = locationIqClient.forwardGeolocation(
                apiKey, address, "json").get(0);
        GeoPoint geoPoint = new GeoPoint(Double.parseDouble(location.getLat()), Double.parseDouble(location.getLon()));
        GeoLocation geoLocation = GeoLocation.of(l -> l
                .latlon(latLon -> latLon
                        .lat(geoPoint.getLat())
                        .lon(geoPoint.getLon())));
        Query query = GeoDistanceQuery.of(g -> g
                .field("location")
                .location(geoLocation)
                .distance(distance)
        )._toQuery();
        return query;
    }

    private Page<SearchResultDTO> mapResults(NativeQuery searchQuery) {
        var searchHits = elasticsearchTemplate.search(searchQuery, DummyIndex.class, IndexCoordinates.of("dummy_index"));
        var searchHitsPaged = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());

        List<SearchResultDTO> dtoList = searchHitsPaged.getContent().stream().map(hit -> {
            DummyIndex doc = hit.getContent();
            return new SearchResultDTO(doc, hit.getHighlightFields());
        }).toList();

        return new PageImpl<>(dtoList, searchQuery.getPageable(), searchHitsPaged.getTotalElements());
    }
    private Query getQuery(String field, String value) {
        boolean isPhrase = value.startsWith("\"") && value.endsWith("\"");
        String finalValue = isPhrase
                ? value.substring(1, value.length() - 1)
                : value;

        return isPhrase
                ? MatchPhraseQuery.of(m -> m.field(field).query(finalValue))._toQuery()
                : MatchQuery.of(m -> m.field(field).query(finalValue))._toQuery();
    }
/*
    public Page<SearchResultDTO> searchByVector(float[] queryVector) {
        Float[] floatObjects = new Float[queryVector.length];
        for (int i = 0; i < queryVector.length; i++) {
            floatObjects[i] = queryVector[i];
        }
        List<Float> floatList = Arrays.stream(floatObjects).collect(Collectors.toList());

        var knnQuery = new KnnQuery.Builder()
                .field("vectorizedContent")
                .queryVector(floatList)
                .numCandidates(100)
                .k(10)
                .boost(10.0f)
                .build();

        var searchQuery = NativeQuery.builder()
            .withKnnQuery(knnQuery)
            .withMaxResults(5)
            .withSearchType(null)
            .build();

        var searchHitsPaged =
                SearchHitSupport.searchPageFor(
                        elasticsearchTemplate.search(searchQuery, DummyIndex.class),
                        searchQuery.getPageable());

        List<SearchResultDTO> dtoList = searchHitsPaged.getContent().stream().map(hit -> {
            DummyIndex doc = hit.getContent();
            SearchResultDTO dto = new SearchResultDTO();
            dto.setTitle(doc.getTitle());
            dto.setServerFilename(doc.getServerFilename());
            return dto;
        }).toList();

        return new PageImpl<>(dtoList, searchQuery.getPageable(), searchHitsPaged.getTotalElements());
    }*/
    public Page<SearchResultDTO> advancedSearch(SearchQueryDTO searchQueryDTO, Pageable pageable) throws IOException {
        List<String> postfix = advancedQueryUtil.toPostfix(searchQueryDTO.keywords());
        Query query = advancedQueryUtil.buildQuery(postfix);

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(query)
                .withHighlightQuery(new HighlightQuery(highlighter, null))
                .withPageable(pageable)
                .build();

        return mapResults(searchQuery);
    }

    public Page<DummyIndex> advancedSearch(List<String> expression, Pageable pageable) {
        if (expression.size() != 3) {
            throw new MalformedQueryException("Search query malformed.");
        }

        String operation = expression.get(1);
        expression.remove(1);
        var searchQueryBuilder =
            new NativeQueryBuilder().withQuery(buildAdvancedSearchQuery(expression, operation))
                .withPageable(pageable);

        return runQuery(searchQueryBuilder.build());
    }
    private Query buildAdvancedSearchQuery(List<String> operands, String operation) {
        return BoolQuery.of(q -> q.must(mb -> mb.bool(b -> {
            var field1 = operands.get(0).split(":")[0];
            var value1 = operands.get(0).split(":")[1];
            var field2 = operands.get(1).split(":")[0];
            var value2 = operands.get(1).split(":")[1];

            switch (operation) {
                case "AND":
                    b.must(sb -> sb.match(
                        m -> m.field(field1).fuzziness(Fuzziness.ONE.asString()).query(value1)));
                    b.must(sb -> sb.match(m -> m.field(field2).query(value2)));
                    break;
                case "OR":
                    b.should(sb -> sb.match(
                        m -> m.field(field1).fuzziness(Fuzziness.ONE.asString()).query(value1)));
                    b.should(sb -> sb.match(m -> m.field(field2).query(value2)));
                    break;
                case "NOT":
                    b.must(sb -> sb.match(
                        m -> m.field(field1).fuzziness(Fuzziness.ONE.asString()).query(value1)));
                    b.mustNot(sb -> sb.match(m -> m.field(field2).query(value2)));
                    break;
            }
            return b;
        })))._toQuery();
    }

    private Page<DummyIndex> runQuery(NativeQuery searchQuery) {

        var searchHits = elasticsearchTemplate.search(searchQuery, DummyIndex.class,
            IndexCoordinates.of("dummy_index"));

        var searchHitsPaged = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());

        return (Page<DummyIndex>) SearchHitSupport.unwrapSearchHits(searchHitsPaged);
    }
}
