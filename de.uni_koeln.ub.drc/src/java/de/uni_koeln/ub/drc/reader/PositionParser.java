/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Parses a PDF to paragraphs and lines.
 * @author Mihail Atanassov <saeko.bjagai@gmail.com>
 */
public final class PositionParser {

  private PositionParser() {
  // Static utility class
  }

  /**
   * @param pdf The PDF document to parse
   * @return The paragraphs parsed from the PDF
   * @throws FileNotFoundException
   */
  public static List<Paragraph> parse(final String pdf) {
    // TODO Split method into smaller methods
    PdfContentReader reader = new PdfContentReader(pdf);
    List<Paragraph> paragraphs = new ArrayList<Paragraph>();
    Scanner s;
    s = new Scanner(reader.txtFile);
    StringBuilder sb = new StringBuilder();
    List<String> result = new ArrayList<String>();

    String previousTm = null;
    float previousSize = -1;
    float fontSize = -1;
    Paragraph p = null;

    while (s.hasNextLine()) {
      // FIXME last line of paragraph is first of next paragraph
      String line = s.nextLine().trim();

      if (line.equals("BT")) {
        p = new Paragraph();
        paragraphs.add(p);
        result.add("\n");
      }

      if (line.endsWith("Tf")) {
        previousSize = fontSize;
        fontSize = Float.parseFloat(line.split(" ")[1]);
      }

      if (line.endsWith("Tm")) {
        if (sb.toString().trim().length() > 0 && previousTm != null) {
          String[] tokens = previousTm.split(" ");
          float x = Float.parseFloat(tokens[4]);
          float y = Float.parseFloat(tokens[5]);
          result.add(String.format("Starting at x='%.2f', y='%.2f', size='%.2f': ", x, y,
              previousSize)
              + sb.toString());
          p.setLine(new Line(reader.getPdfBox(), x, y, fontSize, sb.toString()));
          previousSize = fontSize;
        }
        sb = new StringBuilder();
        previousTm = line;
      }

      if (line.endsWith("Tj")) {
        line = line.replaceAll("[\\(\\)]| Tj", "");
        sb.append(line);
      }
    }
    return paragraphs;
  }

}
