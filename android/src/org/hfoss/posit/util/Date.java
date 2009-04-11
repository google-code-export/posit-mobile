package org.hfoss.posit.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
/**
 * All the date functions go here.
 * @author pgautam
 *
 */
public class Date {
  public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
  /**
   * The current date and time.
   * @return
   */
  public static String now() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    return sdf.format(cal.getTime());

  }

  
}
