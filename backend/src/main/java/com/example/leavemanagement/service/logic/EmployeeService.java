package com.example.leavemanagement.service.logic;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.leavemanagement.model.Employee;
import com.example.leavemanagement.repository.EmployeeRepository;
import com.example.leavemanagement.service.IEmployeeService;

@Service
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    @Override
    public Employee getEmployeeById(Long employeeId) {
        return repository.findById(employeeId).orElse(null);
    }
}
