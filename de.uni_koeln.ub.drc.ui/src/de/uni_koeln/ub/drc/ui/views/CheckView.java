/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

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
 * @author Mihail Atanassov (matana)
 */
public class CheckView extends ViewPart {

	/**
	 * The class / CheckView ID
	 */
	public static final String ID = CheckView.class.getName().toLowerCase();

	private Composite bottom;
	private Canvas canvas;
	private Button check;
	private Display display;
	private Image img;
	private Job job;
	private Composite parent;
	private Scale scale;
	private float scaleFactor = 1.0f;
	private ScrolledComposite scrolledComposite;
	private Label suggestions;
	private List<Button> suggestionButtons = new ArrayList<Button>();
	private Text text;
	private Word word;
	private Composite zoomBottom;

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		display = parent.getDisplay();
		scale();
		addListenerToScaleWidget();
		scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER);
		canvas = new Canvas(scrolledComposite, SWT.CENTER);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setContent(canvas);
		GridLayoutFactory.fillDefaults().generateLayout(parent);
		addSuggestions();
		attachSelectionListener();
		addListenerToCanvas();
	}

	@Override
	public void setFocus() {
	}

	/**
	 * @param word
	 *            The current word
	 * @param text
	 *            The active text widget
	 */
	public void highlight(final Word word, final Text text) {
		System.out.println("selected word: " + word);
		this.word = word;
		this.text = text;
		canvas.redraw();
		doJob();

		// if (img != null) {
		// img.dispose();
		//
		// Box box = word.position();
		// Rectangle rect = getScaledRect(box);
		//
		// Image displayedImage = new Image(parent.getDisplay(),
		// cachedImage.getImageData());
		//
		// GC gc = new GC(displayedImage);
		// drawBoxArea(rect, gc);
		// drawBoxBorder(rect, gc);
		// gc.dispose();
		//
		// imageLabel.setImage(displayedImage);
		// Point p = newOrigin(box, originalHeight, originalWidth,
		// displayedImage);
		// scrolledComposite.setOrigin(p);
		// }
	}

	private void attachSelectionListener() {
		ISelectionService selectionService = (ISelectionService) getSite()
				.getService(ISelectionService.class);
		selectionService.addSelectionListener(new ISelectionListener() {

			private ImageData imageData;

			@SuppressWarnings("unchecked")
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				if (structuredSelection.getFirstElement() instanceof Page) {
					List<Page> pages = (List<Page>) structuredSelection
							.toList();
					if (pages != null && pages.size() > 0) {
						Page page = pages.get(0);
						updateImage(page);
						// scrolledComposite.redraw();
						// scrolledComposite.update();
					}
				}
			}

			private void updateImage(Page page) {
				img = loadCurrentImage(page);
			}

			private Image loadCurrentImage(Page page) {
				final InputStream in = getInputStream(page);
				imageData = new ImageData(in);
				Image newImage = new Image(parent.getDisplay(), imageData);
				return newImage;
			}

			private InputStream getInputStream(Page page) {
				InputStream in = new BufferedInputStream(
						new ByteArrayInputStream(Index.loadImageFor(
								DrcUiActivator.getDefault().currentUser()
										.collection(), DrcUiActivator
										.getDefault().db(), page)));
				return in;
			}
		});

	}

	private void addListenerToCanvas() {
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent event) {
				if (img != null && !img.isDisposed()) {
					GC gc = event.gc;
					Image copy = new Image(scrolledComposite.getDisplay(), img
							.getImageData());
					if (scaleFactor < 1)
						copy = scale(copy);
					gc.drawImage(copy, 0, 0);
					if (word != null) {
						Box box = word.position();
						Rectangle rect = getScaledRect(box);
						drawBoxArea(rect, gc);
						drawBoxBorder(rect, gc);
						Point p = newOrigin(box, copy.getBounds().height,
								copy.getBounds().width, copy);
						scrolledComposite.setOrigin(p);
					}
					gc.dispose();
					scrolledComposite.setMinSize(copy.getBounds().width,
							copy.getBounds().height);
					copy.dispose();
				}
			}

			private Point newOrigin(Box box, int oldHeigt, int oldWidth,
					Image newImage) {
				int height = newImage.getBounds().height;
				int width = newImage.getBounds().width;
				int x = ((oldWidth - width) / 2)
						+ (int) (box.x() * scaleFactor);
				int y = ((oldHeigt - height) / 2)
						+ (int) (box.y() * scaleFactor);
				return new Point(x - 25, y - 25);
			}
		});
	}

	private void addListenerToScaleWidget() {
		scale.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				scaleFactor = scale.getSelection() / 100F;
				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						canvas.redraw();
					}
				});
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void addSuggestions() {
		bottom = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(7, false);
		bottom.setLayout(layout);
		check = new Button(bottom, SWT.CHECK);
		check.setToolTipText(Messages.get().SuggestCorrections);
		check.setSelection(false);
		check.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				doJob();
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

	private void doJob() {
		if (job != null) {
			// If a word is selected while we had a Job running for the previous
			// word, cancel that:
			job.cancel();
		}
		if (!check.getSelection()) {
			suggestions.setText(Messages.get().EditSuggestionsDisabled);
			disposeButtons();
		} else if (word.isLocked()) {
			suggestions.setText(Messages.get().NoEditSuggestionsWordIsLocked);
		} else {
			findEditSuggestions(word, text);
			job.setPriority(Job.DECORATE);
			job.schedule();
		}
	}

	private void drawBoxArea(final Rectangle rect, final GC gc) {
		gc.setAlpha(50);
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
		gc.fillRectangle(rect);
	}

	private void drawBoxBorder(final Rectangle rect, final GC gc) {
		gc.setAlpha(200);
		gc.setLineWidth(2);
		gc.setForeground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GREEN));
		gc.drawRectangle(rect);
	}

	private void findEditSuggestions(final Word word, final Text text) {
		disposeButtons();
		suggestions.setText(Messages.get().FindingEditSuggestions);
		job = new Job(Messages.get().EditSuggestionsSearchJob) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final boolean complete = word.prepSuggestions();
				suggestions.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!complete) {
							suggestions.setText(Messages.get().FindingEditSuggestions);
						} else {
							String info = (word.suggestions().size() == 0) ? Messages
									.get().NoReasonableEditSuggestionsFound
									: String.format(
											Messages.get().SuggestionsFor
													+ " %s (" + Messages.get().Originally //$NON-NLS-1$
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
				suggestions.setText(Messages.get().FindingEditSuggestions);
			};
		};
	}

	private Rectangle getScaledRect(Box box) {
		int startX = (int) ((scaleFactor * box.x()) - (15 * scaleFactor));
		int startY = (int) ((scaleFactor * box.y()) - (6 * scaleFactor));
		int boxWidth = (int) ((scaleFactor * box.width()) + (25 * scaleFactor));
		int boxHeight = (int) ((scaleFactor * box.height()) + (18 * scaleFactor));
		return new Rectangle(startX, startY, boxWidth, boxHeight);
	}

	private void scale() {
		zoomBottom = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		zoomBottom.setLayout(layout);
		Label zoomLabel = new Label(zoomBottom, SWT.NONE);
		zoomLabel.setText(Messages.get().Zoom);
		Label downScaleLabel = new Label(zoomBottom, SWT.NONE);
		downScaleLabel.setText(Messages.get().Minus);
		scale = new Scale(zoomBottom, SWT.NONE);
		Label upScaleLabel = new Label(zoomBottom, SWT.NONE);
		upScaleLabel.setText(Messages.get().Plus);
		Rectangle clientArea = zoomBottom.getClientArea();
		scale.setBounds(clientArea.x, clientArea.y, 200, 64);
		scale.setOrientation(SWT.LEFT_TO_RIGHT);
		scale.setMinimum(30);
		scale.setMaximum(100);
		scale.setSelection(100);
	}

	private Image scale(final Image img) {
		Rectangle rect = img.getBounds();
		ImageData data = img.getImageData().scaledTo(
				(int) (rect.width * scaleFactor),
				(int) (rect.height * scaleFactor));
		img.dispose();
		Image toReturn = new Image(display, data);
		return toReturn;
	}

}
