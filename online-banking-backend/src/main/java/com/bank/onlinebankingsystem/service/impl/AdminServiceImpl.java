package com.bank.onlinebankingsystem.service.impl;

import com.bank.onlinebankingsystem.entity.Admin;
import com.bank.onlinebankingsystem.repository.AdminRepository;
import com.bank.onlinebankingsystem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public Admin registerAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    @Override
    public Admin getAdminById(Long adminId) {
        return adminRepository.findById(adminId).orElse(null);
    }
}