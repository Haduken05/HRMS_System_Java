package logic;

import dataObject.LeaveRequestEntity;
import dataObject.LeaveRequestRow;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LeaveRequestLogic {

    private static final DateTimeFormatter DISPLAY_FMT
            = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    public static LeaveRequestRow toRow(LeaveRequestEntity entity) {
        return new LeaveRequestRow(
                String.valueOf(entity.getRequestId()),
                String.valueOf(entity.getEmpId()),
                entity.getFullName(),
                entity.getLeaveType(),
                formatDate(entity.getStartDate()),
                formatDate(entity.getEndDate()),
                entity.getStatus()
        );
    }

    public static String formatDate(java.util.Date date) {
        if (date == null) {
            return "";
        }
        try {
            LocalDate ld = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            return ld.format(DISPLAY_FMT);
        } catch (Exception e) {
            return date.toString();
        }
    }
}
