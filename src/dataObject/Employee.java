package dataObject;

import java.sql.Date;

public class Employee {

    public final int empId;
    public final String fullName;
    public final String department;
    public final String position;
    public final String contactNo;
    public final String role;
    public final Date hireDate;
    public final int vlCredits; 
    public final int slCredits; 

    public Employee(int empId, String fullName, String department,
            String position, String contactNo, String role,
            Date hireDate, int vlCredits, int slCredits) {
        this.empId = empId;
        this.fullName = fullName;
        this.department = department;
        this.position = position;
        this.contactNo = contactNo;
        this.role = role;
        this.hireDate = hireDate;
        this.vlCredits = vlCredits;
        this.slCredits = slCredits;
    }

    @Override
    public String toString() {
        return "[" + empId + "] " + fullName;
    }
}
