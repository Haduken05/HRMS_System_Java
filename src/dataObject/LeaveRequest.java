package dataObject;

import java.sql.Date;

public class LeaveRequest {

    public final int    requestId;
    public final int    empId;
    public final String fullName;     // joined from employees table
    public final String leaveType;
    public final Date   startDate;
    public final Date   endDate;
    public final String status;

    public LeaveRequest(int requestId, int empId, String fullName,
                        String leaveType, Date startDate, Date endDate, String status) {
        this.requestId = requestId;
        this.empId     = empId;
        this.fullName  = fullName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate   = endDate;
        this.status    = status;
    }
}