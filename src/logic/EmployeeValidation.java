package logic;


public class EmployeeValidation {

    // Add Employee
    public static String validateAdd(String fullName, String contactNo,
                                     String position, String department,
                                     String role) {
        if (fullName == null || fullName.isBlank())
            return "Full Name is required.";

        if (fullName.trim().length() < 2)
            return "Full Name must be at least 2 characters.";

        if (fullName.trim().length() > 200)
            return "Full Name must not exceed 200 characters.";

        if (contactNo == null || contactNo.isBlank())
            return "Contact Number is required.";

        if (!contactNo.trim().matches("^[0-9+\\-\\s]{7,20}$"))
            return "Contact Number must be 7–20 digits and may include +, -, or spaces.";

        if (position == null || position.isBlank())
            return "Position is required.";

        if (position.trim().length() > 50)
            return "Position must not exceed 50 characters.";

        if (department == null || department.isBlank())
            return "Department is required.";

        if (role == null || role.isBlank())
            return "Role is required.";

        return null; // all good
    }

    //  Offboard Employee
    public static String validateOffboard(String empIdText, String reason) {
        if (empIdText == null || empIdText.isBlank())
            return "Employee ID is required.";

        try {
            int id = Integer.parseInt(empIdText.trim());
            if (id <= 0)
                return "Employee ID must be a positive number.";
        } catch (NumberFormatException e) {
            return "Employee ID must be a valid number.";
        }

        if (reason == null || reason.isBlank())
            return "A reason for offboarding is required.";

        if (reason.trim().length() < 10)
            return "Please provide a more detailed reason (at least 10 characters).";

        return null; // all good
    }

    public static int parseId(String empIdText) {
        return Integer.parseInt(empIdText.trim());
    }
}