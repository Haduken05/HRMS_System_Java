package logic;

public class SessionLogic {

    public static final String ROLE_EMPLOYEE = "Employee";

    public static boolean isEmployee(String role) {
        return role.equalsIgnoreCase(ROLE_EMPLOYEE);
    }

    public static int getStartingTabIndex(String role) {
        return isEmployee(role) ? 0 : 3;
    }

    public static boolean isEmpIdEditable(String role) {
        return !isEmployee(role);
    }
}