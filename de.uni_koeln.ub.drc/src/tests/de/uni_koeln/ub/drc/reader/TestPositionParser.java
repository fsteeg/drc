/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.reader;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.uni_koeln.ub.drc.data.Point;

/**
 * @author Mihail Atanassov <saeko.bjagai@gmail.com>
 */
public final class TestPositionParser {

  @Test
  public void parse() {
    List<Paragraph> paragraphs;

    paragraphs = PositionParser.parse("res/rom/PPN345572629_0004/PPN345572629_0004-0001.pdf");
    Line testLine = paragraphs.get(2).getLines().get(0);
    System.out.println(testLine.getText());
    Point scaledStart = testLine.getStartPoint(600, 960);
    Assert.assertEquals(new Point(77, 348), scaledStart);
    
    // int scaledFontSize = testLine.getFontSize(600, 960); // TODO scaled fontsize
    for (Paragraph p : paragraphs) {
      List<Line> lines = p.getLines();
      for (Line line : lines) {
        Point start = line.getStartPoint();
        float x = start.x();
        float y = start.y();
        int fontSize = line.getFontSize();
        String text = line.getText();
        Assert.assertFalse("Encoding should be correct", text.contains("�"));
        System.out.println(String.format("Position: x %s , y %s / Fontsize: %s / Text: %s", x, y,
            fontSize, text));
      }
      System.out.println();
    }

  }

}
