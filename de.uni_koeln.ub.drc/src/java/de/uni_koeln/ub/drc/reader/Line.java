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

import com.itextpdf.text.Rectangle;

/**
 * Represents a line in the scanned page.
 * @author Mihail Atanassov <saeko.bjagai@gmail.com>
 */
public final class Line {

  private Point start;
  private int fontSize;
  private String text;
  private List<String> words = new ArrayList<String>();
  private Rectangle rectangle;

  public Line(final Rectangle rectangle, final float x, final float y, final float fontSize,
      final String text) {
    this.rectangle = rectangle;
    this.start = new Point(x, y);
    this.fontSize = (int) fontSize;
    this.text = text;
    setWords(text);
  }

  public List<String> getWords() {
    return words;
  }

  public Point getStartPoint() {
    return start;
  }

  public int getFontSize() {
    return fontSize;
  }

  public String getText() {
    return text;
  }

  public Point getStartPoint(final int width, final int height) {
    Point boxPoint = getStartPoint();
    int x = getX(rectangle, boxPoint.getX(), width);
    int y = getY(rectangle, boxPoint.getY(), height);
    Point scaledPoint = new Point(x, y);

    return scaledPoint;
  }

  private void setWords(final String text) {
    String[] str = text.split(" ");
    for (String string : str) {
      words.add(string);
    }
  }

  private int getY(final Rectangle box, final float yStart, final int height) {
    return (int) ((((box.getTop() - yStart) * height)) / box.getTop());
  }

  private int getX(final Rectangle box, final float xStart, final int width) {
    return (int) ((((xStart) * width)) / box.getRight());
  }

}
