package com.ddm.ddm_backend.dto;

import com.ddm.ddm_backend.indexmodel.DummyIndex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SearchResultDTO {
    private DummyIndex index;
    private Map<String, List<String>> highlights;
}
