package com.example.leavemanagement.dto;

import com.example.leavemanagement.model.LeaveType;
import java.time.LocalDate;

// Incoming payload for creating a leave request.
public class CreateLeaveRequestDto {

    private Long employeeId;
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;

    // tests fail without empty constructor, because we create empty dto, than fill it.
    public CreateLeaveRequestDto() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LeaveType getType() { return type; }
    public void setType(LeaveType type) { this.type = type; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
