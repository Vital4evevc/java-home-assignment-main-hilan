package com.example.leavemanagement;

import com.example.leavemanagement.controller.LeaveRequestsController;
import com.example.leavemanagement.dto.CreateLeaveRequestDto;
import com.example.leavemanagement.model.*;
import com.example.leavemanagement.repository.EmployeeRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// Runs against a real, throwaway PostgreSQL started by Testcontainers.
// (Docker must be available on the machine running the tests.)
@SpringBootTest
@Testcontainers
class LeaveRequestsTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private LeaveRequestsController controller;

    @Autowired
    private EmployeeRepository employees;

    @Autowired
    private LeaveRequestRepository leaveRequests;

    @Test
    void create_WithinQuota_Succeeds() {
        // Arrange
        Employee emp = new Employee();
        emp.setName("Test Emp");
        emp.setAnnualQuota(20);
        employees.save(emp);

        long before = leaveRequests.count();

        CreateLeaveRequestDto dto = new CreateLeaveRequestDto();
        dto.setEmployeeId(emp.getId());
        dto.setType(LeaveType.VACATION);
        dto.setStartDate(LocalDate.of(2026, 3, 1));
        dto.setEndDate(LocalDate.of(2026, 3, 3)); // 3 days, well within the quota

        // Act
        ResponseEntity<?> result = controller.create(dto);

        // Assert
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertEquals(before + 1, leaveRequests.count());
    }

    @Test
    void create_ExceedsQuota_Fails() {
        // Arrange
        Employee emp = new Employee();
        emp.setName("Test Emp");
        emp.setAnnualQuota(20);
        employees.save(emp);
        
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(emp.getId());
        leaveRequest.setType(LeaveType.VACATION);
        leaveRequest.setStartDate(LocalDate.of(2026, 1, 1));
        leaveRequest.setEndDate(LocalDate.of(2026, 1, 10));
        leaveRequest.setDays(10);
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequests.save(leaveRequest);

        CreateLeaveRequestDto dto = new CreateLeaveRequestDto();
        dto.setEmployeeId(emp.getId());
        dto.setType(LeaveType.VACATION);
        dto.setStartDate(LocalDate.of(2026, 3, 1));
        dto.setEndDate(LocalDate.of(2026, 3, 13));

        // Act
        ResponseEntity<?> result = controller.create(dto); // 13 days, would exceed the quota of 20

        // Assert
        assertTrue(result.getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    // Test to simulate two concurrent approval requests for the same leave request.
    @Test
    void approve_SimultaneousRequests_OnlyOneSucceeds() throws Exception {
        Employee emp = new Employee();
        emp.setName("Test Emp");
        emp.setAnnualQuota(20);
        employees.save(emp);

        CreateLeaveRequestDto dto = new CreateLeaveRequestDto();
        dto.setEmployeeId(emp.getId());
        dto.setType(LeaveType.VACATION);
        dto.setStartDate(LocalDate.of(2026, 4, 1));
        dto.setEndDate(LocalDate.of(2026, 4, 3));

        ResponseEntity<?> createResult = controller.create(dto);
        assertTrue(createResult.getStatusCode().is2xxSuccessful());
        LeaveRequest createdRequest = (LeaveRequest) createResult.getBody();
        assertNotNull(createdRequest);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<ResponseEntity<?>> approveConcurrently = () -> {
            ready.countDown();
            start.await();
            return controller.approve(createdRequest.getId());
        };

        try {
            Future<ResponseEntity<?>> firstApproval = executor.submit(approveConcurrently);
            Future<ResponseEntity<?>> secondApproval = executor.submit(approveConcurrently);

            assertTrue(ready.await(5, TimeUnit.SECONDS), "Both approval calls should be ready");
            start.countDown();

            ResponseEntity<?> firstResult = firstApproval.get(10, TimeUnit.SECONDS);
            ResponseEntity<?> secondResult = secondApproval.get(10, TimeUnit.SECONDS);

            assertEquals(1, (int) java.util.stream.Stream.of(firstResult, secondResult)
                    .filter(result -> result.getStatusCode() == HttpStatus.OK)
                    .count());
            assertEquals(1, (int) java.util.stream.Stream.of(firstResult, secondResult)
                    .filter(result -> result.getStatusCode() == HttpStatus.BAD_REQUEST)
                    .count());
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
