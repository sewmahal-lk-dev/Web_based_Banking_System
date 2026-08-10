package com.bank.onlinebankingsystem.service;

import com.bank.onlinebankingsystem.entity.ServiceRequest;
import java.util.List;

public interface ServiceRequestService {
    ServiceRequest createRequest(ServiceRequest request);
    List<ServiceRequest> getRequestsByUserId(Long userId);
}