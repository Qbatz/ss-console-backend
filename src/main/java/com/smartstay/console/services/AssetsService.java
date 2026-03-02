package com.smartstay.console.services;

import com.smartstay.console.repositories.AssetsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetsService {
    @Autowired
    private AssetsRepository assetsRepository;
}
