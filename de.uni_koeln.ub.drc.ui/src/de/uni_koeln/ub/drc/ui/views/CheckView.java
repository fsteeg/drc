/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
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
	private Page page;
	private Composite zoomBottom;
	private Scale scale;
	private float scaleFactor = 1;
	private Image cachedImage;
	private int originalHeight;
	private int originalWidth;

	/**
	 * @param parent
	 *            The parent composite for this part
	 */
	@Inject
	public CheckView(final Composite parent) {
		this.parent = parent;
		scale();
		scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER);
		imageLabel = new Label(scrolledComposite, SWT.BORDER | SWT.CENTER);
		scrolledComposite.setContent(imageLabel);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		addSuggestions();
		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	/**
	 * @param pages
	 *            The selected pages
	 */
	@Inject
	public void setSelection(
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) final List<Page> pages) {
		if (pages != null && pages.size() > 0
				&& (page == null || !page.equals(pages.get(0)))) {
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

	/**
	 * @param text
	 *            The active text widget
	 */
	@Inject
	public void setSelection(
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) final Text text) {
		Word word = null;
		if (imageLoaded && text != null && !text.isDisposed()
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
			if (!check.getSelection()) {
				suggestions.setText(Messages.EditSuggestionsDisabled);
				disposeButtons();
			} else if (word.isLocked()) {
				suggestions.setText(Messages.NoEditSuggestionsWordIsLocked);
			} else {
				findEditSuggestions(word, text);
				job.setPriority(Job.DECORATE);
				job.schedule();
			}
		} else if (text == null) {
			suggestions.setText(Messages.NoWordSelected);
		}
	}

	private void scale() {
		zoomBottom = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		zoomBottom.setLayout(layout);
		Label zoomLabel = new Label(zoomBottom, SWT.NONE);
		zoomLabel.setText(Messages.Zoom);
		Label downScaleLabel = new Label(zoomBottom, SWT.NONE);
		downScaleLabel.setText(Messages.Minus);
		scale = new Scale(zoomBottom, SWT.NONE);
		Label upScaleLabel = new Label(zoomBottom, SWT.NONE);
		upScaleLabel.setText(Messages.Plus);
		Rectangle clientArea = zoomBottom.getClientArea();
		scale.setBounds(clientArea.x, clientArea.y, 200, 64);
		scale.setOrientation(SWT.LEFT_TO_RIGHT);
		scale.setMinimum(30);
		scale.setMaximum(100);
		scale.setSelection(100);
		scale.setToolTipText(Messages.ZoomToolTip);
		addListener();
	}

	private void addListener() {
		scale.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent event) {
				scaleFactor = scale.getSelection() / 100F;
				if (word != null)
					try {
						updateImage(page);
						markPosition(word);
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
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
		gc.setLineWidth(2);
		gc.setForeground(parent.getDisplay().getSystemColor(
				SWT.COLOR_DARK_GREEN));
		gc.drawRectangle(rect);
	}

	private void findEditSuggestions(final Word word, final Text text) {
		disposeButtons();
		suggestions.setText(Messages.FindingEditSuggestions);
		job = new Job(Messages.EditSuggestionsSearchJob) {
			@Override
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
											+ " %s (" + Messages.Originally //$NON-NLS-1$
											+ " '%s'):", word.history().top() //$NON-NLS-1$
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
		final InputStream in = getInputStream(page);
		imageData = new ImageData(in);
		Image newImage = new Image(parent.getDisplay(), imageData);
		return newImage;
	}

	private void markPosition(final Text text) {
		if (imageLabel.getImage() != null) {
			imageLabel.getImage().dispose();

			Word word = (Word) text.getData(Word.class.toString());
			Box box = word.position();
			Rectangle rect = getScaledRect(box);

			Image displayedImage = new Image(parent.getDisplay(),
					cachedImage.getImageData());

			GC gc = new GC(displayedImage);
			drawBoxArea(rect, gc);
			drawBoxBorder(rect, gc);
			gc.dispose();

			imageLabel.setImage(displayedImage);
			Point p = newOrigin(box, originalHeight, originalWidth,
					displayedImage);
			scrolledComposite.setOrigin(p);
		}
	}

	private void cacheImage(final Image img) {
		if (cachedImage != null)
			cachedImage.dispose();
		cachedImage = new Image(parent.getDisplay(), new Rectangle(
				img.getBounds().x, img.getBounds().y, img.getBounds().width,
				img.getBounds().height));
		GC gc = new GC(cachedImage);
		gc.drawImage(img, 0, 0);
		gc.dispose();
		if (scaleFactor < 1) {
			cachedImage = scaleImage(cachedImage);
		}
		originalHeight = cachedImage.getBounds().height;
		originalWidth = cachedImage.getBounds().width;
	}

	private Point newOrigin(Box box, int oldHeigt, int oldWidth, Image newImage) {
		int height = newImage.getBounds().height;
		int width = newImage.getBounds().width;
		int x = ((oldWidth - width) / 2) + (int) (box.x() * scaleFactor);
		int y = ((oldHeigt - height) / 2) + (int) (box.y() * scaleFactor);
		return new Point(x - 25, y - 25);
	}

	private Image scaleImage(final Image image) {
		Rectangle rect = image.getBounds();
		ImageData data = image.getImageData().scaledTo(
				(int) (rect.width * scaleFactor),
				(int) (rect.height * scaleFactor));
		image.dispose();
		return new Image(parent.getDisplay(), data);
	}

	private Rectangle getScaledRect(Box box) {
		int startX = (int) ((scaleFactor * box.x()) - (15 * scaleFactor));
		int startY = (int) ((scaleFactor * box.y()) - (6 * scaleFactor));
		int boxWidth = (int) ((scaleFactor * box.width()) + (25 * scaleFactor));
		int boxHeight = (int) ((scaleFactor * box.height()) + (18 * scaleFactor));
		return new Rectangle(startX, startY, boxWidth, boxHeight);
	}

	private void updateImage(final Page page) throws IOException {
		if (imageLabel != null && imageLabel.getImage() != null
				&& !imageLabel.getImage().isDisposed())
			imageLabel.getImage().dispose();
		cacheImage(loadImage(page));
		Image dispayedImage = new Image(parent.getDisplay(),
				cachedImage.getImageData());
		imageData = dispayedImage.getImageData();
		if (imageLabel != null)
			imageLabel.setImage(dispayedImage);
		imageLoaded = true;
		scrolledComposite.setMinSize(scrolledComposite.getContent()
				.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		scrolledComposite.layout(true, true);
	}

	private InputStream getInputStream(final Page page) {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(
				Index.loadImageFor(DrcUiActivator.instance().currentUser()
						.collection(), DrcUiActivator.instance().db(), page)));
		return in;
	}

}
