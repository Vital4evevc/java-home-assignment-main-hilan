package com.example.leavemanagement.service;

import java.util.Collection;

import com.example.leavemanagement.dto.CreateLeaveRequestDto;
import com.example.leavemanagement.exception.CustomException;
import com.example.leavemanagement.model.Employee;
import com.example.leavemanagement.model.LeaveRequest;
import com.example.leavemanagement.model.LeaveStatus;
import com.example.leavemanagement.model.LeaveType;

public interface ILeaveRequestService {

    Collection<LeaveRequest> getAllLeaveRequests();

    Collection<LeaveRequest> findByEmployeeIdAndTypeAndStatus(Long employeeId, LeaveType vacation,
            LeaveStatus approved);

    LeaveRequest findById(Long id);

    LeaveRequest createLeaveRequest(CreateLeaveRequestDto dto, Employee employee) throws CustomException;

    LeaveRequest processLeaveRequest(Long id, LeaveStatus status) throws CustomException;

    LeaveRequest createLeaveRquest(CreateLeaveRequestDto dto, int requestedDays);

}
