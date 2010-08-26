/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.io.File;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import de.uni_koeln.ub.drc.data.Db;
import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.SearchOption;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;

/**
 * View containing a search field and a table viewer displaying pages.
 * @author Fabian Steeg (fsteeg)
 */
public final class SearchView {

  private Text searchField;
  private static CCombo searchOptions;
  private TableViewer viewer;

  @Inject
  private IEclipseContext context;

  @Inject
  public SearchView(final Composite parent) {
    Composite searchComposite = new Composite(parent, SWT.NONE);
    searchComposite.setLayout(new GridLayout(2, false));
    initSearchField(searchComposite);
    initOptionsCombo(searchComposite);
    initTableViewer(parent);
    GridLayoutFactory.fillDefaults().generateLayout(parent);
  }

  @PostConstruct
  public void select() {
    if (viewer.getElementAt(0) == null) {
      throw new IllegalArgumentException("No entries in initial search view");
    }
    viewer.setSelection(new StructuredSelection(viewer.getElementAt(0)));
  }

  private void initSearchField(final Composite parent) {
    searchField = new Text(parent, SWT.BORDER);
    searchField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    searchField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(final ModifyEvent e) {
        setInput();
      }
    });
  }

  private void initOptionsCombo(final Composite searchComposite) {
    searchOptions = new CCombo(searchComposite, SWT.NONE);
    searchOptions.setItems(SearchOption.toStrings());
    searchOptions.select(SearchOption.all().id());
    searchOptions.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        setInput();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
        setInput();
      }
    });
  }

  private void initTableViewer(final Composite parent) {
    viewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(final SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        context.modify(IServiceConstants.ACTIVE_SELECTION, selection.toList());
      }
    });
    initTable();
    viewer.setContentProvider(new SearchViewContentProvider());
    viewer.setLabelProvider(new SearchViewLabelProvider());
    setInput();
  }

  private void initTable() {
    final int[] columns = new int[] { 185, 50, 570, 180 };
    createColumn("File", columns[0]);
    createColumn("Octopus", columns[1]);
    createColumn("Text", columns[2]);
    createColumn("Modified", columns[3]);
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
      List<Page> pages = Index.loadPagesFromDb("PPN345572629_0004");
      index = new Index(pages);
    }

    public Page[] getPages(final String term) {
      String selectedSearchOption = searchOptions.getItem(searchOptions.getSelectionIndex());
      return index.search(term, SearchOption.withName(selectedSearchOption));
    }
  }

  private static final class SearchViewContentProvider implements IStructuredContentProvider {
    @Override
    public Object[] getElements(final Object inputElement) {
      Object[] elements = (Object[]) inputElement;
      return elements;
    }

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {}
  }

  private static final class SearchViewLabelProvider extends LabelProvider implements
      ITableLabelProvider {
    @Override
    public String getColumnText(final Object element, final int columnIndex) {
      Page page = (Page) element;
      switch (columnIndex) {
      case 0:
        return fileName(page);
      case 1:
        return PageConverter.convert(fileName(page));
      case 2:
        return page.toText("|");
      case 3:
        return lastModificationDate(page.words());
      default:
        return page.toString();
      }
    }

    private String fileName(Page page) {
      return page.id().substring(page.id().lastIndexOf(File.separatorChar) + 1);
    }

    private String lastModificationDate(List<Word> words) {
      long latest = 0;
      for (Word word : JavaConversions.asList(words)) {
        latest = Math.max(latest, word.history().top().date());
      }
      return new Date(latest).toString();
    }

    @Override
    public Image getColumnImage(final Object element, final int columnIndex) {
      return null;
    }
  }
}
