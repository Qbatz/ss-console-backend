package com.smartstay.console.repositories;

import com.smartstay.console.dao.SmartstayFeatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SmartstayFeaturesRepository extends JpaRepository<SmartstayFeatures, Long> {

    SmartstayFeatures findByFeatureName(String name);

    List<SmartstayFeatures> findAllByIsCommonTrueAndIsActiveTrue();

    List<SmartstayFeatures> findAllByIsActiveTrue();

    SmartstayFeatures findByIdAndIsActiveTrue(Long smartstayFeatureId);

    List<SmartstayFeatures> findAllByIdInAndIsActiveTrue(Set<Long> smartstayFeatureIds);
}
