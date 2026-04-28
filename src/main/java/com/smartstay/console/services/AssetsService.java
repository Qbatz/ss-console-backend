package com.smartstay.console.services;

import com.smartstay.console.dao.AssetsV1;
import com.smartstay.console.repositories.AssetsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetsService {

    @Autowired
    private AssetsRepository assetsRepository;

    public void deleteAll(List<AssetsV1> listAssets) {
        assetsRepository.deleteAll(listAssets);
    }

    public List<AssetsV1> findByHostelId(String hostelId) {
        return assetsRepository.findByHostelId(hostelId);
    }
}
