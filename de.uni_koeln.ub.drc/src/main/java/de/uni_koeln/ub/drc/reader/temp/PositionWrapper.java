package de.uni_koeln.ub.drc.reader.temp;

import org.apache.pdfbox.util.TextPosition;

/**
 * wrapper of TextPosition that adds flags to track status as linestart and
 * paragraph start positions.
 * <p>
 * This is implemented as a wrapper since the TextPosition class doesn't provide
 * complete access to its state fields to subclasses. Also, conceptually
 * TextPosition is immutable while these flags need to be set post-creation so
 * it makes sense to put these flags in this separate class.
 * </p>
 * 
 * @author m.martinez@ll.mit.edu
 * 
 */
/* Third-party code */@SuppressWarnings("all")
public class PositionWrapper {

	private boolean isLineStart = false;
	private boolean isParagraphStart = false;
	private boolean isPageBreak = false;
	private boolean isHangingIndent = false;
	private boolean isArticleStart = false;

	private TextPosition position = null;

	/**
	 * returns the underlying TextPosition object
	 * 
	 * @return
	 */
	public TextPosition getTextPosition() {
		return position;
	}

	public boolean isLineStart() {
		return isLineStart;
	}

	/**
	 * sets the isLineStart() flag to true
	 */
	public void setLineStart() {
		this.isLineStart = true;
	}

	public boolean isParagraphStart() {
		return isParagraphStart;
	}

	/**
	 * sets the isParagraphStart() flag to true.
	 */
	public void setParagraphStart() {
		this.isParagraphStart = true;
	}

	public boolean isArticleStart() {
		return isArticleStart;
	}

	/**
	 * sets the isArticleStart() flag to true.
	 */
	public void setArticleStart() {
		this.isArticleStart = true;
	}

	public boolean isPageBreak() {
		return isPageBreak;
	}

	/**
	 * sets the isPageBreak() flag to true
	 */
	public void setPageBreak() {
		this.isPageBreak = isPageBreak;
	}

	public boolean isHangingIndent() {
		return isHangingIndent;
	}

	/**
	 * sets the isHangingIndent() flag to true
	 */
	public void setHangingIndent() {
		this.isHangingIndent = isHangingIndent;
	}

	/**
	 * constructs a PositionWrapper around the specified TextPosition object.
	 * 
	 * @param position
	 */
	public PositionWrapper(TextPosition position) {
		this.position = position;
	}

}
