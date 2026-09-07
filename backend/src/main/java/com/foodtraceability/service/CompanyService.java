package com.foodtraceability.service;

import com.foodtraceability.entity.Company;
import java.util.List;

public interface CompanyService {
    Company createCompany(String name, String contactPhone, String address);
    Company updateCompany(Long id, String name, String contactPhone, String address);
    void deleteCompany(Long id);
    Company getCompany(Long id);
    List<Company> listAllCompanies();
}
