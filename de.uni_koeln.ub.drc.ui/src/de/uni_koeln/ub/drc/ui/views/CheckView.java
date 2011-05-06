/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.uni_koeln.ub.drc.data.Box;
import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.Messages;

/**
 * View containing the scanned page used to check the original word while
 * editing.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public final class CheckView {
	private Composite parent;
	private Label imageLabel;
	private boolean imageLoaded = false;
	private ScrolledComposite scrolledComposite;
	private ImageData imageData;
	private Label suggestions;
	private Job job;
	private Button check;
	private Text word;
	private List<Button> suggestionButtons = new ArrayList<Button>();
	private Composite bottom;
  private double scaleWidthFactor = 1;
  private double scaleHeightFactor = 1;
  private Page page;

	@Inject
	public CheckView(final Composite parent) {
		this.parent = parent;
		scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER);
		imageLabel = new Label(scrolledComposite, SWT.BORDER | SWT.CENTER);
		scrolledComposite.setContent(imageLabel);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		addSuggestions();
		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	@Inject
	public void setSelection(
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) final List<Page> pages) {
		if (pages != null && pages.size() > 0 && (page == null || !page.equals(pages.get(0)))) {
			page = pages.get(0);
			try {
				updateImage(page);
			} catch (MalformedURLException e) {
				handle(e);
			} catch (IOException e) {
				handle(e);
			}
		} else {
			return;
		}
	}

	@Inject
	public void setSelection(
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) final Text text) {
		Word word = null;
		if (imageLoaded && text != null
				&& (word = (Word) text.getData(Word.class.toString())) != null) {
			this.word = text;
			markPosition(text);
			if (job != null) {
				/*
				 * If a word is selected while we had a Job running for the
				 * previous word, cancel that:
				 */
				job.cancel();
			}
			if (text == null) {
				suggestions.setText(Messages.NoWordSelected);
			} else if (!check.getSelection()) {
				suggestions.setText(Messages.EditSuggestionsDisabled);
				disposeButtons();
			} else if (word.isLocked()) {
				suggestions.setText(Messages.NoEditSuggestionsWordIsLocked);
			} else {
				findEditSuggestions(word, text);
				job.setPriority(Job.DECORATE);
				job.schedule();
			}
		}
	}

	private void addSuggestions() {
		bottom = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(7, false);
		bottom.setLayout(layout);
		check = new Button(bottom, SWT.CHECK);
		check.setToolTipText(Messages.SuggestCorrections);
		check.setSelection(false);
		check.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setSelection(word);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		suggestions = new Label(bottom, SWT.WRAP);
		suggestions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void clearMarker() {
		imageLabel.setImage(reloadImage());
	}

	private void displaySuggestionButtons(final Word word, final Text text) {
		bottom.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				String words = word.suggestions().mkString(" "); //$NON-NLS-1$
				for (String string : words.split(" ")) { //$NON-NLS-1$
					final Button b = new Button(bottom, SWT.FLAT);
					b.setLayoutData(new GridData(SWT.NONE));
					b.setText(string);
					b.addSelectionListener(new SelectionListener() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							if (text != null) {
								text.setText(b.getText());
								text.setSelection(text.getCaretPosition());
								text.setFocus();
							}
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
					suggestionButtons.add(b);
				}
				bottom.pack();
				bottom.redraw();
			}
		});
	}

	private void disposeButtons() {
		if (!suggestionButtons.isEmpty()) {
			for (Button b : suggestionButtons) {
				b.dispose();
			}
		}
		suggestionButtons = new ArrayList<Button>();
	}

	private void drawBoxArea(final Rectangle rect, final GC gc) {
		gc.setAlpha(50);
		gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		gc.fillRectangle(rect);
	}

	private void drawBoxBorder(final Rectangle rect, final GC gc) {
		gc.setAlpha(200);
		gc.setLineWidth(1);
		gc.setForeground(parent.getDisplay().getSystemColor(
				SWT.COLOR_DARK_GREEN));
		gc.drawRectangle(rect);
	}

	private void findEditSuggestions(final Word word, final Text text) {
		disposeButtons();
		suggestions.setText(Messages.FindingEditSuggestions);
		job = new Job(Messages.EditSuggestionsSearchJob) {
			protected IStatus run(final IProgressMonitor monitor) {
				final boolean complete = word.prepSuggestions();
				suggestions.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!complete) {
							suggestions
									.setText(Messages.FindingEditSuggestions);
						} else {
							String info = (word.suggestions().size() == 0) ? Messages.NoReasonableEditSuggestionsFound
									: String.format(Messages.SuggestionsFor
											+ " %s (" + Messages.Originally
											+ " '%s'):", word.history().top()
											.form(), word.original());

							if (!bottom.isDisposed())
								displaySuggestionButtons(word, text);

							if (!suggestions.isDisposed())
								suggestions.setText(info);
						}
					}
				});
				return Status.OK_STATUS;
			}

			@Override
			protected void canceling() {
				word.cancelled_$eq(true);
				suggestions.setText(Messages.FindingEditSuggestions);
			};
		};
	}

	private void handle(Exception e) {
		MessageDialog.openError(parent.getShell(), Messages.CouldNotLoadScan,
				Messages.CouldNotLoadImageForCurrentPage);
		e.printStackTrace();
	}
	
	private Image loadImage(final Page page) throws IOException {
		Display display = parent.getDisplay();
		// TODO image as lazy def in page, fetched on demand?
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(Index.loadImageFor(
		    DrcUiActivator.instance().currentUser().collection(),
				DrcUiActivator.instance().db(), page)));
		// imageData = convertToImageData(scale(in)); // TODO enable for optional scaling
		imageData = new ImageData(in);
		Image newImage = new Image(display, imageData);
		return newImage;
	}

	@SuppressWarnings( "unused" ) // TODO add as option in UI
	private ImageData convertToImageData(BufferedImage bufferedImage)
			throws IOException {
	  ByteArrayOutputStream out = new ByteArrayOutputStream();
	  ImageIO.write(bufferedImage, "png", new BufferedOutputStream(out)); //$NON-NLS-1$
	  BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
		ImageData data = new ImageData(in);
		in.close();
		return data;
	}

	@SuppressWarnings( "unused" ) // TODO add as option in UI
  private BufferedImage scale(InputStream in) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(in);
		int height = scrolledComposite.getMinHeight();
		scaleWidthFactor = ((double) height / bufferedImage.getHeight());
		int scaledWidth = (int) (scaleWidthFactor * bufferedImage.getWidth());
		scaleHeightFactor = ((double) scaledWidth / bufferedImage.getWidth());
		java.awt.Image img = bufferedImage.getScaledInstance(scaledWidth,
				height, BufferedImage.SCALE_AREA_AVERAGING);
		BufferedImage scaledBufferedImage = new BufferedImage(
				img.getWidth(null), img.getHeight(null),
				BufferedImage.TYPE_INT_RGB);
		Graphics graphics = scaledBufferedImage.getGraphics();
		graphics.drawImage(img, 0, 0, null);
		graphics.dispose();
		return scaledBufferedImage;
	}

	private void markPosition(final Text text) {
		imageLabel.getImage().dispose();
		Word word = (Word) text.getData(Word.class.toString());
		Box box = word.position();
		Image image = reloadImage();
		Rectangle rect = getScaledRect(box);
		GC gc = new GC(image);
		drawBoxArea(rect, gc);
		drawBoxBorder(rect, gc);
		gc.dispose();
		imageLabel.setImage(image);
		scrolledComposite.setOrigin(new Point(rect.x - 15, rect.y - 25)); // IMG_SIZE
	}

	private Rectangle getScaledRect(Box box) {
		int startX = (int) ((scaleWidthFactor * box.x()) - 15);
		int startY = (int) ((scaleHeightFactor * box.y()) - 6);
		int boxWidth = (int) ((scaleWidthFactor * box.width()) + 25);
		int boxHeight = (int) ((scaleHeightFactor * box.height()) + 18);
		Rectangle rect = new Rectangle(startX, startY, boxWidth, boxHeight);
		return rect;
	}

	private Image reloadImage() {
		Display display = parent.getDisplay();
		Image newImage = new Image(display, imageData);
		return newImage;
	}

	private void updateImage(final Page page) throws IOException {
		if (imageLabel != null && imageLabel.getImage() != null
				&& !imageLabel.getImage().isDisposed())
			imageLabel.getImage().dispose();
		Image loadedImage = loadImage(page);
		imageData = loadedImage.getImageData();
		imageLabel.setImage(loadedImage);
		imageLoaded = true;
    scrolledComposite.setMinSize(scrolledComposite.getContent().computeSize(SWT.DEFAULT,
        SWT.DEFAULT, true));
    scrolledComposite.layout(true, true);
	}

}
