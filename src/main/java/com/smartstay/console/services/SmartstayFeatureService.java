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

    public List<SmartstayFeatures> getSmartStayFeaturesByIds(Set<Long> smartstayFeatureIds) {
        return smartstayFeaturesRepository.findAllByIdInAndIsActiveTrue(smartstayFeatureIds);
    }
}
