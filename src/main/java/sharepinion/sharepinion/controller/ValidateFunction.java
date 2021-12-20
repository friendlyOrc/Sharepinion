package sharepinion.sharepinion.controller;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class ValidateFunction {

    ValidateFunction() {

    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);
    public static final Pattern VALID_INPUT = Pattern.compile("[-=!#$%^&*(),?:{}|<>]");

    public boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public boolean isAllNumber(String s) {
        for (int i = 0; i < s.length(); i++) {
            try {
                Double.parseDouble(String.valueOf(s.charAt(i)));
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public boolean verifyId(String s) {
        if (isAllNumber(s) && s.length() == 10) {
            return true;
        }
        return false;
    }

    public boolean specialKey(String s) {

        Matcher matcher = VALID_INPUT.matcher(s);
        return (matcher.find() == false);
    }

    public boolean validDOB(java.sql.Date date) {
        java.util.Date date2 = new java.util.Date(date.getTime());
        java.util.Date date1 = new java.util.Date();
        return getDiffYears(date2, date1) >= 18;
    }

    public static int getDiffYears(java.util.Date first, java.util.Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH)
                || (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(java.util.Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public boolean isContainsNumber(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= '0' && s.charAt(i) <= '9')
                return true;
        }
        return false;
    }

}
