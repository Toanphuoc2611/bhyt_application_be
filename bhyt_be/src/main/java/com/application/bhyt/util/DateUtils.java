package com.application.bhyt.util;

import java.time.LocalDate;

public class DateUtils {

    /**
     * Phương thức dùng để chuyển đổi một chuỗi ngày tháng (dateString) thành đối tượng LocalDate.
     * Chuỗi ngày tháng phải có định dạng "dd/MM/yyyy".
     * @param dateString
     * @return
     */
    public static LocalDate toLocalDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        String[] parts = dateString.split("/");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid date format. Expected format: dd/MM/yyyy");
        }
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        return LocalDate.of(year, month, day);
    }

    /**
     * Phương thức dùng để chuyển đổi một đối tượng LocalDate thành chuỗi ngày tháng có định dạng "dd/MM/yyyy".
     * @param date
     * @return
     */
    public static String toString(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.getDayOfMonth() + "/" + date.getMonthValue() + "/" + date.getYear();
    }
}
