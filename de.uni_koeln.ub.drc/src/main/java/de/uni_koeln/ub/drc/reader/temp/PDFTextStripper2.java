/**
 *
 */
package de.uni_koeln.ub.drc.reader.temp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextNormalize;
import org.apache.pdfbox.util.TextPosition;
import org.apache.pdfbox.util.TextPositionComparator;

/**
 * subclass of {@link PDFTextStripper PDFTextStripper} that attempts to detect
 * white-space between paragraphs. This class inserts an extra newline when it
 * finds such. This also inserts an extra newline before each page start.
 * Subclasses can override that behavior by overriding the
 * {@link #getLineSeparator()}, {@link #getParagraphStart()},
 * {@link #getParagraphEnd()} and {@link #getPageSeparator()} methods.
 * <p>
 * The values used for paragraph drop and indent detection can be set
 * programmatically using {@link #setDropThreshold(float)} and
 * {@link #setIndentThreshold(float)} or the defaults can be changed using the
 * System Properties:
 * 
 * <pre>
 *     pdftextstripper2.drop
 *     pdftextstripper2.indent
 * </pre>
 * 
 * which can be set using the -D switch at the start of the Java runtime.
 * </p>
 * 
 * @author m.martinez@ll.mit.edu
 * 
 */
/* Third-party code */@SuppressWarnings("all")
public class PDFTextStripper2 extends PDFTextStripper {

	private static final Class thisClass = PDFTextStripper2.class;

	private static float DEFAULT_INDENT_THRESHOLD = 2.0f;
	private static float DEFAULT_DROP_THRESHOLD = 2.5f;
	// enable the ability to set the default indent/drop thresholds
	// with -D system properties:
	// pdftextstripper2.indent
	// pdftextstripper2.drop
	static {
		String prop = thisClass.getSimpleName().toLowerCase() + ".indent";
		String s = System.getProperty(prop);
		if (s != null && s.length() > 0) {
			try {
				float f = Float.parseFloat(s);
				DEFAULT_INDENT_THRESHOLD = f;
			} catch (NumberFormatException nfe) {
				// ignore and use default
			}
		}
		prop = thisClass.getSimpleName().toLowerCase() + ".drop";
		s = System.getProperty(prop);
		if (s != null && s.length() > 0) {
			try {
				float f = Float.parseFloat(s);
				DEFAULT_DROP_THRESHOLD = f;
			} catch (NumberFormatException nfe) {
				// ignore and use default
			}
		}
	}

	private String paragraphStart = "";
	private String paragraphEnd = lineSeparator;
	private String pageStart = lineSeparator;
	private String pageEnd = lineSeparator;
	private String articleStart = lineSeparator;
	private String articleEnd = lineSeparator;

	private float indentThreshold = DEFAULT_INDENT_THRESHOLD;
	private float dropThreshold = DEFAULT_DROP_THRESHOLD;

	/**
	 * returns the multiple of whitespace character widths for the current text
	 * which the current line start can be indented from the previous line start
	 * beyond which the current line start is considered to be a paragraph
	 * start.
	 * 
	 * @return the number of whitespace character widths to use when detecting
	 *         paragraph indents.
	 */
	public float getIndentThreshold() {
		return indentThreshold;
	}

	/**
	 * sets the multiple of whitespace character widths for the current text
	 * which the current line start can be indented from the previous line start
	 * beyond which the current line start is considered to be a paragraph
	 * start. The default value is 2.0.
	 * 
	 * @param indentThreshold
	 *            the number of whitespace character widths to use when
	 *            detecting paragraph indents.
	 */
	public void setIndentThreshold(float indentThreshold) {
		this.indentThreshold = indentThreshold;
	}

	/**
	 * the minimum whitespace, as a multiple of the max height of the current
	 * characters beyond which the current line start is considered to be a
	 * paragraph start.
	 * 
	 * @return the character height multiple for max allowed whitespace between
	 *         lines in the same paragraph.
	 */
	public float getDropThreshold() {
		return dropThreshold;
	}

	/**
	 * sets the minimum whitespace, as a multiple of the max height of the
	 * current characters beyond which the current line start is considered to
	 * be a paragraph start. The default value is 2.5.
	 * 
	 * @param dropThreshold
	 *            the character height multiple for max allowed whitespace
	 *            between lines in the same paragraph.
	 */
	public void setDropThreshold(float dropThreshold) {
		this.dropThreshold = dropThreshold;
	}

	public String getParagraphStart() {
		return paragraphStart;
	}

	public void setParagraphStart(String s) {
		this.paragraphStart = s;
	}

	public String getParagraphEnd() {
		return paragraphEnd;
	}

	public void setParagraphEnd(String s) {
		this.paragraphEnd = s;
	}

	public String getPageStart() {
		return pageStart;
	}

	public void setPageStart(String pageStart) {
		this.pageStart = pageStart;
	}

	public String getPageEnd() {
		return pageEnd;
	}

	public void setPageEnd(String pageEnd) {
		this.pageEnd = pageEnd;
	}

	/**
	 * This will get the page separator used to demark the boundary between
	 * pages.
	 * 
	 * @deprecated - not used in PDFTextStripper2. Use discrete
	 *             {@link #getPageStart()} and {@link #getPageEnd()} instead.
	 * @return The page separator string.
	 */
	public String getPageSeparator() {
		return getPageEnd() + getPageStart();
	}

	/**
	 * @deprecated - not used in PDFTextStripper2. Use discrete
	 *             {@link #setPageStart(String)} and {@link #setPageEnd(String)}
	 *             instead.
	 * @param separator
	 *            The desired page separator string.
	 */
	public void setPageSeparator(String separator) {
	}

	public String getArticleStart() {
		return articleStart;
	}

	public void setArticleStart(String articleStart) {
		this.articleStart = articleStart;
	}

	public String getArticleEnd() {
		return articleEnd;
	}

	public void setArticleEnd(String articleEnd) {
		this.articleEnd = articleEnd;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void startArticle(boolean isltr) throws IOException {
		output.write(getArticleStart());
	}

	/**
	 * {@inheritDoc}
	 */
	protected void endArticle() throws IOException {
		output.write(getArticleEnd());
	}

	/**
	 * {@inheritDoc}
	 */
	public PDFTextStripper2() throws IOException {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public PDFTextStripper2(String s) throws IOException {
		super(s);
	}

	/**
	 * {@inheritDoc}
	 */
	public PDFTextStripper2(Properties properties) throws IOException {
		super(properties);
	}

	/**
	 * handles the line separator for a new line given the specified current and
	 * previous TextPositions.
	 * 
	 * @param position
	 *            the current text position
	 * @param lastPosition
	 *            the previous text position
	 * @param lastLineStartPosition
	 *            the last text position that followed a line separator.
	 * @throws IOException
	 */
	protected PositionWrapper handleLineSeparation(PositionWrapper current,
			PositionWrapper lastPosition, PositionWrapper lastLineStartPosition)
			throws IOException {
		current.setLineStart();
		isParagraphSeparation(current, lastPosition, lastLineStartPosition);
		lastLineStartPosition = current;
		if (current.isParagraphStart()) {
			if (lastPosition.isArticleStart()) {
				writeParagraphStart();
			} else {
				writeLineSeparator();
				writeParagraphSeparator();
			}
		} else {
			writeLineSeparator();
		}
		return lastLineStartPosition;
	}

	/**
	 * tests the relationship between the last text position, the current text
	 * position and the last text position that followed a line separator to
	 * decide if the gap represents a paragraph separation. This should
	 * <i>only</i> be called for consecutive text positions that first pass the
	 * line separation test.
	 * <p>
	 * This base implementation tests to see if the lastLineStartPosition is
	 * null OR if the current vertical position has dropped below the last text
	 * vertical position by at least 2.5 times the current text height OR if the
	 * current horizontal position is indented by at least 2 times the current
	 * width of a space character.
	 * </p>
	 * <p>
	 * This also attempts to identify text that is indented under a hanging
	 * indent.
	 * </p>
	 * <p>
	 * This method sets the isParagraphStart and isHangingIndent flags on the
	 * current position object.
	 * </p>
	 * 
	 * @param position
	 *            the current text position. This may have its isParagraphStart
	 *            or isHangingIndent flags set upon return.
	 * @param lastPosition
	 *            the previous text position (should not be null).
	 * @param lastLineStartPosition
	 *            the last text position that followed a line separator. May be
	 *            null.
	 */
	protected void isParagraphSeparation(PositionWrapper position,
			PositionWrapper lastPosition, PositionWrapper lastLineStartPosition) {
		boolean result = false;
		if (lastLineStartPosition == null) {
			result = true;
		} else {
			float yGap = Math.abs(position.getTextPosition().getYDirAdj()
					- lastPosition.getTextPosition().getYDirAdj());
			float xGap = (position.getTextPosition().getXDirAdj() - lastLineStartPosition
					.getTextPosition().getXDirAdj());// do we need to flip this
														// for rtl?
			if (yGap > (getDropThreshold() * position.getTextPosition()
					.getHeightDir())) {
				result = true;
			} else if (xGap > (getIndentThreshold() * position
					.getTextPosition().getWidthOfSpace())) {
				// text is indented, but try to screen for hanging indent
				if (!lastLineStartPosition.isParagraphStart()) {
					result = true;
				} else {
					position.setHangingIndent();
				}
			} else if (xGap < -position.getTextPosition().getWidthOfSpace()) {
				// text is left of previous line. Was it a hanging indent?
				if (!lastLineStartPosition.isParagraphStart()) {
					result = true;
				}
			} else if (Math.abs(xGap) < (0.25 * position.getTextPosition()
					.getWidth())) {
				// current horizontal position is within 1/4 a char of the last
				// linestart. We'll treat them as lined up.
				if (lastLineStartPosition.isHangingIndent()) {
					position.setHangingIndent();
				} else if (lastLineStartPosition.isParagraphStart()) {
					// check to see if the previous line looks like
					// any of a number of standard list item formats
					Pattern liPattern = matchListItemPattern(lastLineStartPosition);
					if (liPattern != null) {
						Pattern currentPattern = matchListItemPattern(position);
						if (liPattern == currentPattern) {
							result = true;
						}
					}
				}
			}
		}
		if (result) {
			position.setParagraphStart();
		}
	}

	/**
	 * returns the list item Pattern object that matches the text at the
	 * specified PositionWrapper or null if the text does not match such a
	 * pattern. The list of Patterns tested against is given by the
	 * {@link #getListItemPatterns()} method. To add to the list, simply
	 * override that method (if sub-classing) or explicitly supply your own list
	 * using {@link #setListItemPatterns(List)}.
	 * 
	 * @param pw
	 * @return
	 */
	protected Pattern matchListItemPattern(PositionWrapper pw) {
		TextPosition tp = pw.getTextPosition();
		String txt = tp.getCharacter();
		Pattern p = matchPattern(txt, getListItemPatterns());
		return p;
	}

	/**
	 * iterates over the specified list of Patterns until it finds one that
	 * matches the specified string. Then returns the Pattern.
	 * <p>
	 * Order of the supplied list of patterns is important as most common
	 * patterns should come first. Patterns should be strict in general, and all
	 * will be used with case sensitivity on.
	 * </p>
	 * 
	 * @param s
	 * @param patterns
	 * @return
	 */
	protected static final Pattern matchPattern(String s, List<Pattern> patterns) {
		Pattern matchedPattern = null;
		for (Pattern p : patterns) {
			if (p.matcher(s).matches()) {
				return p;
			}
		}
		return matchedPattern;
	}

	private List<Pattern> liPatterns = null;

	/**
	 * a list of regular expressions that match commonly used list item formats,
	 * i.e. bullets, numbers, letters, Roman numerals, etc. Not meant to be
	 * comprehensive.
	 */
	public static final String[] LIST_ITEM_EXPRESSIONS = { "\\.", "\\d+\\.",
			"\\[\\d+\\]", "\\d+\\)", "[A-Z]\\.", "[a-z]\\.", "[A-Z]\\)",
			"[a-z]\\)", "[IVXL]+\\.", "[ivxl]+\\.",

	};

	/**
	 * returns a list of regular expression Patterns representing different
	 * common list item formats. For example numbered items of form:
	 * <ol>
	 * <li>some text</li>
	 * <li>more text</li>
	 * </ol>
	 * or
	 * <ul>
	 * <li>some text</li>
	 * <li>more text</li>
	 * </ul>
	 * etc., all begin with some character pattern. The pattern "\\d+\."
	 * (matches "1.", "2.", ...) or "\[\\d+\]" (matches "[1]", "[2]", ...).
	 * <p>
	 * This method returns a list of such regular expression Patterns.
	 * 
	 * @return a list of Pattern objects.
	 */
	protected List<Pattern> getListItemPatterns() {
		if (liPatterns == null) {
			liPatterns = new ArrayList<Pattern>();
			for (String expression : LIST_ITEM_EXPRESSIONS) {
				Pattern p = Pattern.compile(expression);
				liPatterns.add(p);
			}
		}
		return liPatterns;
	}

	/**
	 * use to supply a different set of regular expression patterns for matching
	 * list item starts.
	 * 
	 * @param patterns
	 */
	protected void setListItemPatterns(List<Pattern> patterns) {
		liPatterns = patterns;
	}

	/**
	 * writes the paragraph separator string to the output.
	 * 
	 * @throws IOException
	 */
	protected void writeParagraphSeparator() throws IOException {
		writeParagraphEnd();
		writeParagraphStart();
	}

	protected void writeParagraphStart() throws IOException {
		output.write(getParagraphStart());
	}

	protected void writeParagraphEnd() throws IOException {
		output.write(getParagraphEnd());
	}

	protected void writePageStart() throws IOException {
		output.write(getPageStart());
	}

	protected void writePageEnd() throws IOException {
		output.write(getPageEnd());
	}

	/**
	 * The normalizer is used to remove text ligatures/presentation forms and to
	 * correct the direction of right to left text, such as Arabic and Hebrew.
	 * <p>
	 * NOTE - this field duplicates the functionality of the private field by
	 * the same name in the parent class. Could be eliminated with a couple of
	 * minor mods of the parent.
	 * </p>
	 */
	private TextNormalize normalize = new TextNormalize(this.outputEncoding);

	/**
	 * calculates the vertical overlap of the two specified vertical
	 * positions+height pairs.
	 * <p>
	 * NOTE - this Duplicates functionality of a private method by the same name
	 * in the parent class.
	 * </p>
	 * 
	 * @param y1
	 * @param height1
	 * @param y2
	 * @param height2
	 * @return
	 */
	protected final boolean overlap(float y1, float height1, float y2,
			float height2) {
		return within(y1, y2, .1f) || (y2 <= y1 && y2 >= y1 - height1)
				|| (y1 <= y2 && y1 >= y2 - height2);
	}

	/**
	 * This will determine of two floating point numbers are within a specified
	 * variance.
	 * <p>
	 * NOTE - this Duplicates functionality of a private method by the same name
	 * in the parent class.
	 * </p>
	 * 
	 * @param first
	 *            The first number to compare to.
	 * @param second
	 *            The second number to compare to.
	 * @param variance
	 *            The allowed variance.
	 */
	protected boolean within(float first, float second, float variance) {
		return second > first - variance && second < first + variance;
	}

	private static final float ENDOFLASTTEXTX_RESET_VALUE = -1;
	private static final float MAXYFORLINE_RESET_VALUE = -Float.MAX_VALUE;
	private static final float EXPECTEDSTARTOFNEXTWORDX_RESET_VALUE = -Float.MAX_VALUE;
	private static final float MAXHEIGHTFORLINE_RESET_VALUE = -1;
	private static final float MINYTOPFORLINE_RESET_VALUE = Float.MAX_VALUE;
	private static final float LASTWORDSPACING_RESET_VALUE = -1;

	/**
	 * This will print the text of the processed page to "output". It will
	 * estimate, based on the coordinates of the text, where newlines and word
	 * spacings should be placed. The text will be sorted only if that feature
	 * was enabled.
	 * <p>
	 * NOTE - this overrides the parent class'
	 * {@link PDFTextStripper#writePage() writePage} method. It unfortunately
	 * copies in much of the parent code, with only minor mods to account for
	 * visibility. Functionally, the main difference is the replacement of the
	 * call to {@link #writeLineSeparator()} with a call to
	 * {@link #writeLineSeparator(TextPosition, TextPosition)} instead as well
	 * as tracking of a some state information during the parse. Copying the
	 * entire method would not be necessary with only a few mods to the parent
	 * class.
	 * </p>
	 * 
	 * @throws IOException
	 *             If there is an error writing the text.
	 */
	protected void writePage() throws IOException {
		float maxYForLine = MAXYFORLINE_RESET_VALUE;
		float minYTopForLine = MINYTOPFORLINE_RESET_VALUE;
		float endOfLastTextX = ENDOFLASTTEXTX_RESET_VALUE;
		float lastWordSpacing = LASTWORDSPACING_RESET_VALUE;
		float maxHeightForLine = MAXHEIGHTFORLINE_RESET_VALUE;
		PositionWrapper lastPosition = null;
		PositionWrapper lastLineStartPosition = null;

		boolean startOfPage = true;// flag to indicate start of page
		boolean startOfArticle = true;
		if (charactersByArticle.size() > 0)
			writePageStart();

		for (int i = 0; i < charactersByArticle.size(); i++) {
			List textList = (List) charactersByArticle.get(i);
			if (shouldSortByPosition()) {
				TextPositionComparator comparator = new TextPositionComparator();
				Collections.sort(textList, comparator);
			}

			Iterator textIter = textList.iterator();

			/*
			 * Before we can display the text, we need to do some normalizing.
			 * Arabic and Hebrew text is right to left and is typically stored
			 * in its logical format, which means that the rightmost character
			 * is stored first, followed by the second character from the right
			 * etc. However, PDF stores the text in presentation form, which is
			 * left to right. We need to do some normalization to convert the
			 * PDF data to the proper logical output format.
			 * 
			 * Note that if we did not sort the text, then the output of
			 * reversing the text is undefined and can sometimes produce worse
			 * output then not trying to reverse the order. Sorting should be
			 * done for these languages.
			 */

			/*
			 * First step is to determine if we have any right to left text, and
			 * if so, is it dominant.
			 */
			int ltrCnt = 0;
			int rtlCnt = 0;

			while (textIter.hasNext()) {
				TextPosition position = (TextPosition) textIter.next();
				String stringValue = position.getCharacter();
				for (int a = 0; a < stringValue.length(); a++) {
					byte dir = Character.getDirectionality(stringValue
							.charAt(a));
					if ((dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT)
							|| (dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING)
							|| (dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE)) {
						ltrCnt++;
					} else if ((dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT)
							|| (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC)
							|| (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING)
							|| (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE)) {
						rtlCnt++;
					}
				}
			}

			// choose the dominant direction
			boolean isRtlDominant = false;
			if (rtlCnt > ltrCnt) {
				isRtlDominant = true;
			}

			startArticle(!isRtlDominant);
			startOfArticle = true;
			// we will later use this to skip reordering
			boolean hasRtl = false;
			if (rtlCnt > 0) {
				hasRtl = true;
			}

			/*
			 * Now cycle through to print the text. We queue up a line at a time
			 * before we print so that we can convert the line from presentation
			 * form to logical form (if needed).
			 */
			// String lineStr = "";
			List<TextPosition> line = new ArrayList<TextPosition>();

			textIter = textList.iterator(); // start from the beginning again
			/*
			 * PDF files don't always store spaces. We will need to guess where
			 * we should add spaces based on the distances between
			 * TextPositions. Historically, this was done based on the size of
			 * the space character provided by the font. In general, this worked
			 * but there were cases where it did not work. Calculating the
			 * average character width and using that as a metric works better
			 * in some cases but fails in some cases where the spacing worked.
			 * So we use both. NOTE: Adobe reader also fails on some of these
			 * examples.
			 */
			// Keeps track of the previous average character width
			float previousAveCharWidth = -1;
			while (textIter.hasNext()) {
				TextPosition position = (TextPosition) textIter.next();
				PositionWrapper current = new PositionWrapper(position);
				String characterValue = position.getCharacter();

				// Resets the average character width when we see a change in
				// font
				// or a change in the font size
				if (lastPosition != null
						&& ((position.getFont() != lastPosition
								.getTextPosition().getFont()) || (position
								.getFontSize() != lastPosition
								.getTextPosition().getFontSize()))) {
					previousAveCharWidth = -1;
				}

				float positionX;
				float positionY;
				float positionWidth;
				float positionHeight;

				/*
				 * If we are sorting, then we need to use the text direction
				 * adjusted coordinates, because they were used in the sorting.
				 */
				if (shouldSortByPosition()) {
					positionX = position.getXDirAdj();
					positionY = position.getYDirAdj();
					positionWidth = position.getWidthDirAdj();
					positionHeight = position.getHeightDir();
				} else {
					positionX = position.getX();
					positionY = position.getY();
					positionWidth = position.getWidth();
					positionHeight = position.getHeight();
				}

				// The current amount of characters in a word
				int wordCharCount = position.getIndividualWidths().length;

				/*
				 * Estimate the expected width of the space based on the space
				 * character with some margin.
				 */
				float wordSpacing = position.getWidthOfSpace();
				float deltaSpace = 0;
				if ((wordSpacing == 0) || (wordSpacing == Float.NaN)) {
					deltaSpace = Float.MAX_VALUE;
				} else {
					if (lastWordSpacing < 0) {
						deltaSpace = (wordSpacing * getSpacingTolerance());
					} else {
						deltaSpace = (((wordSpacing + lastWordSpacing) / 2f) * getSpacingTolerance());
					}
				}

				/*
				 * Estimate the expected width of the space based on the average
				 * character width with some margin. This calculation does not
				 * make a true average (average of averages) but we found that
				 * it gave the best results after numerous experiments. Based on
				 * experiments we also found that .3 worked well.
				 */
				float averageCharWidth = -1;
				if (previousAveCharWidth < 0) {
					averageCharWidth = (positionWidth / wordCharCount);
				} else {
					averageCharWidth = (previousAveCharWidth + (positionWidth / wordCharCount)) / 2f;
				}
				float deltaCharWidth = (averageCharWidth * getAverageCharTolerance());

				// Compares the values obtained by the average method and the
				// wordSpacing method and picks
				// the smaller number.
				float expectedStartOfNextWordX = EXPECTEDSTARTOFNEXTWORDX_RESET_VALUE;
				if (endOfLastTextX != ENDOFLASTTEXTX_RESET_VALUE) {
					if (deltaCharWidth > deltaSpace) {
						expectedStartOfNextWordX = endOfLastTextX + deltaSpace;
					} else {
						expectedStartOfNextWordX = endOfLastTextX
								+ deltaCharWidth;
					}
				}

				// System.err.println(position.getCharacter()+"\t"+position.getX()+"\t"+position.getY()+" \t"+position.getXDirAdj()+"\t"+position.getYDirAdj()+"\t"+position.getFont());
				if (lastPosition != null) {
					if (startOfArticle) {
						lastPosition.setArticleStart();
						startOfArticle = false;
					}
					// RDD - Here we determine whether this text object is on
					// the current
					// line. We use the lastBaselineFontSize to handle the
					// superscript
					// case, and the size of the current font to handle the
					// subscript case.
					// Text must overlap with the last rendered baseline text by
					// at least
					// a small amount in order to be considered as being on the
					// same line.

					/*
					 * XXX BC: In theory, this check should really check if the
					 * next char is in full range seen in this line. This is
					 * what I tried to do with minYTopForLine, but this caused a
					 * lot of regression test failures. So, I'm leaving it be
					 * for now.
					 */
					if (!overlap(positionY, positionHeight, maxYForLine,
							maxHeightForLine)) {
						// // If we have RTL text on the page, change the
						// direction
						// if (hasRtl)
						// {
						// lineStr = normalize.makeLineLogicalOrder(lineStr,
						// isRtlDominant);
						// }
						//
						// /* normalize string to remove presentation forms.
						// * Note that this must come after the line direction
						// * conversion because the process looks ahead to the
						// next
						// * logical character.
						// */
						// lineStr = normalize.normalizePres(lineStr);
						//
						// //writeString(lineStr);
						line = normalize(line, isRtlDominant, outputEncoding);
						writeLine(line);
						line.clear();
						// lineStr = "";

						lastLineStartPosition = handleLineSeparation(current,
								lastPosition, lastLineStartPosition);

						endOfLastTextX = ENDOFLASTTEXTX_RESET_VALUE;
						expectedStartOfNextWordX = EXPECTEDSTARTOFNEXTWORDX_RESET_VALUE;
						maxYForLine = MAXYFORLINE_RESET_VALUE;
						maxHeightForLine = MAXHEIGHTFORLINE_RESET_VALUE;
						minYTopForLine = MINYTOPFORLINE_RESET_VALUE;
					}

					// Test if our TextPosition starts after a new word would be
					// expected to start.
					if (expectedStartOfNextWordX != EXPECTEDSTARTOFNEXTWORDX_RESET_VALUE
							&& expectedStartOfNextWordX < positionX
							&&
							// only bother adding a space if the last character
							// was not a space
							lastPosition.getTextPosition().getCharacter() != null
							&& !lastPosition.getTextPosition().getCharacter()
									.endsWith(" ")) {
						// lineStr += getWordSeparator();
						line.add(WordSeparator.getSeparator());
					}
				}

				if (positionY >= maxYForLine) {
					maxYForLine = positionY;
				}

				// RDD - endX is what PDF considers to be the x coordinate of
				// the
				// end position of the text. We use it in computing our metrics
				// below.
				endOfLastTextX = positionX + positionWidth;

				// add it to the list
				if (characterValue != null) {
					if (startOfPage && lastPosition == null) {
						writeParagraphStart();// not sure this is correct for
												// RTL?
					}
					// lineStr += characterValue;
					line.add(position);
				}
				maxHeightForLine = Math.max(maxHeightForLine, positionHeight);
				minYTopForLine = Math.min(minYTopForLine, positionY
						- positionHeight);
				lastPosition = current;
				if (startOfPage) {
					lastPosition.setParagraphStart();
					lastPosition.setLineStart();
					lastLineStartPosition = lastPosition;
					startOfPage = false;
				}
				lastWordSpacing = wordSpacing;
				previousAveCharWidth = averageCharWidth;
			}

			// print the final line
			// if (lineStr.length() > 0)
			if (line.size() > 0) {
				// if (hasRtl)
				// {
				// lineStr = normalize.makeLineLogicalOrder(lineStr,
				// isRtlDominant);
				// }
				//
				// // normalize string to remove presentation forms
				// lineStr = normalize.normalizePres(lineStr);
				//
				// //writeString(lineStr);
				line = normalize(line, isRtlDominant, outputEncoding);
				writeLine(line);
				writeParagraphEnd();
			}

			endArticle();
		}
		writePageEnd();
	}

	protected void writeLine(List<TextPosition> line) throws IOException {
		for (TextPosition text : line) {
			if (text instanceof WordSeparator) {
				writeWordSeparator();
			} else {
				writeCharacters(text);
			}
		}
	}

	protected List<TextPosition> normalize(List<TextPosition> line,
			boolean isRtlDominant, String outputEncoding) {
		LinkedList<TextPosition> normalized = new LinkedList<TextPosition>();
		if (isRtlDominant) {
			for (TextPosition text : line) {
				TextPosition tp = text instanceof WordSeparator ? text
						: new NormalizedTextPosition(text, isRtlDominant,
								outputEncoding);
				normalized.addFirst(tp);
			}
		} else {
			for (TextPosition text : line) {
				TextPosition tp = text instanceof WordSeparator ? text
						: new NormalizedTextPosition(text, isRtlDominant,
								outputEncoding);
				normalized.add(tp);
			}
		}
		return normalized;
	}

	/**
	 * internal marker class. Used as a place holder in a line of TextPositions.
	 * 
	 * @author ME21969
	 * 
	 */
	protected static final class WordSeparator extends TextPosition {
		private static final WordSeparator separator = new WordSeparator();

		private WordSeparator() {
		}

		public static final WordSeparator getSeparator() {
			return separator;
		}

	}

	protected static class NormalizedTextPosition extends WrappedTextPosition {
		protected String outputEncoding = null;
		/**
		 * The normalizer is used to remove text ligatures/presentation forms
		 * and to correct the direction of right to left text, such as Arabic
		 * and Hebrew.
		 */
		private static final Map<String, TextNormalize> normalizers = new HashMap<String, TextNormalize>();
		private boolean isRtlDominant = false;
		private String normalizedText = null;

		public NormalizedTextPosition(TextPosition src, boolean isRtlDominant,
				String outputEncoding) {
			super(src);
			this.outputEncoding = outputEncoding;
			this.isRtlDominant = isRtlDominant;
		}

		protected static final TextNormalize getNormalize(String outputEncoding) {
			if (normalizers.get(outputEncoding) == null) {
				normalizers.put(outputEncoding, new TextNormalize(
						outputEncoding));
			}
			return normalizers.get(outputEncoding);
		}

		/**
		 * returns the text of this TextPosition as a String, after first
		 * normalizing it in two ways.
		 * <ol>
		 * <li>if {@link #isRtlDominant()} is true, then reorders the text to
		 * logical ordering.</li>
		 * <li>normalizes for presentation - for example changing ligatures to
		 * plain-text equivalents.</li>
		 * </ol>
		 */
		public String getCharacter() {
			if (normalizedText == null) {
				normalizedText = src.getCharacter();
				if (isRtlDominant) {
					normalizedText = getNormalize(outputEncoding)
							.makeLineLogicalOrder(normalizedText, isRtlDominant);
				}
				normalizedText = getNormalize(outputEncoding).normalizePres(
						normalizedText);

			}
			return normalizedText;
		}

		public boolean isRtlDominant() {
			return isRtlDominant;
		}

	}

	protected static class WrappedTextPosition extends TextPosition {
		protected TextPosition src = null;

		public WrappedTextPosition(TextPosition src) {
			super();
			this.src = src;
		}

		public String getCharacter() {
			return src.getCharacter();
		}

		public Matrix getTextPos() {
			return src.getTextPos();
		}

		public float getDir() {
			return src.getDir();
		}

		public float getX() {
			return src.getX();
		}

		public float getXDirAdj() {
			return src.getXDirAdj();
		}

		public float getY() {
			return src.getY();
		}

		public float getYDirAdj() {
			return src.getYDirAdj();
		}

		public float getWidth() {
			return src.getWidth();
		}

		public float getWidthDirAdj() {
			return src.getWidthDirAdj();
		}

		public float getHeight() {
			return src.getHeight();
		}

		public float getHeightDir() {
			return src.getHeightDir();
		}

		public float getFontSize() {
			return src.getFontSize();
		}

		public float getFontSizeInPt() {
			return src.getFontSizeInPt();
		}

		public PDFont getFont() {
			return src.getFont();
		}

		public float getWordSpacing() {
			return src.getWordSpacing();
		}

		public float getWidthOfSpace() {
			return src.getWidthOfSpace();
		}

		public float getXScale() {
			return src.getXScale();
		}

		public float getYScale() {
			return src.getYScale();
		}

		public float[] getIndividualWidths() {
			return src.getIndividualWidths();
		}

		public String toString() {
			return src.toString();
		}

		public boolean contains(TextPosition tp2) {
			return src.contains(tp2);
		}

		public void mergeDiacritic(TextPosition diacritic,
				TextNormalize normalize) {
			src.mergeDiacritic(diacritic, normalize);
		}

		public boolean isDiacritic() {
			return src.isDiacritic();
		}
	}
}