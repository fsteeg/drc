/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: <br/>
 * Mihail Atanassov - initial API and implementation <br/>
 * Fabian Steeg - Refactored for PdfBox
 *************************************************************************************************/

package de.uni_koeln.ub.drc.reader;

/**
 * Represents a chunk of text, it's location points and the character space
 * width.
 */
public final class ExtractedWord {

	/** the text of the chunk */
	private String text;
	/** the starting location of the chunk */
	private Point startLocation;
	/** the ending location of the chunk */
	private Point endLocation;
	private boolean isParagraphStart;
	private Float fontSize;
	Double pageWidth;
	Double pageHeight;

	/**
	 * @param text
	 *            The extracted text chunk of the PDF document
	 * @param startLocation
	 *            {@link Point} start location of the text chunk
	 * @param endLocation
	 *            {@link Point} end location of the text chunk
	 * @param isParagraphStart
	 *            boolean
	 * @param fontSize
	 *            The font size
	 * @param pageWidth
	 *            The page width
	 * @param pageHeight
	 *            The page height
	 */
	public ExtractedWord(final String text, final Point startLocation,
			final Point endLocation, final boolean isParagraphStart,
			final Float fontSize, final Double pageWidth,
			final Double pageHeight) {
		setText(text);
		this.startLocation = startLocation;
		this.endLocation = endLocation;
		setParagraphStart(isParagraphStart);
		setFontSize(fontSize);
		this.pageHeight = pageHeight;
		this.pageWidth = pageWidth;
	}

	/**
	 * @return The end point of the chunk
	 */
	public Point getEndPoint() {
		return endLocation;
	}

	/**
	 * @return The font size
	 */
	public Float getFontSize() {
		return fontSize;
	}

	/**
	 * @param height
	 *            The height of the JPG image
	 * @return The scaled font size of the line
	 */
	public int getFontSizeScaled(final int height) {
		return (int) ((height * getFontSize()) / pageHeight);
	}

	/**
	 * @return The start point of the chunk
	 */
	public Point getStartPoint() {
		return startLocation;
	}

	/**
	 * @param width
	 *            The width of the JPG image
	 * @param height
	 *            The height of the JPG image
	 * @return The scaled starting point of the line
	 */
	public Point getStartPointScaled(final int width, final int height) {
		return scaled(width, height, getStartPoint());
	}

	/**
	 * @param width
	 *            The width of the JPG image
	 * @param height
	 *            The height of the JPG image
	 * @return The scaled end point of the line
	 */
	public Point getEndPointScaled(final int width, final int height) {
		return scaled(width, height, getEndPoint());
	}

	private Point scaled(final int width, final int height, final Point boxPoint) {
		int x = getX(boxPoint.getX(), width);
		int y = getY(boxPoint.getY(), height);
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
	 * @return {@code true} if it's the first text chunk of an {@link Paragraph}
	 *         otherwise {@code false}
	 */
	public boolean isParagraphStart() {
		return isParagraphStart;
	}

	@Override
	public String toString() {
		return "'" + text + "' | " + getStartPoint() + " | " + getEndPoint();//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}

	private int getX(final double xStart, final int width) {
		return (int) ((((xStart) * width)) / pageWidth);
	}

	private int getY(final double yStart, final int height) {
		return (int) ((((pageHeight - yStart) * height)) / pageHeight);
	}

	private void setFontSize(final Float fontSize) {
		this.fontSize = fontSize;
	}

	private void setParagraphStart(final boolean isParagraphStart) {
		this.isParagraphStart = isParagraphStart;
	}

	private void setText(final String text) {
		this.text = text;
	}

}