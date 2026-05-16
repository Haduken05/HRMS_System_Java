package logic;

public class SessionLogic {

    public static final String ROLE_EMPLOYEE = "Employee";

    // Returns true if this role should have restricted access
    public static boolean isEmployee(String role) {
        return role.equalsIgnoreCase(ROLE_EMPLOYEE);
    }

    // Returns the correct starting tab index for the role
    // Managers start on the Management tab (index 3 before removal,
    // index 0 for employees since other tabs were removed)
    public static int getStartingTabIndex(String role) {
        return isEmployee(role) ? 0 : 3;
    }

    // Returns whether the emp ID field should be editable for this role
    public static boolean isEmpIdEditable(String role) {
        return !isEmployee(role);
    }
}