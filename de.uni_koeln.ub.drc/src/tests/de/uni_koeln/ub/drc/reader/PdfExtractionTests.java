/**************************************************************************************************
* Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
* materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
* this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* <p/>
* Contributors: Mihail Atanassov - initial API and implementation
*************************************************************************************************/

package de.uni_koeln.ub.drc.reader;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
* @author Mihail Atanassov <saeko.bjagai@gmail.com>
*/
public class PdfExtractionTests {

  private String pdfName = "res/rom/PPN345572629_0004/PPN345572629_0004-0001.pdf";
  private PageInfo pi = PdfContentExtractor.extractContentFromPdf(pdfName);

  @Test
  public void encoding() {
    List<Line> lines = pi.getLines();
    for (Line line : lines) {
      List<ExtractedWord> words = line.getWordsInLine();
      for (ExtractedWord word : words) {
        String token = word.getText();
        Assert.assertFalse("Encoding should be correct", token.contains("�"));
      }
    }
  }

  @Test
  public void findTextChunks() {
    List<Line> lines = pi.getLines();
    String toFind = " pievel";
    int counts = 0;
    for (Line line : lines) {
      List<ExtractedWord> words = line.getWordsInLine();
      for (ExtractedWord extractedWord : words) {
        if (extractedWord.getText().equals(toFind)) {
          counts++;
        }
      }
    }
    Assert.assertTrue(String.format(
        "'%s' should be found 2 times, but occurs " + counts + " times", toFind), counts == 2);
  }

  @Test
  public void fontSizeScaling() {
    List<ExtractedWord> words = pi.getParagraps().get(1).getLinesInParagraph().get(0)
        .getWordsInLine();
    for (ExtractedWord extractedWord : words) {
      int fontSize1 = extractedWord.getFontSizeScaled(1000);
      int fontSize2 = extractedWord.getFontSizeScaled(900);
      Assert.assertTrue(String
          .format("Font size %s should be larger than %s", fontSize1, fontSize2),
          fontSize1 > fontSize2);
      Assert.assertTrue(String.format(
          "Font size %s should be larger than unscaled size %s (different measure)", fontSize1,
          extractedWord.getFontSize()), fontSize1 > extractedWord.getFontSize());
    }

  }

  @Test
  public void lineCount() {
    List<Line> lines = pi.getLines();
    Assert.assertTrue("There should be 30 lines detected in the PDF document", lines.size() == 30);
  }

  @Test
  public void lines() {
    List<Line> lines = pi.getLines();
    List<ExtractedWord> wordsInLine = lines.get(18).getWordsInLine();
    int wordCount = wordsInLine.size();
    Assert.assertTrue("line.size() should be 10, but is " + wordCount, wordCount == 10);
    Assert.assertEquals(" cretta,", wordsInLine.get(2).getText());
  }

  @Test
  public void paragraphs() {
    List<Paragraph> paragraphs = pi.getParagraps();
    Assert.assertTrue(paragraphs.get(0).getLinesInParagraph().get(0).getWordsInLine().get(0)
        .getText().startsWith("DANiEL"));
    Assert.assertTrue(paragraphs.size() == 6);
  }

  @Test
  public void point() {
    List<ExtractedWord> words = pi.getLineAt(1).getWordsInLine();
    Point scaledStart = words.get(0).getStartPointScaled(900, 1440);
    Point p = new Point(116, 522);
    Assert.assertEquals(p, scaledStart);
    Assert.assertTrue(words.get(0).getText().toString().startsWith("CatechiSmus"));
  }

}