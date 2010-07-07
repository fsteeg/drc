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
 * @author Mihail Atanssov <saeko.bjagai@gmail.com>
 */
public final class PageInfo {

  private List<Line> lines = new ArrayList<Line>();
  private List<Paragraph> paragraphs = new ArrayList<Paragraph>();
  private List<ExtractedWord> words;

  /**
   * @param words The words extracted from the PDF document
   */
  public PageInfo(final List<ExtractedWord> words) {
    this.words = words;
    toLines();
    toParagrphs();
  }

  /**
   * @param index Position of a line in the PDF document
   * @return The line from index
   */
  public Line getLineAt(final int index) {
    if (index >= 0 && index <= lines.size()) {
      return lines.get(index);
    } else {
      return null;
    }
  }

  /**
   * @return All lines from the PDF document
   */
  public List<Line> getLines() {
    return new ArrayList<Line>(lines);
  }

  /**
   * @param index Position of a paragraph in the PDF document
   * @return The paragraph from index
   */
  public Paragraph getParagraphAt(final int index) {
    if (index >= 0 && index <= paragraphs.size()) {
      return paragraphs.get(index);
    } else {
      return null;
    }
  }

  /**
   * @return All paragraphs from the PDF document
   */
  public List<Paragraph> getParagraps() {
    return new ArrayList<Paragraph>(paragraphs);
  }

  private void toLines() {
    Line line = null;
    for (ExtractedWord ew : words) {
      if (!ew.getText().startsWith(" ")) {
        // FIXME not reliable e.g. page 1 volume 4
        line = new Line();
        lines.add(line);
      }
      line.addExtractedWord(ew);
    }
  }

  private void toParagrphs() {
    Paragraph paragraph = null;
    for (Line line : lines) {
      if (line.getWordsInLine().get(0).isParagraphStart()) {
        paragraph = new Paragraph();
        paragraphs.add(paragraph);
      }
      paragraph.addLine(line);
    }
  }
}
