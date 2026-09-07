package com.foodtraceability.service.impl;

import com.foodtraceability.entity.Company;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.CompanyRepository;
import com.foodtraceability.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {
    private static final Logger log = LoggerFactory.getLogger(CompanyServiceImpl.class);

    private final CompanyRepository repository;

    public CompanyServiceImpl(CompanyRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Company createCompany(String name, String contactPhone, String address) {
        if (repository.existsByName(name)) {
            throw new BusinessException("公司名称已存在: " + name);
        }
        Company company = new Company();
        company.setName(name);
        company.setContactPhone(contactPhone);
        company.setAddress(address);
        Company saved = repository.save(company);
        log.info("[公司管理] 创建公司 - 名称: {}", name);
        return saved;
    }

    @Override
    @Transactional
    public Company updateCompany(Long id, String name, String contactPhone, String address) {
        Company company = repository.findById(id)
                .orElseThrow(() -> new BusinessException("公司不存在: " + id));
        if (name != null && !name.equals(company.getName()) && repository.existsByName(name)) {
            throw new BusinessException("公司名称已存在: " + name);
        }
        if (name != null) company.setName(name);
        if (contactPhone != null) company.setContactPhone(contactPhone);
        if (address != null) company.setAddress(address);
        Company saved = repository.save(company);
        log.info("[公司管理] 更新公司 - ID: {}", id);
        return saved;
    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        Company company = repository.findById(id)
                .orElseThrow(() -> new BusinessException("公司不存在: " + id));
        repository.delete(company);
        log.info("[公司管理] 删除公司 - ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Company getCompany(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("公司不存在: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Company> listAllCompanies() {
        return repository.findAll();
    }
}
