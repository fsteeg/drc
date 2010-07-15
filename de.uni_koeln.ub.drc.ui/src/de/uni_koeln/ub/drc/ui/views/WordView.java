/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.uni_koeln.ub.drc.data.Modification;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.User;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.views.WordViewModel.WordViewContentProvider;
import de.uni_koeln.ub.drc.ui.views.WordViewModel.WordViewLabelProvider;

/**
 * View containing details for the currently selected word.
 * @author Fabian Steeg (fsteeg)
 */
public final class WordView {

  private Word word;
  private TableViewer viewer;
  private Text suggestions;
  private Job job;
  private Button check;
  private Page page;

  @Inject
  public WordView(final Composite parent) {
    initTableViewer(parent);
    Composite bottom = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    bottom.setLayout(layout);
    check = new Button(bottom, SWT.CHECK);
    check.setToolTipText("Suggest corrections");
    check.setSelection(true);
    suggestions = new Text(bottom, SWT.NONE);
    suggestions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    GridLayoutFactory.fillDefaults().generateLayout(parent);
  }

  @Inject
  public void setSelection(@Optional @Named( IServiceConstants.ACTIVE_SELECTION ) final Text text) {
    if (job != null) {
      /* If a word is selected while we had a Job running for the previous word, cancel that: */
      job.cancel();
    }
    if (text == null) {
      suggestions.setText("No word selected");
    } else if (!check.getSelection()) {
      suggestions.setText("Edit suggestions disabled");
    } else {
      this.word = (Word) text.getData();
      setTableInput();
      findEditSuggestions();
      job.setPriority(Job.DECORATE);
      job.schedule();
    }
  }

  @Inject
  public void setSelection(
      @Optional @Named( IServiceConstants.ACTIVE_SELECTION ) final List<Page> pages) {
    if (pages != null && pages.size() > 0) {
      Page page = pages.get(0);
      System.out.println("Setting page: " + page);
      this.page = page;
    }
  }

  private void findEditSuggestions() {
    suggestions.setText("Finding edit suggestions...");
    job = new Job("Edit suggestions search job") {
      protected IStatus run(final IProgressMonitor monitor) {
        final boolean complete = word.prepSuggestions();
        suggestions.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            if (!complete) {
              suggestions.setText("Finding edit suggestions...");
            } else {
              final String s = "Suggestions for " + word.original() + ": "
                  + word.suggestions().mkString(", ");
              if (!suggestions.isDisposed()) {
                suggestions.setText(s);
              }
            }
          }
        });
        return Status.OK_STATUS;
      }

      @Override
      protected void canceling() {
        word.cancelled_$eq(true);
        suggestions.setText("Finding edit suggestions...");
      };
    };
  }

  private void initTableViewer(final Composite parent) {
    viewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
    initTable();
    viewer.setContentProvider(new WordViewContentProvider());
    viewer.setLabelProvider(new WordViewLabelProvider());
    setTableInput();
  }

  private void initTable() {
    final int[] columns = new int[] { 185, 300, 50, 30, 50 };
    createColumn("Form", columns[0], viewer);
    createColumn("Author", columns[1], viewer);
    createColumn("Votes", columns[2], viewer);
    createColumn("", columns[3], viewer);
    createColumn("", columns[4], viewer);
    Table table = viewer.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
  }

  TableViewerColumn createColumn(final String name, final int width, final TableViewer viewer) {
    TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
    column.getColumn().setText(name);
    column.getColumn().setWidth(width);
    column.getColumn().setResizable(true);
    column.getColumn().setMoveable(true);
    return column;
  }

  private void setTableInput() {
    if (word != null) {
      viewer.setInput(WordViewModel.CONTENT.getDetails(word));
    }
    addVotingButtons();
    // FIXME buttons remain visible when changing to word with less modifications in history
  }

  private void addVotingButtons() {
    TableItem[] items = viewer.getTable().getItems();
    for (int i = 0; i < items.length; i++) {
      final TableItem item = items[i];
      final int index = i;
      addButton(item, index, Vote.UP, 3);
      addButton(item, index, Vote.DOWN, 4);
    }
  }

  private static enum Vote {
    UP {
      @Override
      void update(Modification mod, User author, User voter) {
        mod.upvote(voter.id());
        voter.hasUpvoted(); // TODO pass to single vote(Vote) method
        author.wasUpvoted();
      }
    },
    DOWN {
      @Override
      void update(Modification mod, User author, User voter) {
        mod.downvote(voter.id());
        voter.hasDownvoted();
        author.wasDownvoted();
      }
    };
    abstract void update(Modification modification, User author, User voter);
  }

  private void addButton(final TableItem item, final int index, final Vote vote, int col) {
    Button good = createButton(item, vote.toString(), col);
    good.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        Modification modification = (Modification) viewer.getData(index + "");
        if (currentUserMayVote(modification)) {
          vote(modification, DrcUiActivator.instance().currentUser(), vote);
          MessageDialog.openInformation(item.getParent().getShell(), "Vote " + vote, "Voted "
              + modification + ": " + vote);
        }
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {}
    });

  }

  private void vote(Modification modification, User voter, Vote vote) {
    String usersFolder = DrcUiActivator.instance().usersFolder();
    User author = User.withId(modification.author(), usersFolder);
    vote.update(modification, author, voter);
    page.save();
    voter.save(usersFolder); // TODO save internally? requires internal association to location
    author.save(usersFolder);
  }

  private boolean currentUserMayVote(Modification modification) {
    User user = DrcUiActivator.instance().currentUser();
    if (modification.author().equals(user.id())) {
      MessageDialog.openWarning(viewer.getControl().getShell(), "Cannot vote for own edits",
          "You cannot upvote or downvote your own corrections");
      return false;
    }
    if (modification.voters().contains(user.id())) {
      MessageDialog.openWarning(viewer.getControl().getShell(), "Can vote only once",
          "You can only vote once for a correction");
      return false;
    }
    return true;
  }

  private Button createButton(final TableItem item, final String label, int columnIndex) {
    TableEditor editor = new TableEditor(viewer.getTable());
    Button button = new Button(viewer.getTable(), SWT.PUSH | SWT.FLAT);
    button.setText(label); // TODO icon, too?
    button.pack();
    editor.minimumWidth = button.getSize().x;
    editor.horizontalAlignment = SWT.LEFT;
    editor.setEditor(button, item, columnIndex);
    return button;
  }
}
