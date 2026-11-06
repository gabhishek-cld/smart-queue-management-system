package com.qms.config;

import com.qms.model.entity.AppUser;
import com.qms.model.entity.Counter;
import com.qms.model.entity.ServiceType;
import com.qms.model.enums.CounterStatus;
import com.qms.model.enums.UserRole;
import com.qms.repository.AppUserRepository;
import com.qms.repository.CounterRepository;
import com.qms.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final CounterRepository counterRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedServiceTypes();
        seedCounters();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setDisplayName("Admin");

        AppUser staff1 = new AppUser();
        staff1.setUsername("staff1");
        staff1.setPassword(passwordEncoder.encode("staff123"));
        staff1.setRole(UserRole.STAFF);
        staff1.setDisplayName("Priya Sharma");

        AppUser staff2 = new AppUser();
        staff2.setUsername("staff2");
        staff2.setPassword(passwordEncoder.encode("staff123"));
        staff2.setRole(UserRole.STAFF);
        staff2.setDisplayName("Rahul Verma");

        userRepository.saveAll(List.of(admin, staff1, staff2));
    }

    private void seedServiceTypes() {
        if (serviceTypeRepository.count() > 0) return;

        serviceTypeRepository.saveAll(List.of(
                makeService("Account Opening",   "A", 2, 8.0),
                makeService("Cash Deposit",      "C", 2, 5.0),
                makeService("Cash Withdrawal",   "W", 2, 4.0),
                makeService("Technical Support", "T", 3, 12.0),
                makeService("VIP / Priority",    "V", 10, 6.0)  // highest priority weight
        ));
    }

    private void seedCounters() {
        if (counterRepository.count() > 0) return;

        counterRepository.saveAll(List.of(
                makeCounter("Counter 1", "Priya Sharma"),
                makeCounter("Counter 2", "Rahul Verma"),
                makeCounter("Counter 3", null)
        ));
    }

    private ServiceType makeService(String name, String prefix, int weight, double avgMinutes) {
        ServiceType s = new ServiceType();
        s.setName(name);
        s.setPrefix(prefix);
        s.setPriorityWeight(weight);
        s.setAvgServiceMinutes(avgMinutes);
        return s;
    }

    private Counter makeCounter(String name, String staffName) {
        Counter c = new Counter();
        c.setName(name);
        c.setStaffName(staffName);
        c.setStatus(CounterStatus.ACTIVE);
        return c;
    }
}
