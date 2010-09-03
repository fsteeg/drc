/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Util class for converting page numbers.
 * @author Mihail Atanassov <saeko.bjagai@googlemail.com>
 */
public class PageConverter {

  private final static PageConverter INSTANCE = new PageConverter();
  private Map<String, Map<String, String>> outer;

  private PageConverter() {
    initialize();
  }

  /**
   * @param pageId The page id
   * @return The converted page number
   */
  public static String convert(final String pageId) {
    return INSTANCE.extractVolumeAndPage(pageId);
  }

  private String extractVolumeAndPage(final String pageId) {
    String volume = pageId.substring(13, 17);
    String pageNum = pageId.substring(18, 22);
    String[] strings = { volume, pageNum };
    for (int i = 0; i < strings.length; i++) {
      int count = 0;
      char[] str = strings[i].toCharArray();
      for (int j = 0; j < str.length; j++) {
        if (str[j] == '0') {
          count++;
        } else {
          break;
        }
      }
      strings[i] = strings[i].substring(count);
    }
    volume = strings[0];
    pageNum = strings[1];
    Map<String, String> inner = outer.get(volume);
    String toReturn = inner.get(pageNum);
    return toReturn;
  }

  private void initialize() {
    try {
      outer = new HashMap<String, Map<String, String>>();
      Properties props = new Properties();
      props.load(PageConverter.class.getResourceAsStream("page.properties"));
      Set<String> keys = props.stringPropertyNames();
      String[] numerals = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI",
          "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX", "XXI", "XXII", "XXIII",
          "XXIV", "XXV", "XXVI", "XXVII", "XXVIII", "XXIX", "XXX" };
      List<String> romanNumerals = Arrays.asList(numerals);
      for (String key : keys) {
        String value = props.getProperty(key);
        String[] values = value.split(" ");

        int beginRF = Integer.valueOf(values[0]);
        int endRF = Integer.valueOf(values[1]);

        int beginOC = Integer.valueOf(values[2]);
        int endOC = Integer.valueOf(values[3]);

        int beginEp = Integer.valueOf(values[4]);
        int endEp = Integer.valueOf(values[5]);

        String beginRom = values[6];
        String endRom = values[7];

        Map<String, String> inner = putInnerMap(beginRF, endRF, beginOC, endOC, beginEp, endEp,
            beginRom, endRom, romanNumerals);
        outer.put(key, inner);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Map<String, String> putInnerMap(int beginRF, int endRF, int beginOC, int endOC,
      int beginEp, int endEp, String beginRom, String endRom, List<String> romanNumerals) {

    Map<String, String> inner = new HashMap<String, String>();

    for (; beginRF <= endRF; beginRF++, beginOC++) {
      if (beginOC > endOC) {
        inner.put(beginRF + "", "unbekannt");
      } else {
        inner.put(beginRF + "", beginOC + "");
      }
    }
    if (beginRom != null) {
      int index = romanNumerals.indexOf(beginRom);
      for (; beginEp <= endEp; beginEp++, index++) {
        inner.put(beginEp + "", romanNumerals.get(index));
        if (romanNumerals.get(index).equals(endRom)) {
          break;
        }
      }
    }
    return inner;
  }

}
