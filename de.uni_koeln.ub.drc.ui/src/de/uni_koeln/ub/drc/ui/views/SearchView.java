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
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import scala.collection.JavaConversions;
import scala.collection.mutable.ListBuffer;
import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.SearchOption;
import de.uni_koeln.ub.drc.data.Tag;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.Messages;
import de.uni_koeln.ub.drc.util.Chapter;
import de.uni_koeln.ub.drc.util.Count;
import de.uni_koeln.ub.drc.util.MetsTransformer;

/**
 * View containing a search field and a table viewer displaying pages.
 * @author Fabian Steeg (fsteeg)
 */
public final class SearchView {


  private static final String[] VOLUMES = new String[] { "0004", "0008", "0009", "0011", "0012", "0017", "0018", "0024", "0027", "0037" }; // "0033", "0035", "0036", "0037", "0038"
  private Text searchField;
  private Text tagField;
  private Label resultCount;
  private Combo searchOptions;
  private TreeViewer viewer;

  @Inject
  private IEclipseContext context;
  @Inject
  private ESelectionService selectionService;
  @Inject
  private IEventBroker eventBroker;
  private List<String> allPages;
  private int index;
  private Label currentPageLabel;

  private Comparator<Object> comp = new Comparator<Object>() {
    public int compare(Object p1, Object p2) {
      if (p1 instanceof Page && p2 instanceof Page) {
        return ((Page) p1).id().compareTo(((Page) p2).id());
      } else {
        return p1.toString().compareTo(p2.toString());
      }
    }
  };
  private Combo volumes;
  private Composite searchComposite;

  @Inject
  public SearchView(final Composite parent) {
    searchComposite = new Composite(parent, SWT.NONE);
    searchComposite.setLayout(new GridLayout(7, false));
    initVolumeSelector(searchComposite);
    initSearchField(searchComposite);
    initOptionsCombo(searchComposite);
    initTableViewer(parent);
    addPageInfoBar(parent);
    GridLayoutFactory.fillDefaults().generateLayout(parent);
  }

  private void initVolumeSelector(Composite searchComposite) {
    Label label1 = new Label(searchComposite, SWT.NONE);
    label1.setText(Messages.Volume);
    volumes = new Combo(searchComposite, SWT.READ_ONLY);
    String[] volumeLabels = new String[VOLUMES.length];
    for (int i = 0; i < VOLUMES.length; i++) {
      volumeLabels[i] = ""+(i+1);
    }
    volumes.setItems(volumeLabels);
    volumes.setData(VOLUMES);
    volumes.select(0);
    volumes.addSelectionListener(searchListener);
    Label label2 = new Label(searchComposite, SWT.NONE);
    label2.setText(Messages.Has);
  }

  @PostConstruct
  private void addFocusListener() {
    searchField.addFocusListener(new SpecialCharacterView.TextFocusListener(context, searchField));
    tagField.addFocusListener(new SpecialCharacterView.TextFocusListener(context, tagField));
  }
  
  @PostConstruct
  private void addEventHandler() {
    EventHandler handler = new EventHandler() {
      @Override
      public void handleEvent(final Event event) {
        searchComposite.getDisplay().asyncExec(new Runnable() {
          public void run() {
            Page page = (Page) event.getProperty(IEventBroker.DATA);
            if (page.edits() == 1) {
              viewer.setLabelProvider(new SearchViewLabelProvider());
            }
          }
        });
      }
    };
    eventBroker.subscribe(EditView.SAVED, handler);
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
      busyCursorWhile(viewer.getControl().getDisplay(), new Runnable() {
        @Override
        public void run() {
          updateSelection();
        }
      });
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {}
  }

  private void addPageInfoBar(Composite parent) {
    Composite bottomComposite = new Composite(parent, SWT.NONE);
    bottomComposite.setLayout(new GridLayout(6, false));
    Button prev = new Button(bottomComposite, SWT.PUSH | SWT.FLAT);
    prev.setImage(DrcUiActivator.instance().loadImage("icons/prev.gif")); //$NON-NLS-1$
    prev.addSelectionListener(new NavigationListener(Navigate.PREV));
    Button next = new Button(bottomComposite, SWT.PUSH | SWT.FLAT);
    next.setImage(DrcUiActivator.instance().loadImage("icons/next.gif")); //$NON-NLS-1$
    next.addSelectionListener(new NavigationListener(Navigate.NEXT));
    currentPageLabel = new Label(bottomComposite, SWT.NONE);
    insertAddCommentButton(bottomComposite);
    currentPageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  }

  private void insertAddCommentButton(Composite bottomComposite) {
    Label label = new Label(bottomComposite, SWT.NONE);
    label.setText(Messages.AddTag);
    tagField = new Text(bottomComposite, SWT.BORDER);
    Button addComment = new Button(bottomComposite, SWT.PUSH | SWT.FLAT);
    addComment.setToolTipText(Messages.AddNewTagToCurrentPage);
    addComment.setImage(DrcUiActivator.instance().loadImage("icons/add.gif")); //$NON-NLS-1$
    SelectionListener listener = new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) { // on button click
        addComment(tagField);
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) { // on enter in text
        addComment(tagField);
      }

      private void addComment(final Text text) {
        String input = text.getText();
        if (input != null && input.trim().length() != 0) {
          Page page = page(allPages.get(index));
          page.tags().$plus$eq(new Tag(input, DrcUiActivator.instance().currentUser().id()));
          page.saveToDb(DrcUiActivator.instance().currentUser().collection(), DrcUiActivator.instance().db());
          setCurrentPageLabel(page);
          text.setText(""); //$NON-NLS-1$
        }
      }

    };
    addComment.addSelectionListener(listener);
    tagField.addSelectionListener(listener);
  }

  String selected;

  private Page page(String string) {
    volumes.getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
    		selected = selected(volumes);
      }
    });
    return Page.fromXml(
        DrcUiActivator.instance().db().getXml(DrcUiActivator.instance().currentUser().collection() + "/" + selected, asBuffer(Arrays.asList(string))).get() //$NON-NLS-1$
            .head());
  }

  private void updateSelection() {
    if (index < allPages.size() && index >= 0) {
  	  	Page page = page(allPages.get(index));
  	  	setCurrentPageLabel(page);
  	  	StructuredSelection selection = new StructuredSelection(new Page[] { page });
  	  	selectionService.setSelection(selection.toList());
  	  	reload(viewer.getTree().getParent(), page);
    }
  }

  private void reload(final Composite parent, final Page page) {
    if (selectionService.getSelection() instanceof List
        && ((List<?>) selectionService.getSelection()).size() == 1) {
      System.out.println(Messages.ReloadingPage + page);
      parent.getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          Page reloaded = page(page.id());
          selectionService.setSelection(new StructuredSelection(reloaded).toList());
        }
      });
    }
  }

  private void setCurrentPageLabel(Page page) {
    currentPageLabel.setText(String.format(Messages.CurrentPageVolume + " %s, " + Messages.Page + " %s, %s", volumes.getSelectionIndex() + 1, //$NON-NLS-2$ //$NON-NLS-4$
        mets.label(page.number()), page.tags().size() == 0 ? Messages.NotTagged : Messages.TaggedAs + ": "
            + page.tags().mkString(", "))); //$NON-NLS-1$
  }

  @PostConstruct
  public void select() {
    if (viewer.getTree().getItems().length == 0) {
      throw new IllegalArgumentException(Messages.NoEntries);
    }
    allPages = new ArrayList<String>(asList(content.index.pages()));
    Collections.sort(allPages);
    for (String pageId : allPages) {
      if (pageId.equals(DrcUiActivator.instance().currentUser().latestPage())) {
        select(pageId);
        break;
      }
    }
    if (viewer.getSelection().isEmpty()) {
      viewer.setSelection(new StructuredSelection(allPages.get(0)));
    }
  }

  private void select(String pageId) {
    Page page = page(pageId);
    Chapter chapter = mets.chapter(page.number(), Count.File());
    TreeItem[] items = viewer.getTree().getItems();
    for (TreeItem treeItem : items) {
      if (treeItem.getText(3).contains(chapter.title())) {
        treeItem.setExpanded(true);
      }
    }
    viewer.refresh(chapter);
    viewer.setSelection(new StructuredSelection(page));
  }

  private void initSearchField(final Composite parent) {
    resultCount = new Label(parent, SWT.NONE);
    searchField = new Text(parent, SWT.BORDER);
    searchField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    searchField.addSelectionListener(searchListener);
  }

  private void initOptionsCombo(final Composite searchComposite) {
    Label label = new Label(searchComposite, SWT.NONE);
    label.setText(Messages.In);
    searchOptions = new Combo(searchComposite, SWT.READ_ONLY);
    searchOptions.setItems(new String[] { SearchOption.all().toString(),
    		SearchOption.tags().toString(), SearchOption.comments().toString() });
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

  private void updateResultCount(int count) {
    resultCount.setText(String.format("%s %s " + Messages.For, count, count == 1 ? Messages.Hit : Messages.Hits)); //$NON-NLS-1$
  }

  private boolean initial = true;
  
  private void initTableViewer(final Composite parent) {
    viewer = new TreeViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(final SelectionChangedEvent event) {
        busyCursorWhile(viewer.getControl().getDisplay(), new Runnable() {
          @Override
          public void run() {
            final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            if (selectionService.getSelection() != selection
                && selection.getFirstElement() instanceof Page) {
              final Page page = (Page) selection.getFirstElement();
              setCurrentPageLabel(page);
              selectionService.setSelection(selection.toList());
              if (!initial) { // don't reload initial page
                reload(parent, page);
                setCurrentPageLabel(page(allPages.get(index)));
              } else {
                initial = false;
              }
            }
          }
        });
      }
    });
    initTable();
    viewer.setContentProvider(new SearchViewContentProvider());
    viewer.setLabelProvider(new SearchViewLabelProvider());
    setInput();
  }

  private void busyCursorWhile(final Display display, final Runnable runnable) {
    display.asyncExec(new Runnable() {
      @Override
      public void run() {
        BusyIndicator.showWhile(viewer.getControl().getDisplay(), runnable);
      }
    });
  }

  @Inject
  public void setSelection(
      @Optional @Named( IServiceConstants.ACTIVE_SELECTION ) final List<Page> pages) {
    if (pages != null && pages.size() > 0) {
      Page page = pages.get(0);
      if (allPages != null) {
        index = allPages.indexOf(page.id());
      }
    }
  }

  private void initTable() {
    final int[] columns = new int[] { 60, 50, 60, 400, 200, 250 };
    createColumn("", columns[0]); //$NON-NLS-1$
    createColumn(Messages.Volume, columns[1]);
    createColumn(Messages.Page, columns[2]);
    createColumn(Messages.Text, columns[3]);
    createColumn(Messages.Modified, columns[4]);
    createColumn(Messages.Tags, columns[5]);
    Tree tree = viewer.getTree();
    tree.setHeaderVisible(true);
    tree.setLinesVisible(true);
  }

  private void createColumn(final String name, final int width) {
    TreeViewerColumn column1 = new TreeViewerColumn(viewer, SWT.NONE);
    column1.getColumn().setText(name);
    column1.getColumn().setWidth(width);
    column1.getColumn().setResizable(true);
    column1.getColumn().setMoveable(true);
  }

  private Map<Chapter, List<Object>> chapters = new TreeMap<Chapter, List<Object>>();
  private MetsTransformer mets;
  private String last = VOLUMES[0];

  private void setInput() {
    String current = selected(volumes);
    if (content == null || !current.equals(last)) {
      loadData();
      allPages = new ArrayList<String>(asList(content.index.pages()));
    }
    last = current;
    Object[] pages = content.getPages(searchField.getText().trim().toLowerCase());
    Arrays.sort(pages, comp);
    chapters = new TreeMap<Chapter, List<Object>>();
    mets = new MetsTransformer(current + ".xml", DrcUiActivator.instance().db()); //$NON-NLS-1$
    for (Object page : pages) {
      int fileNumber = page instanceof Page ? ((Page) page).number()
          : new Page(null, (String) page).number();
      Chapter chapter = mets.chapter(fileNumber, Count.File());
      List<Object> pagesInChapter = chapters.get(chapter);
      if (pagesInChapter == null) {
        pagesInChapter = new ArrayList<Object>();
        chapters.put(chapter, pagesInChapter);
      }
      pagesInChapter.add(page);
    }
    viewer.setInput(chapters);
    updateResultCount(pages.length);
  }

  private void loadData() {
    ProgressMonitorDialog dialog = new ProgressMonitorDialog(searchField.getShell());
    dialog.open();
    try {
      dialog.run(true, true, new IRunnableWithProgress() {
        public void run(final IProgressMonitor m) throws InvocationTargetException,
            InterruptedException {
          content = new SearchViewModelProvider(m);
        }
      });
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private SearchViewModelProvider content = null;

  private final class SearchViewModelProvider {

    Index index;
    String selected;

    private SearchViewModelProvider(final IProgressMonitor m) {
      viewer.getTree().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          selected = selected(volumes);
        }
      });
      List<String> ids = asList(DrcUiActivator.instance().db().getIds(DrcUiActivator.instance().currentUser().collection() + "/" + selected).get()); //$NON-NLS-1$
      m.beginTask(Messages.LoadingData, ids.size() / 2); // we only load the XML files
      List<String> pages = new ArrayList<String>();
      for (String id : ids) {
        if (id.endsWith(".xml")) { //$NON-NLS-1$
          m.subTask(id);
          pages.add(id);
          m.worked(1);
        }
        if (m.isCanceled()) {
          break;
        }
      }
      index = new Index(asBuffer(pages).toList(), DrcUiActivator.instance().db(), selected);
      m.done();
    }

    Object[] search;

    public Object[] getPages(final String term) {
      final String selectedSearchOption = searchOptions.getItem(searchOptions.getSelectionIndex());
      ProgressMonitorDialog dialog = new ProgressMonitorDialog(searchField.getShell());
      dialog.open();
      try {
        dialog.run(true, true, new IRunnableWithProgress() {
          public void run(final IProgressMonitor m) throws InvocationTargetException,
              InterruptedException {
            if (term.trim().equals("")) { //$NON-NLS-1$
              search = JavaConversions.asList(index.pages()).toArray(new String[] {});
            } else {
            	m.beginTask(Messages.SearchingIn + " " + index.pages().size() + " " + Messages.Pages, index.pages()
                        .size());
              search = null;
              new Thread(new Runnable() {
                public void run() {
                  while (search == null) {
                    m.worked(1);
                    try {
                      Thread.sleep(10);
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    }
                    if (m.isCanceled()) {
                      m.done();
                    }
                  }
                }
              }).start();
              search = index.search(term);
            }
          }
        });
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return search;
    }
  }

  private final class SearchViewContentProvider implements IStructuredContentProvider,
      ITreeContentProvider {
    @Override
    public Object[] getElements(final Object inputElement) {
      if (inputElement instanceof Map) {
        Object[] array = ((Map) inputElement).keySet().toArray(new Chapter[] {});
        Arrays.sort(array);
        return array;
      }
      Object[] elements = (Object[]) inputElement;
      return elements;
    }

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {}

    @Override
    public Object[] getChildren(Object parentElement) {
      List<Object> ids = chapters.get(parentElement);
      List<Page> pages = new ArrayList<Page>();
      for (Object object : ids) {
        pages.add(object instanceof String ? page((String) object) : (Page) object);
      }
      return pages.toArray(new Page[] {});
    }

    @Override
    public Object getParent(Object element) {
      return null;
    }

    @Override
    public boolean hasChildren(Object element) {
      return element instanceof Chapter;
    }
  }

  private boolean isPage(Object element) {
    return element instanceof Page;
  }

  private String selected(Combo volumes) {
    return "PPN345572629_" //$NON-NLS-1$
        + (volumes == null ? VOLUMES[0] : ((String[])volumes.getData())[volumes.getSelectionIndex()]);
  }

  private Page asPage(Object element) {
    return (Page) element;
  }

  private final class SearchViewLabelProvider extends LabelProvider implements ITableLabelProvider {
    @Override
    public String getColumnText(final Object element, final int columnIndex) {
      switch (columnIndex) {
      case 0:
        return ""; //$NON-NLS-1$
      case 1:
        return isPage(element) ? volumes.getSelectionIndex() + 1 + "" : ""; //$NON-NLS-1$ //$NON-NLS-2$
      case 2:
        return isPage(element) ? mets.label(asPage(element).number()) + "" : ""; //$NON-NLS-1$ //$NON-NLS-2$
      case 3: {
        if (isPage(element)) {
          String text = asPage(element).toText("|"); //$NON-NLS-1$
          return text.substring(0, Math.min(60, text.length())) + "..."; //$NON-NLS-1$
        } else
          return element.toString();
      }
      case 4:
        return isPage(element) ? lastModificationDate(asList(asPage(element).words())) : ""; //$NON-NLS-1$
      case 5:
        return isPage(element) ? asPage(element).tags().mkString(", ") : ""; //$NON-NLS-1$ //$NON-NLS-2$
      default:
        return element.toString();
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
      if (columnIndex == 0 && element instanceof Page) {
        Page page = (Page) element;
        // return new Image(searchOptions.getDisplay(), new ByteArrayInputStream(
        // Index.loadImageFor((Page) element))); // TODO add thumbnails to DB, use here
        return DrcUiActivator.instance().loadImage(
            page.edits() == 0 ? "icons/page.gif" : "icons/edited.gif"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      return null;
    }
  }

}