package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.Employee;
import com.example.leavemanagement.service.IEmployeeService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Read-only lookup so the UI can offer an employee picker.
@RestController
@RequestMapping("/api/employees")
public class EmployeesController {

    private final IEmployeeService employeeService;

    public EmployeesController(IEmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // GET /api/employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAll() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }
}
