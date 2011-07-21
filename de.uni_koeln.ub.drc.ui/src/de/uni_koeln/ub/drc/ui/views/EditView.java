/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import scala.collection.mutable.Stack;

import com.quui.sinist.XmlDb;

import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.Modification;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.User;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.Messages;
import de.uni_koeln.ub.drc.util.PlainTextCopy;

/**
 * A view that the area to edit the text. Marks the section in the image file
 * that corresponds to the word in focus (in {@link CheckView}).
 * 
 * @author Fabian Steeg (fsteeg)
 */
public final class EditView {

	@Inject
	IEclipseContext context;
	@Inject
	private IEventBroker eventBroker;

	static final String SAVED = "pagesaved"; //$NON-NLS-1$
	final MDirtyable dirtyable;
	final EditComposite editComposite;
	Label label;
	ScrolledComposite sc;

	/**
	 * Pass this view's context to the embedded composite
	 */
	@PostConstruct
	public void setContext() {
		editComposite.context = context;
		focusLatestWord();
		eventBroker = (IEventBroker) context.get(IEventBroker.class.getName());
	}

	private void focusLatestWord() {
		if (editComposite != null && editComposite.getWords() != null) {
			Text text = editComposite.getWords().get(
					DrcUiActivator.instance().currentUser().latestWord());
			text.setFocus();
			sc.showControl(text);
		}
	}

	/**
	 * @param parent
	 *            The parent composite for this part
	 * @param dirtyable
	 *            The dirtyable to display edit status
	 */
	@Inject
	public EditView(final Composite parent, final MDirtyable dirtyable) {
		this.dirtyable = dirtyable;
		sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
		label = new Label(parent, SWT.CENTER | SWT.WRAP);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editComposite = new EditComposite(this, SWT.NONE);
		sc.setContent(editComposite);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		editComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	/**
	 * @param pages
	 *            The selected pages
	 */
	@Inject
	public void setSelection(
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) final List<Page> pages) {
		if (pages != null && pages.size() > 0) {
			Page page = pages.get(0);
			if (dirtyable.isDirty()) {
				MessageDialog dialog = new MessageDialog(
						editComposite.getShell(), Messages.SavePage, null,
						Messages.CurrentPageModified, MessageDialog.CONFIRM,
						new String[] { IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL }, 0);
				dialog.create();
				if (dialog.open() == Window.OK) {
					doSave(null);
				}
			}
			editComposite.update(page);
			sc.setMinSize(editComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		} else {
			return;
		}
		dirtyable.setDirty(false);
	}

	/**
	 * @param progressMonitor
	 *            To show progress during saving
	 */
	@Persist
	public void doSave(@Optional final IProgressMonitor progressMonitor) {
		final IProgressMonitor monitor = progressMonitor == null ? new NullProgressMonitor()
				: progressMonitor;
		final Page page = editComposite.getPage();
		monitor.beginTask(Messages.SavingPage, page.words().size());
		final List<Text> words = editComposite.getWords();
		editComposite.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < words.size(); i++) {
					Text text = words.get(i);
					resetWarningColor(text);
					addToHistory(text);
					monitor.worked(1);
				}
				saveToXml(page);
				plainTextCopy(page);
				eventBroker.post(EditView.SAVED, page);
			}

			private void plainTextCopy(final Page page) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						String col = Index.DefaultCollection()
								+ PlainTextCopy.suffix();
						String vol = page.id().split("-")[0]; //$NON-NLS-1$
						XmlDb db = DrcUiActivator.instance().db();
						System.out.printf("Copy text to '%s', '%s' in %s\n", //$NON-NLS-1$
								col, vol, db);
						PlainTextCopy.saveToDb(page, col, vol, db);
					}
				}).start();
			}

			private void addToHistory(Text text) {
				String newText = text.getText();
				Word word = (Word) text.getData(Word.class.toString());
				Stack<Modification> history = word.history();
				Modification oldMod = history.top();
				if (!newText.equals(oldMod.form())
						&& !word.original().trim()
								.equals(Page.ParagraphMarker())) {
					User user = DrcUiActivator.instance().currentUser();
					if (!oldMod.author().equals(user.id())
							&& !oldMod.voters().contains(user.id())) {
						oldMod.downvote(user.id());
						User.withId(Index.DefaultCollection(),
								DrcUiActivator.instance().userDb(),
								oldMod.author()).wasDownvoted();
					}
					history.push(new Modification(newText, user.id()));
					user.hasEdited();
					user.save(DrcUiActivator.instance().userDb());
					text.setFocus();
				}
			}

			private void resetWarningColor(Text text) {
				if (text.getForeground()
						.equals(text.getDisplay().getSystemColor(
								EditComposite.DUBIOUS))) {
					text.setForeground(text.getDisplay().getSystemColor(
							EditComposite.DEFAULT));
					label.setText(""); //$NON-NLS-1$
				}
			}
		});
		dirtyable.setDirty(false);
	}

	private void saveToXml(final Page page) {
		System.out.println("Saving page: " + page); //$NON-NLS-1$
		page.saveToDb(DrcUiActivator.instance().currentUser().collection(),
				DrcUiActivator.instance().db());
	}
}
