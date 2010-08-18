/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class for converting page numbers. 
 * @author Mihail Atanassov <saeko.bjagai@googlemail.com>
 */
public class PageConverter {

  private final static PageConverter INSTANCE = new PageConverter();
  private final static String[] NUMERALS = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII",
      "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX", "XXI", "XXII", "XXIII", "XXIV",
      "XXV", "XXVI", "XXVII", "XXVIII", "XXIX", "XXX" };
  private Map<String, Map<String, String>> outer;

  private PageConverter() {

    outer = new HashMap<String, Map<String, String>>();
    List<String> romanNumerals = Arrays.asList(NUMERALS);

    // Volume 4, pages 1 till 208 (RF)
    // Volume 4, pages 209 till 218 (RF)
    outer.put("4", init(1, 208, 1, 209, 218, "III", romanNumerals));

  }

  /**
   * @param pageId The page id
   * @return The corresponding page number to be converted
   */
  public static String convert(final String pageId) {
    return INSTANCE.extractVolumeAndPage(pageId);
  }

  private Map<String, String> init(int startRF, int endRF, int startOC, int startRoman, int endRoman, String roman,
      List<String> romanNumerals) {

    Map<String, String> inner = new HashMap<String, String>();

    for (; startRF <= endRF; startRF++) {
      inner.put(startRF + "", startRF + "");
    }

    if (roman != null) {
      int index = romanNumerals.indexOf(roman);

      for (; startRoman <= endRoman; startRoman++, index++) {
        inner.put(startRoman + "", romanNumerals.get(index));
      }
    }

    return inner;
  }

  private String extractVolumeAndPage(String pageId) {
    
    String volume = pageId.substring(13, 17);
    String pageNum = pageId.substring(18, 22);

    String[] strings = { volume, pageNum };

    for (int i = 0; i <= strings.length - 1; i++) {
      int count = 0;
      char[] string = strings[i].toCharArray();
      for (int j = 0; j < string.length - 1; j++) {
        if (string[j] == '0') {
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
}
