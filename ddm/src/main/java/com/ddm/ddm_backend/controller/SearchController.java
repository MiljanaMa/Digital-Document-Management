package com.ddm.ddm_backend.controller;

import ai.djl.translate.TranslateException;
import com.ddm.ddm_backend.dto.SearchDTO;
import com.ddm.ddm_backend.dto.SearchQueryDTO;
import com.ddm.ddm_backend.dto.SearchResultDTO;
import com.ddm.ddm_backend.indexmodel.DummyIndex;
import com.ddm.ddm_backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/simple")
    public Page<SearchResultDTO> simpleSearch(@RequestBody SearchDTO searchDTO, Pageable pageable) throws TranslateException {
        return searchService.simpleSearch(searchDTO, pageable);
    }
    @PostMapping("/simple1")
    public Page<SearchResultDTO> simpleSearch(@RequestParam Boolean isKnn,
                                         @RequestBody SearchQueryDTO simpleSearchQuery,
                                         Pageable pageable) {
        return searchService.simpleSearch(simpleSearchQuery.keywords(), pageable, isKnn);
    }

    @PostMapping("/advanced")
    public Page<SearchResultDTO> advancedSearch(@RequestBody SearchQueryDTO advancedSearchQuery,
                                           Pageable pageable) {
        try {
            return searchService.advancedSearch(advancedSearchQuery, pageable);
        } catch (IOException e) {
            return Page.empty(pageable);
        }
    }
}