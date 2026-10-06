package com.aayusheklavya.onboarding.api;

import com.aayusheklavya.onboarding.api.Dtos.*;
import com.aayusheklavya.onboarding.domain.ApplicationStatus;
import com.aayusheklavya.onboarding.domain.BusinessApplication;
import com.aayusheklavya.onboarding.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final OnboardingService service;

    public ApplicationController(OnboardingService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApplicationSummary> list(@RequestParam(required = false) ApplicationStatus status) {
        return service.list(status).stream().map(ApplicationSummary::from).toList();
    }

    @GetMapping("/summary")
    public Map<ApplicationStatus, Long> summary() {
        return service.summary();
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable UUID id) {
        return ApplicationResponse.from(service.get(id));
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequest req) {
        BusinessApplication app = service.create(req.businessName(), req.legalStructure(), req.naicsCode(), req.annualRevenue());
        return ResponseEntity.created(URI.create("/api/applications/" + app.getId())).body(ApplicationResponse.from(app));
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable UUID id, @Valid @RequestBody ApplicationRequest req) {
        return ApplicationResponse.from(service.updateDetails(id, req.businessName(), req.legalStructure(), req.naicsCode(), req.annualRevenue()));
    }

    @PostMapping("/{id}/representatives")
    @ResponseStatus(HttpStatus.CREATED)
    public RepresentativeResponse addRepresentative(@PathVariable UUID id, @Valid @RequestBody RepresentativeRequest req) {
        return RepresentativeResponse.from(service.addRepresentative(id, req.fullName(), req.email(), req.role(),
                req.ownershipPercent(), req.authorisedSigner()));
    }

    @DeleteMapping("/{id}/representatives/{repId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRepresentative(@PathVariable UUID id, @PathVariable UUID repId) {
        service.removeRepresentative(id, repId);
    }

    @PostMapping("/{id}/submit")
    public ApplicationResponse submit(@PathVariable UUID id) {
        return ApplicationResponse.from(service.submit(id));
    }

    @PostMapping("/{id}/review")
    public ApplicationResponse review(@PathVariable UUID id) {
        return ApplicationResponse.from(service.startReview(id));
    }

    @PostMapping("/{id}/approve")
    public ApplicationResponse approve(@PathVariable UUID id, @RequestBody(required = false) DecisionRequest req) {
        return ApplicationResponse.from(service.approve(id, req == null ? null : req.note()));
    }

    @PostMapping("/{id}/reject")
    public ApplicationResponse reject(@PathVariable UUID id, @Valid @RequestBody DecisionRequest req) {
        return ApplicationResponse.from(service.reject(id, req.note()));
    }
}
