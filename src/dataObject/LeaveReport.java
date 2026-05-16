package dataObject;

import java.sql.Date;

public class LeaveReport {

    public final String fullName;
    public final String leaveType;
    public final Date   startDate;
    public final Date   endDate;
    public final String status;

    public LeaveReport(String fullName, String leaveType,
                       Date startDate, Date endDate, String status) {
        this.fullName  = fullName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate   = endDate;
        this.status    = status;
    }
}