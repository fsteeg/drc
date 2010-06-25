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
  private StringBuilder sb;
  private Rectangle rectangle;
  private List<Float> wordSpaces;

  /**
   * @param rectangle The iText {@link Rectangle}
   * @param x The X-coordinate of the beginning of the line within the PDF document
   * @param y The Y-coordinate of the beginning of the line within the PDF document
   * @param fontSize The font size of the line
   * @param sb The {@link StringBuilder} which appends the words of the line
   * @param wordSpaces A List of word spaces of the line
   */
  public Line(final Rectangle rectangle, final float x, final float y, final float fontSize,
      final StringBuilder sb, final List<Float> wordSpaces) {
    this.rectangle = rectangle;
    this.start = new Point(x, y);
    this.fontSize = (int) fontSize;
    this.sb = sb;
    this.wordSpaces = wordSpaces;
  }

  /**
   * @param wordSpace The word-spacing parameter
   */
  void addWordSpace(final float wordSpace) {
    wordSpaces.add(wordSpace);
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
    int x = getX(rectangle, boxPoint.getX(), width);
    int y = getY(rectangle, boxPoint.getY(), height);
    Point scaledPoint = new Point(x, y);
    return scaledPoint;
  }

  /**
   * @param height The height of the JPG image
   * @return The scaled font size of the line
   */
  public int getFontSizeScaled(final int height) {
    return (int) ((height * getFontSize()) / rectangle.getHeight());
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
