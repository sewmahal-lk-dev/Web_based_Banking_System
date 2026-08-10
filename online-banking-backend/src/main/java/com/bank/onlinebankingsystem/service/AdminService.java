package com.bank.onlinebankingsystem.service;

import com.bank.onlinebankingsystem.entity.Admin;

public interface AdminService {
    Admin registerAdmin(Admin admin);
    Admin getAdminById(Long adminId);
}