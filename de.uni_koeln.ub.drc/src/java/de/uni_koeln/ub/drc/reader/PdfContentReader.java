/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.reader;

import java.io.IOException;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;

/**
 * Reads content from a PDF document.
 * @author Mihail Atanassov <saeko.bjagai@gmail.com>
 */
public final class PdfContentReader {

  String txtFile;
  private PdfReader reader;

  /**
   * @param pdf The PDF document to read
   */
  public PdfContentReader(final String pdf) {
    read(pdf);
  }

  /**
   * @return The iText {@link Rectangle}
   */
  public Rectangle getPdfBox() {
    return reader.getCropBox(1);
  }

  private void read(final String file) {

    try {
      StringBuilder sb = new StringBuilder();
      reader = new PdfReader(file);
      int size = reader.getNumberOfPages();

      for (int i = 1; i <= size; i++) {
        PdfDictionary page = reader.getPageN(i);
        PRIndirectReference objectReference = (PRIndirectReference) page.get(PdfName.CONTENTS);
        PRStream stream = (PRStream) PdfReader.getPdfObject(objectReference);
        byte[] streamBytes = PdfReader.getStreamBytes(stream);
        sb.append(new String(streamBytes, "iso-8859-1"));
      }
      txtFile = new String(sb);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
