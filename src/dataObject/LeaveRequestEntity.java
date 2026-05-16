package dataObject;

import java.sql.Date;

public class LeaveRequestEntity {

    private final int requestId;
    private final int empId;
    private final String fullName;
    private final String leaveType;
    private final Date startDate;
    private final Date endDate;
    private final String status;

    public LeaveRequestEntity(int requestId, int empId, String fullName,
            String leaveType, Date startDate, Date endDate,
            String status) {
        this.requestId = requestId;
        this.empId = empId;
        this.fullName = fullName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getEmpId() {
        return empId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }
}
