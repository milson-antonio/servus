package com.milsondev.servus.services;

import com.milsondev.servus.db.entities.ServiceTypeEntity;
import com.milsondev.servus.db.repositories.ServiceTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceTypeService(ServiceTypeRepository serviceTypeRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
    }

    public List<ServiceTypeEntity> findAll() {
        return serviceTypeRepository.findAll();
    }

    public Optional<ServiceTypeEntity> findByName(final String name){
        return serviceTypeRepository.findByName(name);
    }
}
