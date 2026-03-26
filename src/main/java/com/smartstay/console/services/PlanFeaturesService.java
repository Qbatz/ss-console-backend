package com.smartstay.console.services;

import com.smartstay.console.dao.PlanFeatures;
import com.smartstay.console.repositories.PlanFeaturesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PlanFeaturesService {

    @Autowired
    private PlanFeaturesRepository planFeaturesRepository;

    public List<PlanFeatures> findAllByIds(Set<Long> ids){
        return planFeaturesRepository.findAllByIdInAndIsActiveTrue(ids);
    }
}
