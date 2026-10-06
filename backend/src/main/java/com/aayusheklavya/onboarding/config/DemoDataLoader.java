package com.aayusheklavya.onboarding.config;

import com.aayusheklavya.onboarding.domain.BusinessApplicationRepository;
import com.aayusheklavya.onboarding.domain.RepresentativeRole;
import com.aayusheklavya.onboarding.service.OnboardingService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.UUID;

/** Seeds a few applications in different states so the dashboard has something to show on first run. */
@Configuration
@Profile("demo")
public class DemoDataLoader {

    @Bean
    ApplicationRunner seed(OnboardingService service, BusinessApplicationRepository repo) {
        return args -> {
            if (repo.count() > 0) return;

            UUID a = service.create("Blue Lotus Bakery LLC", "LLC", "311811", new BigDecimal("420000")).getId();
            service.addRepresentative(a, "Meera Nair", "meera@bluelotus.example", RepresentativeRole.OWNER, new BigDecimal("60"), true);
            service.addRepresentative(a, "Rohan Nair", "rohan@bluelotus.example", RepresentativeRole.PARTNER, new BigDecimal("40"), false);
            service.submit(a);
            service.startReview(a);

            UUID b = service.create("Summit Freight Partners", "PARTNERSHIP", "484121", new BigDecimal("2750000")).getId();
            service.addRepresentative(b, "Daniel Okafor", "daniel@summitfreight.example", RepresentativeRole.PARTNER, new BigDecimal("50"), true);
            service.addRepresentative(b, "Priya Shah", "priya@summitfreight.example", RepresentativeRole.PARTNER, new BigDecimal("50"), true);
            service.submit(b);
            service.startReview(b);
            service.approve(b, "KYC complete, documents verified");

            UUID c = service.create("Northwind Dental Group", "CORPORATION", "621210", new BigDecimal("1300000")).getId();
            service.addRepresentative(c, "Dr. Alice Chen", "alice@northwind.example", RepresentativeRole.DIRECTOR, new BigDecimal("100"), true);
            service.submit(c);

            UUID d = service.create("Pixel Forge Studios", "SOLE_PROPRIETOR", "541511", new BigDecimal("95000")).getId();
            service.addRepresentative(d, "Tom Reyes", "tom@pixelforge.example", RepresentativeRole.OWNER, new BigDecimal("100"), false);

            UUID e = service.create("Harbor Lights Hotel", "LLC", "721110", new BigDecimal("5100000")).getId();
            service.addRepresentative(e, "Sofia Marin", "sofia@harborlights.example", RepresentativeRole.OFFICER, new BigDecimal("35"), true);
            service.submit(e);
            service.reject(e, "Ownership breakdown incomplete: 65% unaccounted for");
        };
    }
}
