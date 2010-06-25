/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.reader;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * Represents a chunk of text, it's location vectors and the character space width.
 */
public final class ExtractedWord {

  /** the text of the chunk */
  private String text;
  /** the starting location of the chunk */
  private Vector startLocation;
  /** the ending location of the chunk */
  private Vector endLocation;
  /** the width of a single space character in the font of the chunk */
  private float charSpaceWidth;

  private boolean isParagraphStart;
  private Float fontSize;
  private Rectangle rectangle;

  /**
   * @param text The extracted text chunk of the PDF document
   * @param startLocation {@link Vector} start location of the text chunk
   * @param endLocation {@link Vector} end location of the text chunk
   * @param charSpaceWidth the character space width
   * @param isParagraphStart boolean
   * @param fontSize The font size
   * @param rectangle The iText {@link Rectangle}
   */
  public ExtractedWord(final String text, final Vector startLocation, final Vector endLocation,
      final Float charSpaceWidth, final boolean isParagraphStart, final Float fontSize,
      final Rectangle rectangle) {
    setText(text);
    setStartLocation(startLocation);
    setEndLocation(endLocation);
    setCharSpaceWidth(charSpaceWidth);
    setParagraphStart(isParagraphStart);
    setFontSize(fontSize);
    setRectangle(rectangle);
  }

  /**
   * @return The width of a single space character in the font of the text chunk
   */
  public float getCharSpaceWidth() {
    return charSpaceWidth;
  }

  /**
   * @return The end location of the text chunk as {@link Vector}
   */
  public Vector getEndLocation() {
    return endLocation;
  }

  /**
   * @return The end point of the chunk
   */
  public Point getEndPoint() {
    return new Point(endLocation.get(Vector.I1), endLocation.get(Vector.I2));
  }

  /**
   * @return The font size
   */
  public Float getFontSize() {
    return fontSize;
  }

  /**
   * @param height The height of the JPG image
   * @return The scaled font size of the line
   */
  public int getFontSizeScaled(final int height) {
    return (int) ((height * getFontSize()) / rectangle.getHeight());
  }

  /**
   * @return The iText {@link Rectangle}
   */
  public Rectangle getRectangle() {
    return rectangle;
  }

  /**
   * @return The start location of the text chunk as {@link Vector}
   */
  public Vector getStartLocation() {
    return startLocation;
  }

  /**
   * @return The start point of the chunk
   */
  public Point getStartPoint() {
    return new Point(startLocation.get(Vector.I1), startLocation.get(Vector.I2));
  }

  /**
   * @param width The width of the JPG image
   * @param height The height of the JPG image
   * @return The scaled starting point of the line
   */
  public Point getStartPointScaled(final int width, final int height) {
    return scaled(width, height, getStartPoint());
  }
  
  /**
   * @param width The width of the JPG image
   * @param height The height of the JPG image
   * @return The scaled end point of the line
   */
  public Point getEndPointScaled(final int width, final int height) {
    return scaled(width, height, getEndPoint());
  }

  private Point scaled(final int width, final int height, final Point boxPoint) {
    int x = getX(rectangle, boxPoint.getX(), width);
    int y = getY(rectangle, boxPoint.getY(), height);
    Point scaledPoint = new Point(x, y);
    return scaledPoint;
  }

  /**
   * @return the text of the text chunk
   */
  public String getText() {
    return text;
  }

  /**
   * @return {@code true} if it's the first text chunk of an {@link Paragraph} otherwise {@code
   *         false}
   */
  public boolean isParagraphStart() {
    return isParagraphStart;
  }

  @Override
  public String toString() {
    return "'" + text + "' | " + getStartPoint() + " | " + getEndPoint();
  }

  private int getX(final Rectangle box, final float xStart, final int width) {
    return (int) ((((xStart) * width)) / box.getRight());
  }

  private int getY(final Rectangle box, final float yStart, final int height) {
    return (int) ((((box.getTop() - yStart) * height)) / box.getTop());
  }

  private void setCharSpaceWidth(final float charSpaceWidth) {
    this.charSpaceWidth = charSpaceWidth;
  }

  private void setEndLocation(final Vector endLocation) {
    this.endLocation = endLocation;
  }

  private void setFontSize(final Float fontSize) {
    this.fontSize = fontSize;
  }

  private void setParagraphStart(final boolean isParagraphStart) {
    this.isParagraphStart = isParagraphStart;
  }

  private void setRectangle(final Rectangle rectangle) {
    this.rectangle = rectangle;

  }

  private void setStartLocation(final Vector startLocation) {
    this.startLocation = startLocation;
  }

  private void setText(final String text) {
    this.text = text;
  }

}