/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

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

  private void toTxt(final String file, final StringBuilder sb) {
    try {
      String s = new String(sb);
      PrintWriter pw = new PrintWriter(new File(txtFile), "iso-8859-1");
      pw.write(s);
      pw.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
        sb.append(new String(streamBytes));
      }

      txtFile = file.replace(".pdf", ".txt");
      toTxt(txtFile, sb);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
