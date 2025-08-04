package com.example.gcesapi.repository;

import com.example.gcesapi.model.SubDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubDistrictRepository extends JpaRepository<SubDistrict, Long> {
}
