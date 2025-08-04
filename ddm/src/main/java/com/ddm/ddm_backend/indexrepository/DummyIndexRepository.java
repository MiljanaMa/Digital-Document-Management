package com.ddm.ddm_backend.indexrepository;

import com.ddm.ddm_backend.indexmodel.IndexUnit;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyIndexRepository
    extends ElasticsearchRepository<IndexUnit, String> {
}
