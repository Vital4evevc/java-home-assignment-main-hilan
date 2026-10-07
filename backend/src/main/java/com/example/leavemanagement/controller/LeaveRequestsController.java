package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.CreateLeaveRequestDto;
import com.example.leavemanagement.exception.CustomException;
import com.example.leavemanagement.model.Employee;
import com.example.leavemanagement.model.LeaveRequest;
import com.example.leavemanagement.model.LeaveStatus;
import com.example.leavemanagement.service.IEmployeeService;
import com.example.leavemanagement.service.ILeaveRequestService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// NOTE: This controller was written quickly for a POC.
// It does data access, business logic and validation all in one place.
@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestsController {

    private final IEmployeeService employeeService;
    private final ILeaveRequestService leaveRequestService;

    @PersistenceContext
    private EntityManager entityManager;

    public LeaveRequestsController(IEmployeeService employeeService, ILeaveRequestService leaveRequestService) {
        this.employeeService = employeeService;
        this.leaveRequestService = leaveRequestService;
    }

    // GET /api/leave-requests
    @GetMapping
    public ResponseEntity<List<LeaveRequest>> getAll() {
        List<LeaveRequest> all = leaveRequestService.getAllLeaveRequests().stream()
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
        Employee employee = employeeService.getEmployeeById(dto.getEmployeeId());
        if (employee == null) {
            return ResponseEntity.status(404).body("Employee not found");
        }

        LeaveRequest leaveRequest = null;
        try {
            leaveRequest = leaveRequestService.createLeaveRequest(dto, employee);
        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }

        return ResponseEntity.ok(leaveRequest);
    }

    // POST /api/leave-requests/{id}/approve
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        LeaveRequest leaveRequest = null;
        try {
            leaveRequest = leaveRequestService.processLeaveRequest(id, LeaveStatus.APPROVED);
        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }

        return ResponseEntity.ok(leaveRequest);
    }

    // POST /api/leave-requests/{id}/reject
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> decline(@PathVariable Long id) {
        LeaveRequest leaveRequest = null;
        try {
            leaveRequest = leaveRequestService.processLeaveRequest(id, LeaveStatus.REJECTED);
        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }

        return ResponseEntity.ok(leaveRequest);
    }
}
