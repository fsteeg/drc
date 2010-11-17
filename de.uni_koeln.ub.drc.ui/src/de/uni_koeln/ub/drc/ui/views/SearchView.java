/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import static scala.collection.JavaConversions.asBuffer;
import static scala.collection.JavaConversions.asList;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import scala.collection.JavaConversions;

import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.SearchOption;
import de.uni_koeln.ub.drc.data.Tag;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;

/**
 * View containing a search field and a table viewer displaying pages.
 * @author Fabian Steeg (fsteeg)
 */
public final class SearchView {

  private Text searchField;
  private Label resultCount;
  private static Combo searchOptions;
  private TableViewer viewer;

  @Inject
  private IEclipseContext context;
  private List<Page> allPages;
  private int index;
  private Label currentPageLabel;

  private Comparator<Page> comp = new Comparator<Page>() {
    public int compare(Page p1, Page p2) {
      return p1.id().compareTo(p2.id());
    }
  };

  @Inject
  public SearchView(final Composite parent) {
    Composite searchComposite = new Composite(parent, SWT.NONE);
    searchComposite.setLayout(new GridLayout(4, false));
    initSearchField(searchComposite);
    initOptionsCombo(searchComposite);
    initTableViewer(parent);
    addPageInfoBar(parent);
    GridLayoutFactory.fillDefaults().generateLayout(parent);
  }

  private enum Navigate {
    NEXT, PREV
  }

  private class NavigationListener implements SelectionListener {
    private Navigate nav;

    public NavigationListener(Navigate nav) {
      this.nav = nav;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      viewer.setSelection(new StructuredSelection());
      index = nav == Navigate.PREV ? index - 1 : index + 1;
      updateSelection();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {}
  }

  private void addPageInfoBar(Composite parent) {
    Composite bottomComposite = new Composite(parent, SWT.NONE);
    bottomComposite.setLayout(new GridLayout(6, false));
    Button prev = new Button(bottomComposite, SWT.PUSH | SWT.FLAT);
    prev.setImage(DrcUiActivator.instance().loadImage("icons/prev.gif"));
    prev.addSelectionListener(new NavigationListener(Navigate.PREV));
    Button next = new Button(bottomComposite, SWT.PUSH | SWT.FLAT);
    next.setImage(DrcUiActivator.instance().loadImage("icons/next.gif"));
    next.addSelectionListener(new NavigationListener(Navigate.NEXT));
    currentPageLabel = new Label(bottomComposite, SWT.NONE);
    insertAddCommentButton(bottomComposite);
    currentPageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  }

  private void insertAddCommentButton(Composite bottomComposite) {
    Label label = new Label(bottomComposite, SWT.NONE);
    label.setText("Add tag:");
    final Text text = new Text(bottomComposite, SWT.BORDER);
    Button addComment = new Button(bottomComposite, SWT.PUSH | SWT.FLAT);
    addComment.setToolTipText("Add a new tag to the current page");
    addComment.setImage(DrcUiActivator.instance().loadImage("icons/add.gif"));
    SelectionListener listener = new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) { // on button click
        addComment(text);
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) { // on enter in text
        addComment(text);
      }

      private void addComment(final Text text) {
        String input = text.getText();
        if (input != null && input.trim().length() != 0) {
          Page page = allPages.get(index);
          page.tags().$plus$eq(new Tag(input, DrcUiActivator.instance().currentUser().id()));
          page.saveToDb(DrcUiActivator.instance().db());
          setCurrentPageLabel(page);
          text.setText("");
        }
      }

    };
    addComment.addSelectionListener(listener);
    text.addSelectionListener(listener);
  }

  private void updateSelection() {
    if (index < allPages.size() && index >= 0) {
      Page page = allPages.get(index);
      setCurrentPageLabel(page);
      StructuredSelection selection = new StructuredSelection(new Page[] { page });
      context.modify(IServiceConstants.ACTIVE_SELECTION, selection.toList());
      reload(viewer.getTable().getParent(),page);
    }
  }
  
  private void reload(final Composite parent, final Page page) {
    System.out.println("Reloading page: " + page);
    parent.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        Page reloaded = Page.fromXml(
            DrcUiActivator
                .instance()
                .db()
                .getXml(SearchViewModelProvider.COLLECTION,
                    JavaConversions.asBuffer(Arrays.asList(page.id()))).get().head(), page.id());
        context.modify(IServiceConstants.ACTIVE_SELECTION, new StructuredSelection(reloaded).toList());
      }
    });
  }

  private void setCurrentPageLabel(Page page) {
    currentPageLabel.setText(String.format("Current page: volume %s, page %s, %s", mappedVolume(page),
        page.number(), page.tags().size() == 0 ? "not tagged" : "tagged as: "
            + page.tags().mkString(", ")));
  }

  @PostConstruct
  public void select() {
    if (viewer.getElementAt(0) == null) {
      throw new IllegalArgumentException("No entries in initial search view");
    }
    TableItem[] items = viewer.getTable().getItems();
    for (int i = 0; i < items.length; i++) {
      Page page = (Page) items[i].getData();
      if(page.id().equals(DrcUiActivator.instance().currentUser().latestPage())) {
        viewer.setSelection(new StructuredSelection(viewer.getElementAt(i)));
        break;
      }
    }
    if(viewer.getSelection().isEmpty()){
      viewer.setSelection(new StructuredSelection(viewer.getElementAt(0)));
    }
    allPages = new ArrayList<Page>(asList(SearchViewModelProvider.content.index.pages()));
    Collections.sort(allPages, comp);
  }

  private void initSearchField(final Composite parent) {
    resultCount = new Label(parent, SWT.NONE);
    searchField = new Text(parent, SWT.BORDER);
    searchField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    searchField.addSelectionListener(searchListener);
  }

  private void initOptionsCombo(final Composite searchComposite) {
    Label label = new Label(searchComposite, SWT.NONE);
    label.setText("in:");
    searchOptions = new Combo(searchComposite, SWT.READ_ONLY);
    searchOptions.setItems(new String[] { SearchOption.all().toString(),
        SearchOption.tags().toString() });
    searchOptions.select(0);
    searchOptions.addSelectionListener(searchListener);
  }

  private SelectionListener searchListener = new SelectionListener() {
    @Override
    public void widgetSelected(final SelectionEvent e) {
      setInput();
    }

    @Override
    public void widgetDefaultSelected(final SelectionEvent e) {
      setInput();
    }
  };
  
  private void updateResultCount() {
    int count = viewer.getTable().getItemCount();
    resultCount.setText(String.format("%s %s for:", count, count == 1 ? "hit" : "hits"));
  }

  private boolean initial = true;
  
  private void initTableViewer(final Composite parent) {
    viewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(final SelectionChangedEvent event) {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (selection.getFirstElement() instanceof Page) {
          String oldPageLabel = currentPageLabel.getText();
          final Page page = (Page) selection.getFirstElement();
          setCurrentPageLabel(page);
          context.modify(IServiceConstants.ACTIVE_SELECTION, selection.toList());
          if (!initial) { // don't reload initial page
            reload(parent, page);
          } else {
            initial = false;
          }
        }
      }
    });
    initTable();
    viewer.setContentProvider(new SearchViewContentProvider());
    viewer.setLabelProvider(new SearchViewLabelProvider());
    setInput();
  }

  @Inject
  public void setSelection(
      @Optional @Named( IServiceConstants.ACTIVE_SELECTION ) final List<Page> pages) {
    if (pages != null && pages.size() > 0) {
      Page page = pages.get(0);
      if (allPages != null) {
        index = allPages.indexOf(page);
      }
    }
  }

  private void initTable() {
    final int[] columns = new int[] { 25, 50, 60, 400, 200, 250 };
    createColumn("", columns[0]);
    createColumn("Volume", columns[1]);
    createColumn("Page", columns[2]);
    createColumn("Text", columns[3]);
    createColumn("Modified", columns[4]);
    createColumn("Tags", columns[5]);
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
    if (SearchViewModelProvider.content == null) {
      loadData();
    }
    Page[] pages = SearchViewModelProvider.content.getPages(searchField.getText().trim()
        .toLowerCase());
    Arrays.sort(pages, comp);
    viewer.setInput(pages);
    updateResultCount();
  }

  private void loadData() {
    ProgressMonitorDialog dialog = new ProgressMonitorDialog(searchField.getShell());
    dialog.open();
    try {
      dialog.run(true, true, new IRunnableWithProgress() {
        public void run(final IProgressMonitor m) throws InvocationTargetException,
            InterruptedException {
          SearchViewModelProvider.content = new SearchViewModelProvider(m);
        }
      });
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static final class SearchViewModelProvider {
    private static SearchViewModelProvider content = null;
    private final static String COLLECTION = "PPN345572629_0004";
    Index index;

    private SearchViewModelProvider(IProgressMonitor m) {
      List<String> ids = asList(DrcUiActivator.instance().db().getIds(COLLECTION).get());
      m.beginTask("Loading data from the DB...", ids.size() / 2); // we only load the XML files
      List<Page> pages = new ArrayList<Page>();
      for (String id : ids) {
        if (id.endsWith(".xml")) {
          m.subTask(id);
          pages.add(Page.fromXml(
              DrcUiActivator.instance().db().getXml(COLLECTION, asBuffer(Arrays.asList(id))).get().head(),
              id));
          m.worked(1);
        }
        if (m.isCanceled()) {
          break;
        }
      }
      index = new Index(asBuffer(pages).toList());
      m.done();
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
        return "";
      case 1:
        return mappedVolume(page) + "";
      case 2:
        return PageConverter.convert(fileName(page));
      case 3:
        return page.toText("|").substring(0, 60) + "...";
      case 4:
        return lastModificationDate(asList(page.words()));
      case 5:
        return page.tags().mkString(", ");
      default:
        return page.toString();
      }
    }
    
    private String fileName(Page page) {
      return page.id().substring(page.id().lastIndexOf(File.separatorChar) + 1);
    }

    private String lastModificationDate(List<Word> words) {
      long latest = 0;
      for (Word word : words) {
        latest = Math.max(latest, word.history().top().date());
      }
      return new Date(latest).toString();
    }

    @Override
    public Image getColumnImage(final Object element, final int columnIndex) {
      if (columnIndex == 0) {
        Page page = (Page) element;
        // return new Image(searchOptions.getDisplay(), new ByteArrayInputStream(
        // Index.loadImageFor((Page) element))); // TODO add thumbnails to DB, use here
        return DrcUiActivator.instance().loadImage(
            page.edits() == 0 ? "icons/page.gif" : "icons/edited.gif");
      }
      return null;
    }
  }
  
  private static int mappedVolume(Page page) {
    return page.volume() - 3; // TODO proper mapping for volume
  }
}
