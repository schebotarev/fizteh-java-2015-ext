package ru.mipt.diht.samples.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author s.chebotarev
 * @since 05.11.2015
 */
public class Utils {
    /*
    * Возвращает объект Date с указанной в формате "yyyy.MM.dd" датой.
    **/
    public static Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("yyyy.MM.dd").parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("illegal date: " + dateStr, e);
        }
    }
}
