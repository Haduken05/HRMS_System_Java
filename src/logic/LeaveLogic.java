package logic;

import java.util.Date;

public class LeaveLogic {

    public static String validate(String empIdStr, String leaveType,
                                   Date startDate, Date endDate, String selectedPath) {
        if (empIdStr.isEmpty() || startDate == null || endDate == null)
            return "All fields are required!";

        Date today = new Date();
        if (startDate.before(today))
            return "Start date cannot be in the past!";
        if (endDate.before(startDate))
            return "End date cannot be before Start date!";

        long daysNotice = (startDate.getTime() - today.getTime()) / (24 * 60 * 60 * 1000);
        if (leaveType.equals("VL") && daysNotice < 1)
            return "Vacation Leave must be filed at least 2 days in advance!";

        long daysOfLeave = (endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000);
        if (leaveType.equals("SL") && daysOfLeave >= 1 && selectedPath.equals("None"))
            return "Sick Leave for 2+ days requires a Medical Certificate!";

        return null; // null = valid
    }
}