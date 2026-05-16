package dataObject;

public class Employee {

    public final int    empId;
    public final String fullName;
    public final String department;
    public final String position;
    public final String contactNo;
    public final String role;

    public Employee(int empId, String fullName, String department,
                    String position, String contactNo, String role) {
        this.empId      = empId;
        this.fullName   = fullName;
        this.department = department;
        this.position   = position;
        this.contactNo  = contactNo;
        this.role       = role;
    }

    // Convenience — for displaying in tables or dropdowns
    @Override
    public String toString() {
        return "[" + empId + "] " + fullName;
    }
}