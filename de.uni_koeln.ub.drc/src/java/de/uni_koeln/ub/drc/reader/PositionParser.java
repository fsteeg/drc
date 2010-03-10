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
   */
  public static List<Paragraph> parse(final String pdf) {
    PdfContentReader reader = new PdfContentReader(pdf);
    List<Paragraph> paragraphs = new ArrayList<Paragraph>();
    Scanner s = new Scanner(reader.getPdfContent());

    String previousTm = null;
    float fontSize = -1;
    Paragraph p = null;
    Line currentLine = null;

    while (s.hasNextLine()) {
      String line = s.nextLine().trim();

      if (line.equals("BT")) {
        p = new Paragraph();
        paragraphs.add(p);
      }

      if (line.endsWith("Tf")) {
        fontSize = Float.parseFloat(line.split(" ")[1]);
      }

      if (line.endsWith("Tm")) {
        if (previousTm != null) {
          String[] tokens = previousTm.split(" ");
          float x = Float.parseFloat(tokens[4]);
          float y = Float.parseFloat(tokens[5]);
          currentLine = new Line(reader.getPdfBox(), x, y, fontSize, new StringBuilder());
          p.addLine(currentLine);
        }
        previousTm = line;
      }

      if (currentLine != null && line.endsWith("Tj")) {
        line = line.replaceAll("[\\(\\)]| Tj", "");
        currentLine.getText().append(line);
      }
    }
    return paragraphs;
  }

}
