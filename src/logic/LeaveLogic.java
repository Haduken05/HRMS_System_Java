package logic;

import dbquery.LeaveQuery;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LeaveLogic {

    public static String validate(int empId, String leaveType,
            Date startDate, Date endDate, String selectedPath) {
        if (startDate == null || endDate == null) {
            return "Please select both From and To dates.";
        }
        if (leaveType == null || leaveType.isBlank() || leaveType.equals("Select Leave Type")) {
            return "Please select a leave type.";
        }

        Date start = stripTime(startDate);
        Date end = stripTime(endDate);
        Date today = stripTime(new Date());

        if (start.before(today)) {
            return "Start date cannot be in the past!";
        }
        if (end.before(start)) {
            return "End date cannot be before Start date!";
        }

        long daysOfLeave = daysBetween(start, end) + 1;
        long daysNotice = daysBetween(today, start);

        if (LeaveQuery.hasOverlap(empId, start, end)) {
            return "<html>You already have a <b>Pending or Approved</b> leave request<br>"
                    + "that overlaps with the selected dates.<br><br>"
                    + "Please wait for the admin to process your existing request,<br>"
                    + "or choose different dates.</html>";
        }

        switch (leaveType) {
            case "VL":
                if (daysNotice < 2) {
                    return "Vacation Leave must be filed at least 2 days in advance!";
                }
                int vlCredits = LeaveQuery.getVLCredits(empId);
                if (vlCredits < 0) {
                    return "Could not retrieve VL credits. Please try again.";
                }
                if (daysOfLeave > vlCredits) {
                    return "Insufficient Vacation Leave credits! You have " + vlCredits
                            + " VL day(s) remaining, but are requesting " + daysOfLeave + ".";
                }
                break;

            case "SL":
                
                if (daysOfLeave >= 2
                        && (selectedPath == null || selectedPath.isBlank()
                        || selectedPath.equals("None"))) {
                    return """
                           Sick Leave for 2 or more days requires a Medical Certificate!
                           Please upload a supporting document before submitting.""";
                }
                int slCredits = LeaveQuery.getSLCredits(empId);
                if (slCredits < 0) {
                    return "Could not retrieve SL credits. Please try again.";
                }
                if (daysOfLeave > slCredits) {
                    return "Insufficient Sick Leave credits! You have " + slCredits
                            + " SL day(s) remaining, but are requesting " + daysOfLeave + ".";
                }
                break;

            default:
                return "Unknown leave type: " + leaveType;
        }
        return null;
    }

    public static long daysBetween(Date a, Date b) {
        return TimeUnit.MILLISECONDS.toDays(b.getTime() - a.getTime());
    }

    public static Date stripTime(Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
