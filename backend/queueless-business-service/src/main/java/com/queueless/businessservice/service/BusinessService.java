package com.queueless.businessservice.service;

import com.queueless.businessservice.dto.request.BusinessRequest;
import com.queueless.businessservice.dto.response.BusinessResponse;
import com.queueless.businessservice.entity.Business;
import com.queueless.businessservice.entity.BusinessCategory;
import com.queueless.businessservice.exception.ResourceNotFoundException;
import com.queueless.businessservice.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessService {

    private final BusinessRepository businessRepository;

    public List<BusinessResponse> getAll(BusinessCategory category, String city, Boolean open) {
        List<Business> results;

        if (category != null && city != null && open != null) {
            results = businessRepository.findByCategoryAndCityIgnoreCaseAndOpen(category, city, open);
        } else if (category != null && city != null) {
            results = businessRepository.findByCategoryAndCityIgnoreCase(category, city);
        } else if (category != null && open != null) {
            results = businessRepository.findByCategoryAndOpen(category, open);
        } else if (city != null && open != null) {
            results = businessRepository.findByCityIgnoreCaseAndOpen(city, open);
        } else if (category != null) {
            results = businessRepository.findByCategory(category);
        } else if (city != null) {
            results = businessRepository.findByCityIgnoreCase(city);
        } else if (open != null) {
            results = businessRepository.findByOpen(open);
        } else {
            results = businessRepository.findAll();
        }

        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BusinessResponse getById(UUID id) {
        return toResponse(findBusiness(id));
    }

    public BusinessResponse create(BusinessRequest request) {
        Business business = Business.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .phone(request.getPhone())
                .averageServiceTime(request.getAverageServiceTime())
                .open(false)
                .build();
        Business saved = businessRepository.save(business);
        log.info("Business created: {} ({})", saved.getName(), saved.getId());
        return toResponse(saved);
    }

    public BusinessResponse update(UUID id, BusinessRequest request) {
        Business business = findBusiness(id);
        business.setName(request.getName());
        business.setCategory(request.getCategory());
        business.setDescription(request.getDescription());
        business.setAddress(request.getAddress());
        business.setCity(request.getCity());
        business.setPhone(request.getPhone());
        business.setAverageServiceTime(request.getAverageServiceTime());
        return toResponse(businessRepository.save(business));
    }

    public void delete(UUID id) {
        findBusiness(id);
        businessRepository.deleteById(id);
        log.info("Business deleted: {}", id);
    }

    public BusinessResponse setOpenStatus(UUID id, boolean open) {
        Business business = findBusiness(id);
        business.setOpen(open);
        return toResponse(businessRepository.save(business));
    }

    private Business findBusiness(UUID id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + id));
    }

    public BusinessResponse toResponse(Business b) {
        return BusinessResponse.builder()
                .id(b.getId()).name(b.getName()).category(b.getCategory())
                .description(b.getDescription()).address(b.getAddress())
                .city(b.getCity()).phone(b.getPhone())
                .averageServiceTime(b.getAverageServiceTime())
                .open(b.isOpen()).createdAt(b.getCreatedAt())
                .build();
    }
}
