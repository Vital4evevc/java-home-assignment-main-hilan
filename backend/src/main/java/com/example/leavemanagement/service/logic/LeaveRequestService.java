package com.example.leavemanagement.service.logic;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.leavemanagement.dto.CreateLeaveRequestDto;
import com.example.leavemanagement.exception.CustomException;
import com.example.leavemanagement.exception.ExceedsException;
import com.example.leavemanagement.exception.InProcessException;
import com.example.leavemanagement.exception.NotFoundException;
import com.example.leavemanagement.exception.NotPendingException;
import com.example.leavemanagement.model.Employee;
import com.example.leavemanagement.model.LeaveRequest;
import com.example.leavemanagement.model.LeaveStatus;
import com.example.leavemanagement.model.LeaveType;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import com.example.leavemanagement.service.ILeaveRequestService;

@Service
public class LeaveRequestService implements ILeaveRequestService {

    private final LeaveRequestRepository repository;

    private final Set<LeaveRequest> leaveRequestsInProcess = new HashSet<>();

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository) {
        this.repository = leaveRequestRepository;
    }

    @Override
    public Collection<LeaveRequest> getAllLeaveRequests() {
        return repository.findAll();
    }

    @Override
    public LeaveRequest findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Collection<LeaveRequest> findByEmployeeIdAndTypeAndStatus(Long employeeId, LeaveType vacationType,
            LeaveStatus statusType) {
        return repository.findByEmployeeIdAndTypeAndStatus(employeeId, vacationType, statusType);
    }

    @Override
    public LeaveRequest createLeaveRequest(CreateLeaveRequestDto dto, Employee employee) throws CustomException {
        int requestedDays = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;

        // How many vacation days has the employee already used this year?
        int usedDays = findByEmployeeIdAndTypeAndStatus(dto.getEmployeeId(), LeaveType.VACATION, LeaveStatus.APPROVED)
                .stream()
                .mapToInt(LeaveRequest::getDays)
                .sum();

        // Make sure the request does not exceed the quota.
        int sumUsedDays = requestedDays + usedDays;
        if (dto.getType() == LeaveType.VACATION && sumUsedDays > employee.getAnnualQuota()) {
            throw new ExceedsException();
        }

        return createLeaveRquest(dto, requestedDays);
    }

    @Override
    public LeaveRequest processLeaveRequest(Long id, LeaveStatus status) throws CustomException {

        LeaveRequest leaveRequest = findById(id);
        if (leaveRequest == null) {
            throw new NotFoundException();
        }
        synchronized (leaveRequest) {
            if (leaveRequestsInProcess.contains(leaveRequest)) {
                throw new InProcessException();
            } else if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
                throw new NotPendingException();
            } else {
                leaveRequestsInProcess.add(leaveRequest);
            }
            leaveRequest.setStatus(status);
            repository.save(leaveRequest);
            leaveRequestsInProcess.remove(leaveRequest);
        }
        return leaveRequest;
    }

    public LeaveRequest createLeaveRquest(CreateLeaveRequestDto dto, int requestedDays) {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(dto.getEmployeeId());
        leaveRequest.setType(dto.getType());
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setDays(requestedDays);
        leaveRequest.setStatus(LeaveStatus.PENDING);

        repository.save(leaveRequest);
        return leaveRequest;
    }

}
