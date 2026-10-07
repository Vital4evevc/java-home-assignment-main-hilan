package com.example.leavemanagement.service;

import java.util.List;

import com.example.leavemanagement.model.Employee;

public interface IEmployeeService {
    List<Employee> getAllEmployees();

    Employee getEmployeeById(Long employeeId);
}
