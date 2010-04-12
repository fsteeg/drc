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

import de.uni_koeln.ub.drc.data.Point;

/**
 * Represents a line in the scanned page.
 * @author Mihail Atanassov <saeko.bjagai@gmail.com>
 */
public final class Line {

  private Point start;
  private int fontSize;
  private StringBuilder sb;
  private Rectangle rectangle;

  /**
   * @param rectangle The iText {@link Rectangle}
   * @param x The X-coordinate of the beginning of the line within the PDF document
   * @param y The Y-coordinate of the beginning of the line within the PDF document
   * @param fontSize The font size of the line
   * @param sb The {@link StringBuilder} which appends the words of the line
   */
  public Line(final Rectangle rectangle, final float x, final float y, final float fontSize,
      final StringBuilder sb) {
    this.rectangle = rectangle;
    this.start = new Point(x, y);
    this.fontSize = (int) fontSize;
    this.sb = sb;
  }

  /**
   * @return The words from the line
   */
  public List<String> getWords() {
    List<String> words = new ArrayList<String>();
    String[] str = sb.toString().split(" ");
    for (String string : str) {
      words.add(string);
    }
    return words;
  }

  /**
   * The starting point is find in the lower left corner of the first letter of the line.
   * @return The starting point of the Line
   */
  public Point getStartPoint() {
    return start;
  }

  /**
   * @param width The width of the JPG image
   * @param height The height of the JPG image
   * @return The scaled starting point of the line
   */
  public Point getStartPointScaled(final int width, final int height) {
    Point boxPoint = getStartPoint();
    int x = getX(rectangle, boxPoint.x(), width);
    int y = getY(rectangle, boxPoint.y(), height);
    Point scaledPoint = new Point(x, y);
    return scaledPoint;
  }

  /**
   * @param heigth The height of the JPG image
   * @return The scaled font size of the line
   */
  public int getfontSizetScaled(final int heigth) {
    return (int) ((heigth * getFontSize()) / rectangle.getHeight());
  }

  /**
   * @return The font size of the line
   */
  public int getFontSize() {
    return fontSize;
  }

  /**
   * @return The text
   */
  public StringBuilder getText() {
    return sb;
  }

  private int getY(final Rectangle box, final float yStart, final int height) {
    return (int) ((((box.getTop() - yStart) * height)) / box.getTop());
  }

  private int getX(final Rectangle box, final float xStart, final int width) {
    return (int) ((((xStart) * width)) / box.getRight());
  }

}
