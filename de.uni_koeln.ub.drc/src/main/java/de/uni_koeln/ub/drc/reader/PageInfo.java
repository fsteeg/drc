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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mihail Atanssov <saeko.bjagai@gmail.com>(original version) <br/>
 *         Fabian Steeg <fsteeg@gmail.com> (Refactored for PdfBox)
 */
public final class PageInfo {

	private List<Paragraph> paragraphs = new ArrayList<Paragraph>();
	private List<ExtractedWord> words;

	/**
	 * @param words
	 *            The words extracted from the PDF document
	 */
	public PageInfo(final List<ExtractedWord> words) {
		this.words = words;
		toParagraphs();
	}

	/**
	 * @param index
	 *            Position of a paragraph in the PDF document
	 * @return The paragraph from index
	 */
	public Paragraph getParagraphAt(final int index) {
		if (index >= 0 && index <= paragraphs.size())
			return paragraphs.get(index);
		return null;
	}

	/**
	 * @return All paragraphs from the PDF document
	 */
	public List<Paragraph> getParagraphs() {
		return new ArrayList<Paragraph>(paragraphs);
	}

	private void toParagraphs() {
		Paragraph paragraph = new Paragraph();
		paragraphs.add(paragraph);
		for (ExtractedWord word : words) {
			if (word.isParagraphStart()) {
				paragraph = new Paragraph();
				paragraph.addWord(word);
				paragraphs.add(paragraph);
			} else {
				paragraph.addWord(word);
			}
		}
	}

	@Override
	public String toString() {
		return String.format("%s with %s paragraphs, %s words", getClass() //$NON-NLS-1$
				.getSimpleName(), paragraphs.size(), words.size());
	}
}
