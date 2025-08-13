package com.example.gcesapi.repository;

import com.example.gcesapi.model.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageRepository extends JpaRepository<Village, Long> {
    Page<Village> findByVillageNameContainingIgnoreCase(String villageName, Pageable pageable);
    Page<Village> findByVillageNameContainingIgnoreCaseAndStateLgdCode(String villageName, Long stateLgdCode, Pageable pageable);
    Page<Village> findByStateLgdCode(Long stateLgdCode, Pageable pageable);
}
