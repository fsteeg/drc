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
 * @author Mihail Atanassov <saeko.bjagai@gmail.com>
 */
public final class Paragraph {

  private List<Line> lines = new ArrayList<Line>();

  /**
   * @param line The line to be added to the paragraph
   */
  void addLine(final Line line) {
    lines.add(line);
  }

  /**
   * @return The Lines of a paragraph
   */
  public List<Line> getLinesInParagraph() {
    return new ArrayList<Line>(lines);
  }

}
