package dataObject;

public class LeaveRequestRow {

    private final String requestId;
    private final String empId;
    private final String fullName;
    private final String leaveType;
    private final String startDate;
    private final String endDate;
    private final String status;

    public LeaveRequestRow(String requestId, String empId, String fullName,
            String leaveType, String startDate, String endDate,
            String status) {
        this.requestId = requestId;
        this.empId = empId;
        this.fullName = fullName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getEmpId() {
        return empId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    public LeaveRequestRow withStatus(String newStatus) {
        return new LeaveRequestRow(
                requestId, empId, fullName, leaveType, startDate, endDate, newStatus);
    }

    public Object[] toTableRow() {
        return new Object[]{requestId, empId, fullName, leaveType, startDate, endDate, status};
    }
}
