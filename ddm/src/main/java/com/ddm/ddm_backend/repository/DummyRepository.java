package com.ddm.ddm_backend.repository;

import com.ddm.ddm_backend.model.DummyTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyRepository extends JpaRepository<DummyTable, Integer> {
}
