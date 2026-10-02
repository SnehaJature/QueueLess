package com.queueless.businessservice.controller;

import com.queueless.businessservice.dto.request.BusinessRequest;
import com.queueless.businessservice.dto.request.ServiceRequest;
import com.queueless.businessservice.dto.response.BusinessResponse;
import com.queueless.businessservice.dto.response.ServiceResponse;
import com.queueless.businessservice.entity.BusinessCategory;
import com.queueless.businessservice.service.BusinessService;
import com.queueless.businessservice.service.BusinessServiceManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
@Tag(name = "Businesses", description = "Business and service management")
public class BusinessController {

    private final BusinessService businessService;
    private final BusinessServiceManager serviceManager;

    @GetMapping
    @Operation(summary = "List businesses with optional filters")
    public ResponseEntity<List<BusinessResponse>> getAll(
            @RequestParam(required = false) BusinessCategory category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean open) {
        return ResponseEntity.ok(businessService.getAll(category, city, open));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(businessService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BusinessResponse> create(@Valid @RequestBody BusinessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(businessService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessResponse> update(@PathVariable UUID id, @Valid @RequestBody BusinessRequest request) {
        return ResponseEntity.ok(businessService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        businessService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/open")
    public ResponseEntity<BusinessResponse> openBusiness(@PathVariable UUID id) {
        return ResponseEntity.ok(businessService.setOpenStatus(id, true));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<BusinessResponse> closeBusiness(@PathVariable UUID id) {
        return ResponseEntity.ok(businessService.setOpenStatus(id, false));
    }

    // Services
    @GetMapping("/{id}/services")
    public ResponseEntity<List<ServiceResponse>> getServices(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceManager.getServicesForBusiness(id));
    }

    @PostMapping("/{id}/services")
    public ResponseEntity<ServiceResponse> addService(@PathVariable UUID id, @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceManager.addService(id, request));
    }

    @PutMapping("/services/{serviceId}")
    public ResponseEntity<ServiceResponse> updateService(@PathVariable UUID serviceId, @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(serviceManager.updateService(serviceId, request));
    }

    @DeleteMapping("/services/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID serviceId) {
        serviceManager.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }
}
