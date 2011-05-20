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

import java.awt.print.PageFormat;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.TextPosition;

import de.uni_koeln.ub.drc.reader.temp.PDFTextStripper2;
import de.uni_koeln.ub.drc.reader.temp.PositionWrapper;

/**
 * The utility class {@code PdfContentExtractor} is optimized for parsing PDF
 * documents generated from an OCR result of ABBYY FineReader 9.0 Professional
 * Edition. Subclasses a PdfBox PDFTextStripper to get the text, paragraph and
 * and position information.
 * 
 * @author Mihail Atanssov <saeko.bjagai@gmail.com> (original version) <br/>
 *         Fabian Steeg <fsteeg@gmail.com> (Refactored for PdfBox)
 */
public class PdfContentExtractor extends PDFTextStripper2 {

	private static String location;
	private List<TextPosition> paragraphs = new ArrayList<TextPosition>();

	/**
	 * @throws IOException
	 *             From superclass
	 */
	public PdfContentExtractor() throws IOException {
		super();
	}

	@Override
	protected void isParagraphSeparation(final PositionWrapper position,
			final PositionWrapper lastPosition,
			final PositionWrapper lastLineStartPosition) {
		/*
		 * TODO we can get lines from here if we need to, we will be called here
		 * for every line
		 */
		super.isParagraphSeparation(position, lastPosition,
				lastLineStartPosition);
		if (position.isParagraphStart()) {
			paragraphs.add(position.getTextPosition());
		}
	}

	/**
	 * @param pdfName
	 *            The full path to the PDF file to extract content from
	 * @return The PageInfo object for the PDF
	 */
	public static PageInfo extractContentFromPdf(String pdfName) {
		try {
			location = pdfName;
			PDDocument document = PDDocument.load(new File(pdfName));
			PdfContentExtractor x = initExtractor(document);
			PageInfo result = x.toPageInfo();
			document.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static PdfContentExtractor initExtractor(final PDDocument document)
			throws IOException {
		StringWriter writer = new StringWriter();
		PdfContentExtractor x = new PdfContentExtractor();
		x.setDropThreshold(3.75f);
		// x.setIndentThreshold(1f); // for tweaking paragraph detection
		try {
			x.writeText(document, writer);
		} catch (NullPointerException e) {
			System.err.println("Could not process: " + location); //$NON-NLS-1$
			e.printStackTrace();
		}
		return x;
	}

	private PageInfo toPageInfo() {
		Vector<List<TextPosition>> positions = charactersByArticle;
		List<ExtractedWord> words = new ArrayList<ExtractedWord>();
		if (positions.size() == 0 || positions.get(0).size() == 0) {
			System.err.println("No content found for: " + location); //$NON-NLS-1$
			return new PageInfo(words);
		}
		TextPosition currentWordStart = positions.get(0).get(0);
		StringBuilder currentWordText = new StringBuilder();
		for (List<TextPosition> list : positions) {
			for (TextPosition pos : list) {
				if (currentWordStart == null) {
					currentWordStart = pos; // remember start for new words
				}
				currentWordText.append(pos.getCharacter());
				if (pos.getCharacter().equals(" ") //$NON-NLS-1$
						|| pos.getCharacter().equals("-")) { //$NON-NLS-1$
					ExtractedWord w = word(currentWordStart, currentWordText,
							pos);
					if (currentWordText.toString().trim().length() > 0) {
						words.add(w);
					}
					currentWordText = new StringBuilder();
					currentWordStart = null; // forget current word start
				}
			}
		}
		return new PageInfo(words);
	}

	private ExtractedWord word(final TextPosition currentWordStart,
			final StringBuilder currentWord, final TextPosition endPosition) {
		String wordText = currentWord.toString();
		PageFormat format = document.getPageFormat(0);
		double width = format.getWidth();
		double height = format.getHeight();
		Point start = new Point(currentWordStart.getX(), height
				- currentWordStart.getY());
		Point end = new Point(endPosition.getX(), height - endPosition.getY());
		ExtractedWord w = new ExtractedWord(wordText, start, end,
				paragraphs.contains(currentWordStart),
				endPosition.getFontSize(), width, height);
		return w;
	}
}
