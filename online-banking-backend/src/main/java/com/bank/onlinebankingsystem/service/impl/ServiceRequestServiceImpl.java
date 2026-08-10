package com.bank.onlinebankingsystem.service.impl;

import com.bank.onlinebankingsystem.entity.ServiceRequest;
import com.bank.onlinebankingsystem.repository.ServiceRequestRepository;
import com.bank.onlinebankingsystem.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceRequestServiceImpl implements ServiceRequestService {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Override
    public ServiceRequest createRequest(ServiceRequest request) {
        return serviceRequestRepository.save(request);
    }

    @Override
    public List<ServiceRequest> getRequestsByUserId(Long userId) {
        return serviceRequestRepository.findByUserId(userId);
    }
}