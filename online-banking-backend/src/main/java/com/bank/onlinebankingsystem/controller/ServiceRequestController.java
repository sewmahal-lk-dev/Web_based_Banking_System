package com.bank.onlinebankingsystem.controller;

import com.bank.onlinebankingsystem.entity.ServiceRequest;
import com.bank.onlinebankingsystem.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
@CrossOrigin(origins = "*")
public class ServiceRequestController {

    @Autowired
    private ServiceRequestService serviceRequestService;

    @PostMapping
    public ResponseEntity<ServiceRequest> createRequest(@RequestBody ServiceRequest request) {
        return ResponseEntity.ok(serviceRequestService.createRequest(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ServiceRequest>> getRequests(@PathVariable Long userId) {
        return ResponseEntity.ok(serviceRequestService.getRequestsByUserId(userId));
    }
}