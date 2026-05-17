package dataObject;

import java.util.Date;

public class Employee {

    public final int empId;
    public final String fullName;
    public final String username;
    public final String department;
    public final String position;
    public final String contactNo;
    public final String profilePic;  
    public final String role;
    public final String password;
    public final Date hireDate;
    public final int vlCredits;
    public final int slCredits;

    public Employee(int empId, String fullName, String username,
            String department, String position, String contactNo,
            String profilePic, String role, String password,
            Date hireDate, int vlCredits, int slCredits) {
        this.empId = empId;
        this.fullName = fullName;
        this.username = username;
        this.department = department;
        this.position = position;
        this.contactNo = contactNo;
        this.profilePic = profilePic;
        this.role = role;
        this.password = password;
        this.hireDate = hireDate;
        this.vlCredits = vlCredits;
        this.slCredits = slCredits;
    }
}
