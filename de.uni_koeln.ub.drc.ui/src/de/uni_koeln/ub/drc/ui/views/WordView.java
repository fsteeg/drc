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
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
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

  @Inject public WordView(final Composite parent) {
    initTableViewer(parent);
    suggestions = new Text(parent, SWT.NONE);
    GridLayoutFactory.fillDefaults().generateLayout(parent);
  }

  @Inject public void setSelection(@Optional @Named( IServiceConstants.SELECTION ) final Text text) {
    if (job != null) {
      /* If a word is selected while we had a Job running for the previous word, cancel that: */
      job.cancel();
    }
    if (text == null) {
      suggestions.setText("No word selected.");
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
          @Override public void run() {
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

      @Override protected void canceling() {
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
    final int[] columns = new int[] { 185, 800 };
    createColumn("Form", columns[0], viewer);
    createColumn("Author", columns[1], viewer);
    Table table = viewer.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
  }

  void createColumn(final String name, final int width, final TableViewer viewer) {
    TableViewerColumn column1 = new TableViewerColumn(viewer, SWT.NONE);
    column1.getColumn().setText(name);
    column1.getColumn().setWidth(width);
    column1.getColumn().setResizable(true);
    column1.getColumn().setMoveable(true);
  }

  private void setTableInput() {
    if (word != null) {
      viewer.setInput(WordViewModelProvider.CONTENT.getDetails(word));
    }
  }

  private static final class WordViewModelProvider {
    public static final WordViewModelProvider CONTENT = new WordViewModelProvider();

    public Modification[] getDetails(final Word word) {
      return JavaConversions.asCollection(word.history()).toArray(new Modification[] {});
    }
  }

  private static final class WordViewContentProvider implements IStructuredContentProvider {
    @Override public Object[] getElements(final Object inputElement) {
      Object[] elements = (Object[]) inputElement;
      return elements;
    }

    @Override public void dispose() {}

    @Override public void inputChanged(final Viewer viewer, final Object oldInput,
        final Object newInput) {}
  }

  private static final class WordViewLabelProvider extends LabelProvider implements
      ITableLabelProvider {
    @Override public String getColumnText(final Object element, final int columnIndex) {
      Modification modification = (Modification) element;
      switch (columnIndex) {
      case 0:
        return modification.form();
      case 1:
        return modification.author();
      default:
        return modification.toString(); // TODO do we need modification.time()?
      }
    }

    @Override public Image getColumnImage(final Object element, final int columnIndex) {
      return null;
    }
  }
}
