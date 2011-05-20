package de.uni_koeln.ub.drc.reader;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link PdfContentExtractor} class.
 */
public class PdfExtractionTests {

	private String pdfName = "res/tests/PPN345572629_0004 - 0007.pdf"; //$NON-NLS-1$
	private PageInfo pi = PdfContentExtractor.extractContentFromPdf(pdfName);

	/**
	 * Check word extraction and tokenization.
	 * 
	 * @throws IOException
	 *             On loading issues.
	 */
	@Test
	public void words() throws IOException {
		System.out.println("PageInfo: " + pi); //$NON-NLS-1$
		List<Paragraph> paragraps = pi.getParagraphs();
		for (Paragraph paragraph : paragraps) {
			List<ExtractedWord> words = paragraph.getWords();
			for (ExtractedWord word : words) {
				Assert.assertFalse("There should be no empty words in: " //$NON-NLS-1$
						+ words, word.getText().trim().length() == 0);
				Assert.assertFalse("Encoding should be correct", word.getText() //$NON-NLS-1$
						.contains("�")); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Count extracted text chunks.
	 */
	@Test
	public void findTextChunks() {
		List<Paragraph> ps = pi.getParagraphs();
		String toFind = "pievel"; //$NON-NLS-1$
		int counts = 0;
		for (Paragraph para : ps) {
			// List<ExtractedWord> words = para.getWordsInLine();
			for (ExtractedWord extractedWord : para.getWords()) {
				if (extractedWord.getText().contains(toFind)) {
					counts++;
				}
			}
		}
		Assert.assertTrue(
				String.format("'%s' should be found 2 times, but occurs " //$NON-NLS-1$
						+ counts + " times", toFind), counts == 2); //$NON-NLS-1$
	}

	/**
	 * Test font scaling.
	 */
	@Test
	public void fontSizeScaling() {
		List<ExtractedWord> words = pi.getParagraphs().get(1).getWords();
		for (ExtractedWord extractedWord : words) {
			int fontSize1 = extractedWord.getFontSizeScaled(1440);
			int fontSize2 = extractedWord.getFontSizeScaled(900);
			Assert.assertTrue(String.format(
					"Font size %s should be larger than %s", fontSize1, //$NON-NLS-1$
					fontSize2), fontSize1 > fontSize2);
			Assert.assertTrue(
					String.format(
							"Font size %s should be larger than unscaled size %s (different measure)", //$NON-NLS-1$
							fontSize1, extractedWord.getFontSize()),
					fontSize1 > extractedWord.getFontSize());
		}

	}

	/**
	 * Test paragraph detection.
	 */
	@Test
	public void paragraphs() {
		List<Paragraph> paragraphs = pi.getParagraphs();
		Assert.assertTrue(paragraphs.get(0).getWords().get(0).getText()
				.startsWith("DANiEL")); //$NON-NLS-1$
		Assert.assertTrue(paragraphs.size() == 4);
	}

	/**
	 * Test coordinates scaling.
	 */
	@Test
	public void point() {
		List<ExtractedWord> words = pi.getParagraphs().get(1).getWords();
		Point scaledStart = words.get(0).getStartPointScaled(900, 1440);
		Point p = new Point(192, 564);
		Assert.assertEquals(p, scaledStart);
		Assert.assertTrue(words.get(0).getText().toString()
				.startsWith("(Abgedruckt")); //$NON-NLS-1$
	}

}
