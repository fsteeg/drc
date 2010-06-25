/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.reader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ContentByteUtils;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * The utility class {@code PdfContentExtractor} is optimized for parsing PDF documents generated
 * from an OCR result of ABBYY FineReader 9.0 Professional Edition.
 * @author Mihail Atanssov <saeko.bjagai@gmail.com>
 */

public final class PdfContentExtractor {

  /**
   * Constant for the field locationalResult in {@link LocationTextExtractionStrategy}.
   */
  private static final int LOCATIONRESULT = 1;
  /**
   * Constant for the field {@code String} text of {@code private static class TextChunk} in
   * {@link LocationTextExtractionStrategy}.
   */
  private static final int TEXT = 0;
  /**
   * Constant for the field {@link Vector} startLocation of {@code private static class TextChunk}
   * in {@link LocationTextExtractionStrategy}.
   */
  private static final int STARTLOCATION = 1;
  /**
   * Constant for the field {@link Vector} endLocation of {@code private static class TextChunk} in
   * {@link LocationTextExtractionStrategy}.
   */
  private static final int ENDLOCATION = 2;
  /**
   * Constant for the field {@link float} charSpaceWidth of {@code private static class TextChunk}
   * in {@link LocationTextExtractionStrategy}.
   */
  private static final int CHARSPACEWIDTH = 8;

  private PdfContentExtractor() {
  // static utility class
  }

  /**
   * Parses the PDF document and returns a {@link List} of {@link ExtractedWord}.
   * @param pdfName Name of the PDF document to be parsed
   * @return Instance of {@link PageInfo}
   */
  public static PageInfo extractContentFromPdf(final String pdfName) {

    List<ExtractedWord> extractedWords = new ArrayList<ExtractedWord>();
    PdfReader reader = null;
    Map<Point, Float> map = null;
    Float fontSize = 0F;

    try {

      reader = new PdfReader(pdfName);

      int numsOfPages = reader.getNumberOfPages();

      for (int page = 1; page <= numsOfPages; page++) {

        byte[] content = reader.getPageContent(page);
        String rawContent = new String(content);
        map = getParagraphsAndFontSizes(rawContent);

        LocationTextExtractionStrategy strategy = new LocationTextExtractionStrategy();

        PdfDictionary pageDic = reader.getPageN(page);
        PdfDictionary resourcesDic = pageDic.getAsDict(PdfName.RESOURCES);

        PdfContentStreamProcessor processor = new PdfContentStreamProcessor(strategy);
        processor.processContent(ContentByteUtils.getContentBytesForPage(reader, page),
            resourcesDic);

        // Begin Reflection
        try {

          /*
           * Reflecting the com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy Class
           */
          Class<?> cls = LocationTextExtractionStrategy.class;
          // Class.forName("com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy");

          Field[] fields = cls.getDeclaredFields();

          /*
           * To access the fields they have to be set accessible == true
           */
          for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
          }

          /*
           * With the get() method of the class Field and by passing a instance of the
           * LocationTextExtractionStrategy we retrieve an instance of type Object
           */
          Object o = fields[LOCATIONRESULT].get(strategy);
          /*
           * Just perform a simple cast to convert to the right type.
           */
          List<?> locationalResult = (List<?>) o;

          for (Object object : locationalResult) {

            Field[] innerFields = object.getClass().getDeclaredFields();

            for (Field field : innerFields) {
              field.setAccessible(true);
            }
            /*
             * Now it is important to retrieve the fields in the same order like there are listed in
             * the private inner class
             * com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy.TextChunk
             */
            String text = (String) innerFields[TEXT].get(object);
            Vector startLocation = (Vector) innerFields[STARTLOCATION].get(object);
            Vector endLocation = (Vector) innerFields[ENDLOCATION].get(object);
            Float charSpaceWidth = (Float) innerFields[CHARSPACEWIDTH].get(object);

            /*
             * Create an instance of ExtractedWord class.
             */

            boolean isParagraphStart = false;

            for (Point p1 : map.keySet()) {
              Point p2 = new Point(startLocation.get(Vector.I1), startLocation.get(Vector.I2));
              if (p1.equals(p2)) {
                isParagraphStart = true;
                fontSize = map.get(p1);
              }
            }

            ExtractedWord tmp = new ExtractedWord(text, startLocation, endLocation, charSpaceWidth,
                isParagraphStart, fontSize, reader.getCropBox(page));

            /*
             * At least we add the instance of ExtractedWord to the list extractedWords
             */
            extractedWords.add(tmp);
          }
        } catch (SecurityException e) {
          e.printStackTrace();
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }

        // End Reflection
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    List<ExtractedWord> result = concatenate(extractedWords);
    return new PageInfo(result);
  }

  /**
   * @param extractedWords List of {@link ExtractedWord}
   * @return List of merged {@link ExtractedWord}
   */
  private static List<ExtractedWord> concatenate(final List<ExtractedWord> extractedWords) {
    List<ExtractedWord> toReturn = new ArrayList<ExtractedWord>();

    for (int i = 0; i < extractedWords.size() - 1;) {

      ExtractedWord current = extractedWords.get(i);
      ExtractedWord currentNext = extractedWords.get(i + 1);

      /* First token of a line or token in line */
      if (firstOfLineOrInLine(current, currentNext)) {
        ExtractedWord ew = mergeFirstOrInLineExtractedWords(current, currentNext);
        toReturn.add(ew);
        i += 2;
        /* Token end of line or token not spliced */
      } else if (endOfLineOrNotSpliced(current, currentNext)) {
        toReturn.add(current);
        i++;
        /* Token spliced 3 times */
      } else if (toReturn.size() > 0) {
        ExtractedWord removed = toReturn.remove(toReturn.size() - 1);
        String text = removed.getText() + current.getText();
        Vector startLocation = removed.getStartLocation();
        Vector endLocation = current.getEndLocation();
        Float charSpaceWidth = current.getCharSpaceWidth();
        boolean isParagraphStart = current.isParagraphStart();
        Float fontSize = removed.getFontSize();
        Rectangle rectangle = removed.getRectangle();
        ExtractedWord ew = new ExtractedWord(text, startLocation, endLocation, charSpaceWidth,
            isParagraphStart, fontSize, rectangle);
        toReturn.add(ew);
        i++;
        /* Default case */
      } else {
        toReturn.add(current);
        i++;
      }

    }
    toReturn.add(extractedWords.get(extractedWords.size() - 1));
    return toReturn;
  }

  private static boolean endOfLineOrNotSpliced(final ExtractedWord current,
      final ExtractedWord currentNext) {

    boolean a = current.getText().startsWith(" ") && currentNext.getText().startsWith(" ");

    boolean b = current.getText().startsWith(" ") && current.getText().endsWith(" ");

    return a || b;
  }

  private static boolean firstOfLineOrInLine(final ExtractedWord current,
      final ExtractedWord currentNext) {

    boolean a = !current.getText().startsWith(" ") && !currentNext.getText().startsWith(" ")
        && current.getEndPoint().equals(currentNext.getStartPoint());

    boolean b = current.getText().startsWith(" ") && !currentNext.getText().startsWith(" ")
        && current.getEndPoint().equals(currentNext.getStartPoint());

    return a || b;
  }

  private static Map<Point, Float> getParagraphsAndFontSizes(final String rawContent) {
    Scanner s = new Scanner(rawContent);

    List<Float> fontSizes = new ArrayList<Float>();
    List<Point> paragraphStartingPoints = new ArrayList<Point>();

    Map<Point, Float> map = new HashMap<Point, Float>();
    boolean newBT = false;

    while (s.hasNext()) {
      String line = s.nextLine().trim();
      if (line.equals("BT")) {
        newBT = true;
      }

      if (line.endsWith("Tf") && newBT) {
        fontSizes.add(Float.parseFloat(line.split(" ")[1]));
      }

      if (line.endsWith("Tm") && newBT) {
        String[] tokens = line.split(" ");
        float x = Float.parseFloat(tokens[4]);
        float y = Float.parseFloat(tokens[5]);
        Point p = new Point(x, y);
        paragraphStartingPoints.add(p);
        newBT = false;
      }
    }

    for (int i = 0; i < paragraphStartingPoints.size() && i < fontSizes.size(); i++) {
      Point p = paragraphStartingPoints.get(i);
      Float f = fontSizes.get(i);
      map.put(p, f);
    }

    return map;
  }

  /**
   * @param current The current {@link ExtractedWord}
   * @param currentNext The current.next {@link ExtractedWord}
   * @return A new {@link ExtractedWord} merged from current and currentNext
   */
  private static ExtractedWord mergeFirstOrInLineExtractedWords(final ExtractedWord current,
      final ExtractedWord currentNext) {

    String text = current.getText() + currentNext.getText();
    Vector startLocation = current.getStartLocation();
    Vector endLocation = currentNext.getEndLocation();
    Float charSpaceWidth = current.getCharSpaceWidth();
    boolean isParagraphStart = current.isParagraphStart();
    Float fontSize = current.getFontSize();
    Rectangle rectangle = current.getRectangle();

    ExtractedWord ew = new ExtractedWord(text, startLocation, endLocation, charSpaceWidth,
        isParagraphStart, fontSize, rectangle);
    return ew;
  }
}
