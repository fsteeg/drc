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

/**
 * Represents a paragraph in the scanned page.
 * 
 * @author Mihail Atanassov <saeko.bjagai@gmail.com>
 */
public final class Paragraph {

	private List<ExtractedWord> words = new ArrayList<ExtractedWord>();

	/**
	 * @param line
	 *            The line to be added to the paragraph
	 */
	void addWord(final ExtractedWord line) {
		words.add(line);
	}

	/**
	 * @return The Lines of a paragraph
	 */
	public List<ExtractedWord> getWords() {
		return new ArrayList<ExtractedWord>(words);
	}

}
