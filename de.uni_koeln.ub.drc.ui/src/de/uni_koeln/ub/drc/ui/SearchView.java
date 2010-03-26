/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import javax.inject.Inject;

import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.Page;

/**
 * View containing a search field and a table viewer displaying pages.
 * @author Fabian Steeg (fsteeg)
 */
public final class SearchView {

  private Text searchField;
  private TableViewer viewer;

  @Inject private IEclipseContext context;

  @Inject public SearchView(final Composite parent) {
    initSearchField(parent);
    initTableViewer(parent);
    GridLayoutFactory.fillDefaults().generateLayout(parent);
  }

  @PostConstruct public void select() {
    viewer.setSelection(new StructuredSelection(viewer.getElementAt(0)));
  }

  private void initSearchField(final Composite parent) {
    searchField = new Text(parent, SWT.BORDER);
    searchField.addModifyListener(new ModifyListener() {
      @Override public void modifyText(final ModifyEvent e) {
        setInput();
      }
    });
  }

  private void initTableViewer(final Composite parent) {
    viewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(final SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        context.modify(IServiceConstants.SELECTION, selection.size() == 1 ? selection
            .getFirstElement() : selection.toArray());
      }
    });
    initTable();
    viewer.setContentProvider(new SearchViewContentProvider());
    viewer.setLabelProvider(new SearchViewLabelProvider());
    setInput();
  }

  private void initTable() {
    final int[] columns = new int[] { 185, 800 };
    createColumn("File", columns[0]);
    createColumn("Description", columns[1]);
    Table table = viewer.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
  }

  private void createColumn(final String name, final int width) {
    TableViewerColumn column1 = new TableViewerColumn(viewer, SWT.NONE);
    column1.getColumn().setText(name);
    column1.getColumn().setWidth(width);
    column1.getColumn().setResizable(true);
    column1.getColumn().setMoveable(true);
  }

  private void setInput() {
    viewer.setInput(SearchViewModelProvider.CONTENT.getPages(searchField.getText().trim()
        .toLowerCase()));
  }

  private static final class SearchViewModelProvider {
    public static final SearchViewModelProvider CONTENT = new SearchViewModelProvider();
    private Index index;

    private SearchViewModelProvider() {
      index = new Index(Index.loadPagesFromFolder(EditComposite.fileFromBundle("pages")
          .getAbsolutePath()));
    }

    public Page[] getPages(final String term) {
      return index.search(term);
    }
  }

  private static final class SearchViewContentProvider implements IStructuredContentProvider {
    @Override public Object[] getElements(final Object inputElement) {
      Object[] elements = (Object[]) inputElement;
      return elements;
    }

    @Override public void dispose() {}

    @Override public void inputChanged(final Viewer viewer, final Object oldInput,
        final Object newInput) {}
  }

  private static final class SearchViewLabelProvider extends LabelProvider implements
      ITableLabelProvider {
    @Override public String getColumnText(final Object element, final int columnIndex) {
      Page page = (Page) element;
      switch (columnIndex) {
      case 0:
        return page.id();
      case 1:
        return page.toString();
      default:
        return page.toString();
      }
    }

    @Override public Image getColumnImage(final Object element, final int columnIndex) {
      return null;
    }
  }
}
