/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Experimental representation of a Word, the basic unit of work when editing. Conceptually, it is
 * made of an original form (the original OCR result, for which we obtain coordnates in the original
 * OCR), a position in the original scan (for display in the UI; these coordinates are hard-coded
 * and absolute here, they will be obtained from the scanned PDF files and converted to relative
 * values to allow display at various image sizes) and a stack of modifications, the top of which is
 * the current form. The history has to be maintained for review and corrections.
 * @author Fabian Steeg (fsteeg)
 */
class Word {

  /**
   * Represent a modification made to a word, consisting of the form the word is modified to and the
   * author of that modification (to maintain a modificaiton history for review and correction).
   * @author Fabian Steeg (fsteeg)
   */
  static class Modification {
    Modification(String form, String author) {
      this.form = form;
      this.author = author;
    }

    String form;
    String /* Author */author; // in the future: complex type with location, rights, etc.

    @Override
    public String toString() {
      return String.format("'%s' by '%s'", form, author);
    }
  }

  Rectangle position;
  String original;
  /* A word's history is a stack of modifications, the top of which is the current form: */
  Stack<Modification> history = new Stack<Modification>();

  public Word(String original, Rectangle position) {
    this.original = original;
    this.history.push(new Modification(original, "OCR"));
    this.position = position;
  }

  public String formattedHistory() {
    StringBuilder builder = new StringBuilder("Modification history:\n\n");
    List<Modification> mods = new ArrayList<Modification>(history);
    Collections.reverse(mods);
    for (Modification modification : mods) {
      builder.append(modification).append("\n");
    }
    return builder.toString().trim();
  }

  @Override
  public String toString() {
    return String.format("Original '%s', Current '%s', at %s, history %s", original,
        history.peek().form, position, history);
  };

  /* Dummy data for testing: */

  static List<Word> testData() {

    /*
     * This models what we get from the OCR: the original word forms as recognized by the OCR,
     * together with their coordinates in the scan result (originally a PDF with absolute values):
     */
    Map<String, Rectangle> positions = new HashMap<String, Rectangle>();
    positions.put("daniel", new Rectangle(130, 283, 150, 30));
    positions.put("bonifaci", new Rectangle(280, 285, 180, 30));
    positions.put("catechismus", new Rectangle(70, 330, 80, 20));
    positions.put("als", new Rectangle(110, 390, 30, 20));
    positions.put("slaunt", new Rectangle(78, 498, 45, 15));

    /*
     * This models the other part we get from the OCR: the full text, which we need to tokenize and
     * convert into Word objects to be displayed and edited in the UI:
     */
    String[] strings = "Daniel Bonifaci Catechismus Als Slaunt".split(" ");
    List<Word> result = new ArrayList<Word>();
    for (String w : strings) {
      result.add(new Word(w, positions.get(w.toLowerCase())));
    }
    return result;
  }

}
