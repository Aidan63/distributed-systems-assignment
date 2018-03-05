package uk.aidanlee.dsp_assignment.utils;

import java.util.Date;

public class TimeTools {
    public static String padDigits(float _time) {
        String trimmedTime = String.valueOf(_time).substring(0, 2);
        return lpad(trimmedTime, "0", 2);
    }

    public static String formattedTime(float _time) {
        Date date = new Date((long) _time);
        return date.toString();
    }

    private static String lpad(String _s, String _c, int _l) {
        if (_c.length() <= 0) {
            return _s;
        }

        while (_s.length() < _l) {
            _s = _c + _s;
        }

        return _s;
    }
}
