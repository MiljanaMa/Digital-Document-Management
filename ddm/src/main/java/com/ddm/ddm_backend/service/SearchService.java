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
import joptsimple.internal.Strings;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            new Highlight(List.of(new HighlightField("content"), new HighlightField("incidentSeverity"),
                    new HighlightField("employeeFullName"), new HighlightField("affectedOrganizationName"),
                    new HighlightField("securityOrganizationName")));

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
    public Page<SearchResultDTO> simpleSearch(List<String> keywords, Pageable pageable, boolean isKNN) {
        if (isKNN) {
            try {
                return searchByVector(VectorizationUtil.getEmbedding(Strings.join(keywords, " ")), keywords);
            } catch (TranslateException e) {
                log.error("Vectorization failed");
                return Page.empty();
            }
        }
        var searchQueryBuilder =
                new NativeQueryBuilder().withQuery(buildSimpleSearchQuery(keywords))
                        .withPageable(pageable)
                        .withHighlightQuery(new HighlightQuery(highlighter, null))
                        .build();
        return mapResults(searchQueryBuilder);
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
    public Page<SearchResultDTO> searchByVector(float[] queryVector, List<String> keywords) {
        Float[] floatObjects = new Float[queryVector.length];
        for (int i = 0; i < queryVector.length; i++) {
            floatObjects[i] = queryVector[i];
        }
        List<Float> floatList = Arrays.stream(floatObjects).collect(Collectors.toList());

        var knnQuery = new KnnQuery.Builder()
                .field("vectorizedContent")
                .queryVector(floatList)
                .numCandidates(100L)
                .boost(10.0f)
                .build();
        String searchText = String.join(" ", keywords);

        Query textQuery = MatchQuery.of(mq -> mq
                .field("content")
                .query(searchText)
        )._toQuery();

        Query combinedQuery = BoolQuery.of(b -> b
                .must(knnQuery._toQuery())
                .should(textQuery)
        )._toQuery();

        var searchQuery = NativeQuery.builder()
                .withQuery(combinedQuery)
                .withMaxResults(5)
                .withSearchType(null)
                .withHighlightQuery(new HighlightQuery(highlighter, null))
                .build();
        return mapResults(searchQuery);
    }
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
    private Query buildSimpleSearchQuery(List<String> tokens) {
        return BoolQuery.of(q -> q.must(mb -> mb.bool(b -> {
            tokens.forEach(token -> {
                boolean isPhrase = token.startsWith("\"") && token.endsWith("\"");
                String finalValue = isPhrase
                        ? token.substring(1, token.length() - 1)
                        : token;
                if (isPhrase) {
                    b.should(sb -> sb.matchPhrase(m -> m.field("content").query(finalValue)));

                    b.should(sb -> sb.matchPhrase(m -> m.field("affectedOrganizationName").query(finalValue)));
                    b.should(sb -> sb.matchPhrase(m -> m.field("securityOrganizationName").query(finalValue)));
                    b.should(sb -> sb.matchPhrase(m -> m.field("employeeFullName").query(finalValue)));
                    b.should(sb -> sb.term(m -> m.field("incidentSeverity").value(finalValue)));

                } else {
                    b.should(sb -> sb.match(m -> m.field("content").query(finalValue).boost(0.5f)));
                    b.should(sb -> sb.match(m -> m.field("affectedOrganizationName").query(finalValue)));
                    b.should(sb -> sb.match(m -> m.field("securityOrganizationName").query(finalValue)));
                    b.should(sb -> sb.match(m -> m.field("employeeFullName").query(finalValue)));
                    b.should(sb -> sb.term(m -> m.field("incidentSeverity").value(finalValue)));
                }
            });
            return b;
        })))._toQuery();
    }
}
