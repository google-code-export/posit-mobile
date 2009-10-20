package org.hfoss.posit.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Date {
  public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

  public static String now() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    return sdf.format(cal.getTime());

  }

  
}
