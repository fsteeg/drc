/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import scala.collection.JavaConversions;
import de.uni_koeln.ub.drc.data.Modification;
import de.uni_koeln.ub.drc.data.Word;

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
    final int[] columns = new int[] { 185, 250, 50, 30, 50 };
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
      viewer.setInput(WordViewModelProvider.CONTENT.getDetails(word));
    }
    addVotingButtons();
    // FIXME buttons remain visible when changing to word with less modifications in history
  }

  private void addVotingButtons() {
    TableItem[] items = viewer.getTable().getItems();
    System.out.println("Got: " + items.length);
    for (int i = 0; i < items.length; i++) {
      final TableItem item = items[i];
      final int index = i;
      addUpvoteButton(item, index);
      addDownvoteButton(item, index);
    }
  }

  private void addDownvoteButton(final TableItem item, final int index) {
    Button bad = createButton(item, "down", 4);
    bad.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        MessageDialog.openInformation(item.getParent().getShell(), "Downvote", "Would downvote: "
            + viewer.getData(index + ""));
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {}
    });
  }

  private void addUpvoteButton(final TableItem item, final int index) {
    Button good = createButton(item, "up", 3);
    good.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        MessageDialog.openInformation(item.getParent().getShell(), "Upvote", "Would upvote: "
            + viewer.getData(index + ""));
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {}
    });
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

  private static final class WordViewModelProvider {
    public static final WordViewModelProvider CONTENT = new WordViewModelProvider();

    public Modification[] getDetails(final Word word) {
      return JavaConversions.asCollection(word.history()).toArray(new Modification[] {});
    }
  }

  private static final class WordViewContentProvider implements IStructuredContentProvider {
    @Override
    public Object[] getElements(final Object inputElement) {
      Object[] elements = (Object[]) inputElement;
      return elements;
    }

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
      System.out.println("Changed to: " + newInput);
      if (newInput != null) {
        Modification[] newMods = (Modification[]) newInput;
        for (int i = 0; i < newMods.length; i++) {
          viewer.setData(i + "", newMods[i]);
        }
      }
    }
  }

  private static final class WordViewLabelProvider extends LabelProvider implements
      ITableLabelProvider {

    @Override
    public String getColumnText(final Object element, final int columnIndex) {
      Modification modification = (Modification) element;
      switch (columnIndex) {
      case 0:
        return modification.form();
      case 1:
        return modification.author();
      case 2:
        return "0"; // TODO modification.score
      default:
        return null; // TODO do we need modification.time()?
      }
    }

    @Override
    public Image getColumnImage(final Object element, final int columnIndex) {
      return null;
    }
  }
}
