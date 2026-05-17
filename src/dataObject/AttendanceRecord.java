package dataObject;

import java.sql.Timestamp;

public class AttendanceRecord {

    public final int attendanceId;
    public final int empId;
    public final String fullName;
    public final String department;
    public final Timestamp timeIn;
    public final Timestamp timeOut;   
    public final String dateLogged;
    public final String status;
    public final String remarks;   
    public final int otHours;

    public AttendanceRecord(int attendanceId, int empId, String fullName,
            String department, Timestamp timeIn, Timestamp timeOut,
            String dateLogged, String status, String remarks,
            int otHours) {
        this.attendanceId = attendanceId;
        this.empId = empId;
        this.fullName = fullName;
        this.department = department;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.dateLogged = dateLogged;
        this.status = status;
        this.remarks = remarks;
        this.otHours = otHours;
    }
}
