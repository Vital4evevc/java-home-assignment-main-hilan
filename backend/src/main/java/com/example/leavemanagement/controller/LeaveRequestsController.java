package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.CreateLeaveRequestDto;
import com.example.leavemanagement.model.Employee;
import com.example.leavemanagement.model.LeaveRequest;
import com.example.leavemanagement.model.LeaveStatus;
import com.example.leavemanagement.model.LeaveType;
import com.example.leavemanagement.repository.EmployeeRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.List;

// NOTE: This controller was written quickly for a POC.
// It does data access, business logic and validation all in one place.
@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestsController {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public LeaveRequestsController(EmployeeRepository employeeRepository,
                                   LeaveRequestRepository leaveRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    // GET /api/leave-requests
    @GetMapping
    public ResponseEntity<List<LeaveRequest>> getAll() {
        List<LeaveRequest> all = leaveRequestRepository.findAll().stream()
                .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
                .toList();
        return ResponseEntity.ok(all);
    }

    // GET /api/leave-requests/search?name=Dana
    // Lets the UI quickly find requests by employee name.
    @GetMapping("/search")
    public ResponseEntity<List<LeaveRequest>> search(@RequestParam String name) {
        // Build a quick query to filter by the employee name.
        String sql = "SELECT * FROM leave_requests WHERE employee_id IN " +
                "(SELECT id FROM employees WHERE name LIKE '%" + name + "%')";

        @SuppressWarnings("unchecked")
        List<LeaveRequest> results = entityManager
                .createNativeQuery(sql, LeaveRequest.class)
                .getResultList();

        return ResponseEntity.ok(results);
    }

    // POST /api/leave-requests
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateLeaveRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId()).orElse(null);
        if (employee == null) {
            return ResponseEntity.status(404).body("Employee not found");
        }

        int requestedDays = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;

        // How many vacation days has the employee already used this year?
        int usedDays = leaveRequestRepository
                .findByEmployeeIdAndTypeAndStatus(dto.getEmployeeId(), LeaveType.VACATION, LeaveStatus.APPROVED)
                .stream()
                .mapToInt(LeaveRequest::getDays)
                .sum();

        // Make sure the request does not exceed the quota.
        int sumUsedDays = requestedDays + usedDays;
        if (dto.getType() == LeaveType.VACATION && sumUsedDays > employee.getAnnualQuota()) {
            return ResponseEntity.badRequest().body("Not enough vacation balance");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(dto.getEmployeeId());
        leaveRequest.setType(dto.getType());
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setDays(requestedDays);
        leaveRequest.setStatus(LeaveStatus.PENDING);

        leaveRequestRepository.save(leaveRequest);

        return ResponseEntity.ok(leaveRequest);
    }
}
