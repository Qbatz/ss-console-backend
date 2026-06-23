package com.smartstay.console.services;

import com.smartstay.console.dao.SmartstayFeatures;
import com.smartstay.console.repositories.SmartstayFeaturesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SmartstayFeatureService {

    @Autowired
    private SmartstayFeaturesRepository smartstayFeaturesRepository;

    public List<SmartstayFeatures> getAllCommonFeatures() {
        return smartstayFeaturesRepository.findAllByIsCommonTrueAndIsActiveTrue();
    }

    public List<SmartstayFeatures> getAllSmartstayFeatures() {
        return smartstayFeaturesRepository.findAllByIsActiveTrue();
    }

    public List<SmartstayFeatures> getSmartstayFeaturesByIds(Set<Long> smartstayFeatureIds) {
        return smartstayFeaturesRepository.findAllByIdInAndIsActiveTrue(smartstayFeatureIds);
    }

    public SmartstayFeatures getSmartstayFeatureById(Long smartstayFeatureId) {
        return smartstayFeaturesRepository.findByIdAndIsActiveTrue(smartstayFeatureId);
    }

    public SmartstayFeatures save(SmartstayFeatures smartstayFeatures) {
        return smartstayFeaturesRepository.save(smartstayFeatures);
    }

    public boolean existsByFeatureName(String featureName) {
        return smartstayFeaturesRepository.existsByFeatureNameAndIsActiveTrue(featureName);
    }

    public boolean existsByFeatureNameAndNotInId(String featureName, Long smartstayFeatureId) {
        return smartstayFeaturesRepository.existsByFeatureNameAndIdNotAndIsActiveTrue(featureName, smartstayFeatureId);
    }
}
