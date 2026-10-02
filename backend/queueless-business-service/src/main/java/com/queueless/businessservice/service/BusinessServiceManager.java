package com.queueless.businessservice.service;

import com.queueless.businessservice.dto.request.ServiceRequest;
import com.queueless.businessservice.dto.response.ServiceResponse;
import com.queueless.businessservice.entity.BusinessService;
import com.queueless.businessservice.exception.ResourceNotFoundException;
import com.queueless.businessservice.repository.BusinessRepository;
import com.queueless.businessservice.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessServiceManager {

    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;

    public List<ServiceResponse> getServicesForBusiness(UUID businessId) {
        ensureBusinessExists(businessId);
        return serviceRepository.findByBusinessIdAndActive(businessId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ServiceResponse addService(UUID businessId, ServiceRequest request) {
        ensureBusinessExists(businessId);
        BusinessService service = BusinessService.builder()
                .businessId(businessId)
                .name(request.getName())
                .description(request.getDescription())
                .estimatedMinutes(request.getEstimatedMinutes())
                .active(true)
                .build();
        return toResponse(serviceRepository.save(service));
    }

    public ServiceResponse updateService(UUID serviceId, ServiceRequest request) {
        BusinessService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + serviceId));
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setEstimatedMinutes(request.getEstimatedMinutes());
        return toResponse(serviceRepository.save(service));
    }

    public void deleteService(UUID serviceId) {
        BusinessService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + serviceId));
        service.setActive(false);
        serviceRepository.save(service);
    }

    private void ensureBusinessExists(UUID businessId) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business not found: " + businessId);
        }
    }

    private ServiceResponse toResponse(BusinessService s) {
        return ServiceResponse.builder()
                .id(s.getId()).businessId(s.getBusinessId())
                .name(s.getName()).description(s.getDescription())
                .estimatedMinutes(s.getEstimatedMinutes()).active(s.isActive())
                .build();
    }
}
