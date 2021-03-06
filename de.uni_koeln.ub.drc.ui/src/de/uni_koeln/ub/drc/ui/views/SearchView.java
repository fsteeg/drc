/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

import scala.Enumeration;
import scala.collection.JavaConversions;

import com.quui.sinist.XmlDb;

import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.SearchOption;
import de.uni_koeln.ub.drc.data.Status;
import de.uni_koeln.ub.drc.data.Tag;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.Messages;
import de.uni_koeln.ub.drc.util.Chapter;
import de.uni_koeln.ub.drc.util.Count;
import de.uni_koeln.ub.drc.util.MetsTransformer;

/**
 * View containing a search field and a table viewer displaying pages.
 * 
 * @author Fabian Steeg (fsteeg), Mihail Atanassov (matana)
 */
public final class SearchView extends ViewPart {

	/**
	 * The class / SearchView ID
	 */
	public static final String ID = SearchView.class.getName().toLowerCase();

	private Text searchField;
	private Text tagField;
	private Label resultCount;
	private TreeViewer viewer;
	private Combo searchOptions;
	private TreeMap<String, Enumeration.Value> options = new TreeMap<String, Enumeration.Value>();
	private Combo volumes;
	private Composite searchComposite;
	private Button close;
	private List<String> allPages;
	private int index;
	private Label currentPageLabel;
	private List<Page> toExport;
	private String selected;

	/**
	 * @param parent
	 *            The parent composite for this part
	 */
	@Override
	public void createPartControl(Composite parent) {
		getSite().setSelectionProvider(viewer);
		searchComposite = new Composite(parent, SWT.NONE);
		searchComposite.setLayout(new GridLayout(7, false));
		initVolumeSelector(searchComposite);
		initSearchField(searchComposite);
		initOptionsCombo(searchComposite);
		initTableViewer(parent);
		addPageInfoBar(parent);
		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	@Override
	public void setFocus() {
	}

	/**
	 * @return The selection to export
	 */
	public List<Page> getSelectedPages() {
		return toExport;
	}

	/**
	 * Select the last word edited by this user.
	 */
	public void select() {
		String latestPage = DrcUiActivator.getDefault().currentUser()
				.latestPage();
		if (latestPage.trim().isEmpty()) {
			return;
		}
		String volume = latestPage.split("_")[1].split("-")[0]; //$NON-NLS-1$ //$NON-NLS-2$
		volumes.select(Index.RF().indexOf(volume));
		setInput();
		if (viewer.getTree().getItems().length == 0) {
			throw new IllegalArgumentException(Messages.get().NoEntries);
		}
		allPages = new ArrayList<String>(
				JavaConversions.asJavaList(content.modelIndex.pages()));
		Collections.sort(allPages);
		for (String pageId : allPages) {
			if (pageId.equals(latestPage)) {
				select(pageId);
				break;
			}
		}
		if (viewer.getSelection().isEmpty()) {
			viewer.setSelection(new StructuredSelection(allPages.get(0)));
		}
	}

	/**
	 * Updates the TreeViewer after a Word has been modified and saved.
	 */
	public void updateTreeViewer() {
		viewer.setLabelProvider(new SearchViewLabelProvider());
	}

	private Comparator<Object> comp = new Comparator<Object>() {
		@Override
		public int compare(Object p1, Object p2) {
			if (p1 instanceof Page && p2 instanceof Page)
				return ((Page) p1).id().compareTo(((Page) p2).id());
			return p1.toString().compareTo(p2.toString());
		}
	};

	private Combo show;

	private void initVolumeSelector(Composite searchComposite) {
		Label label1 = new Label(searchComposite, SWT.NONE);
		label1.setText(Messages.get().Volume);
		volumes = new Combo(searchComposite, SWT.READ_ONLY);
		String[] volumeLabels = new String[Index.RF().size()];
		for (int i = 0; i < Index.RF().size(); i++) {
			volumeLabels[i] = Index.Volumes().get(Index.RF().apply(i)).get();
		}
		volumes.setItems(volumeLabels);
		volumes.setData(JavaConversions.asJavaList(Index.RF()));
		volumes.select(0);
		volumes.addSelectionListener(searchListener);
		Label label2 = new Label(searchComposite, SWT.NONE);
		label2.setText(Messages.get().Has);
	}

	/* DI */@SuppressWarnings("unused")
	@PostConstruct
	private void addFocusListener() {
		searchField
				.addFocusListener(new SpecialCharacterView.TextFocusListener(
						searchField));
		tagField.addFocusListener(new SpecialCharacterView.TextFocusListener(
				tagField));
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
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	private void addPageInfoBar(Composite parent) {
		Composite bottomComposite = new Composite(parent, SWT.NONE);
		bottomComposite.setLayout(new GridLayout(9, false));
		Button prev = new Button(bottomComposite, SWT.PUSH | SWT.FLAT);
		prev.setImage(DrcUiActivator.getDefault().loadImage("icons/prev.gif")); //$NON-NLS-1$
		prev.addSelectionListener(new NavigationListener(Navigate.PREV));
		Button next = new Button(bottomComposite, SWT.PUSH | SWT.FLAT);
		next.setImage(DrcUiActivator.getDefault().loadImage("icons/next.gif")); //$NON-NLS-1$
		next.addSelectionListener(new NavigationListener(Navigate.NEXT));
		currentPageLabel = new Label(bottomComposite, SWT.NONE);
		Label label = new Label(bottomComposite, SWT.NONE);
		label.setText(Messages.get().Show + ": "); //$NON-NLS-1$
		show = new Combo(bottomComposite, SWT.READ_ONLY);
		show.setItems(new String[] { Messages.get().All, Messages.get().Open });
		show.select(0);
		show.addSelectionListener(searchListener);
		insertAddTagButton(bottomComposite);
		currentPageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addPageCheckedButton(bottomComposite);
	}

	private void addPageCheckedButton(Composite parent) {
		close = new Button(parent, SWT.CHECK);
		close.setText(Messages.get().ClosePage);
		close.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				Page page = page(allPages.get(index));
				page.status().$plus$eq(
						new Status(DrcUiActivator.getDefault().currentUser()
								.id(), System.currentTimeMillis(), close
								.getSelection()));
				page.saveToDb(DrcUiActivator.getDefault().currentUser()
						.collection(), DrcUiActivator.getDefault().db());
				viewer.setLabelProvider(new SearchViewLabelProvider());
				reload(close.getParent(), page);
			}
		});
	}

	private void insertAddTagButton(Composite bottomComposite) {
		Label label = new Label(bottomComposite, SWT.NONE);
		label.setText(Messages.get().AddTag);
		tagField = new Text(bottomComposite, SWT.BORDER);
		Button addComment = new Button(bottomComposite, SWT.PUSH | SWT.FLAT);
		addComment.setToolTipText(Messages.get().AddNewTagToCurrentPage);
		addComment.setImage(DrcUiActivator.getDefault().loadImage(
				"icons/add.gif")); //$NON-NLS-1$
		SelectionListener listener = new SelectionListener() {
			@Override
			// on button click
			public void widgetSelected(SelectionEvent e) {
				addTag(tagField);
			}

			@Override
			// on enter in text
			public void widgetDefaultSelected(SelectionEvent e) {
				addTag(tagField);
			}

			private void addTag(final Text text) {
				String input = text.getText();
				if (input != null && input.trim().length() != 0) {
					Page page = page(allPages.get(index));
					page.tags().$plus$eq(
							new Tag(input, DrcUiActivator.getDefault()
									.currentUser().id()));
					page.saveToDb(DrcUiActivator.getDefault().currentUser()
							.collection(), DrcUiActivator.getDefault().db());
					setCurrentPageLabel(page);
					viewer.setLabelProvider(new SearchViewLabelProvider());
					text.setText(""); //$NON-NLS-1$
				}
			}
		};
		addComment.addSelectionListener(listener);
		tagField.addSelectionListener(listener);
		// FIXME: Insertion of special characters
		// tagField.addFocusListener(new
		// SpecialCharacterView.TextFocusListener(tagField));
	}

	private Page page(String string) {
		volumes.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				selected = selected(volumes);
			}
		});
		return Page
				.fromXml(
						DrcUiActivator
								.getDefault()
								.db()
								.getXml(DrcUiActivator.getDefault()
										.currentUser().collection()
										+ "/" + selected, JavaConversions.asScalaBuffer(Arrays.asList(string))).get() //$NON-NLS-1$
								.head(), ""); //$NON-NLS-1$
	}

	private void updateSelection() {
		if (index < allPages.size() && index >= 0) {
			Page page = page(allPages.get(index));
			setCurrentPageLabel(page);
			StructuredSelection selection = new StructuredSelection(
					new Page[] { page });
			viewer.setSelection(selection);
			reload(viewer.getTree().getParent(), page);
		}
	}

	private void reload(final Composite parent, final Page page) {
		if (viewer.getSelection() instanceof List
				&& ((List<?>) viewer.getSelection()).size() == 1) {
			System.out.println(Messages.get().ReloadingPage + page);
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					Page reloaded = page(page.id());
					viewer.setSelection(new StructuredSelection(reloaded));
				}
			});
		}
	}

	private void setCurrentPageLabel(Page page) {
		currentPageLabel
				.setText(String.format(
						Messages.get().CurrentPageVolume
								+ " %s, " + Messages.get().Page + " %s", volumes.getItem(volumes.getSelectionIndex()), //$NON-NLS-1$ //$NON-NLS-2$
						mets.label(page.number())));
		close.setSelection(page.done());
	}

	private void select(String pageId) {
		Page page = page(pageId);
		Chapter chapter = mets.chapters(page.number(), Count.File()).head();
		TreeItem[] items = viewer.getTree().getItems();
		for (TreeItem treeItem : items) {
			if (treeItem.getText(3).contains(chapter.title())) {
				treeItem.setExpanded(true);
			}
		}
		viewer.refresh(chapter);
		viewer.setSelection(new StructuredSelection(page));
		EditView view = DrcUiActivator.find(EditView.class);
		view.focusLatestWord();
	}

	private void initSearchField(final Composite parent) {
		resultCount = new Label(parent, SWT.NONE);
		updateResultCount(-1);
		searchField = new Text(parent, SWT.BORDER);
		searchField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		searchField.addSelectionListener(searchListener);
	}

	private void initOptionsCombo(final Composite searchComposite) {
		Label label = new Label(searchComposite, SWT.NONE);
		label.setText(Messages.get().In);
		options.put(Messages.get().Text, SearchOption.all());
		options.put(Messages.get().Tags, SearchOption.tags());
		options.put(Messages.get().Comments, SearchOption.comments());
		searchOptions = new Combo(searchComposite, SWT.READ_ONLY);
		searchOptions.setItems(options.keySet().toArray(
				new String[options.keySet().size()]));
		searchOptions.select(new ArrayList<String>(options.keySet())
				.indexOf(Messages.get().Text));
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
		resultCount
				.setText(String
						.format("%s %s " + Messages.get().For, count == -1 ? "[no]" : count, count == 1 ? Messages.get().Hit : Messages.get().Hits)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void initTableViewer(final Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				busyCursorWhile(viewer.getControl().getDisplay(),
						new Runnable() {
							@SuppressWarnings("unchecked")
							@Override
							public void run() {
								final IStructuredSelection selection = (IStructuredSelection) event
										.getSelection();
								Object item = selection.getFirstElement();
								if (item instanceof Page) {
									final Page page = (Page) item;
									toExport = selection.toList();
									index = allPages.indexOf(page.id());
									setCurrentPageLabel(page);
								}

							}
						});
			}
		});
		initTable();
		viewer.setContentProvider(new SearchViewContentProvider());
		viewer.setLabelProvider(new SearchViewLabelProvider());
		DrcUiActivator.getDefault().register(this);
		getSite().setSelectionProvider(viewer);
	}

	private void busyCursorWhile(final Display display, final Runnable runnable) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				BusyIndicator.showWhile(viewer.getControl().getDisplay(),
						runnable);
			}
		});
	}

	private void initTable() {
		final int[] columns = new int[] { 60, 50, 60, 400, 200, 150, 100 };
		createColumn("", columns[0]); //$NON-NLS-1$
		createColumn(Messages.get().Volume, columns[1]);
		createColumn(Messages.get().Page, columns[2]);
		createColumn(Messages.get().Text, columns[3]);
		createColumn(Messages.get().Modified, columns[4]);
		createColumn(Messages.get().Tags, columns[5]);
		createColumn(Messages.get().Comments, columns[6]);
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
	private String last = JavaConversions.asJavaList(Index.RF()).get(0);

	/**
	 * Load SearchView content
	 */
	public void setInput() {
		BusyIndicator.showWhile(viewer.getControl().getDisplay(),
				new Runnable() {
					@Override
					public void run() {
						load();
					}
				});
	}

	private void pingCollection(final String current, final Page page,
			final XmlDb db) {
		/* Ping the collection in the background to avoid delay on first save: */
		new Thread(new Runnable() {
			@Override
			public void run() {
				db.putXml(page.toXml(), Index.DefaultCollection()
						+ "/" + current, page.id()); //$NON-NLS-1$
			}
		}).start();
	}

	private void loadData() {
		content = new SearchViewModelProvider();
	}

	private SearchViewModelProvider content = null;

	private final class SearchViewModelProvider {

		Index modelIndex;
		String modelSelected = null;

		private SearchViewModelProvider() {
			viewer.getTree().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					modelSelected = selected(volumes);
				}
			});
			List<String> ids = JavaConversions.asJavaList(DrcUiActivator
					.getDefault()
					.db()
					.getIds(DrcUiActivator.getDefault().currentUser()
							.collection()
							+ "/" + modelSelected).get()); //$NON-NLS-1$
			// we only load the XML files
			List<String> pages = new ArrayList<String>();
			for (String id : ids) {
				if (id.endsWith(".xml")) { //$NON-NLS-1$
					pages.add(id);
				}
			}
			modelIndex = new Index(JavaConversions.asScalaBuffer(pages)
					.toList(), DrcUiActivator.getDefault().db(), modelSelected,
					Index.DefaultCollection());
		}

		Object[] search;

		public Object[] getPages(final String term) {
			final String type = searchOptions.getItem(searchOptions
					.getSelectionIndex());
			if (term.trim().equals("")) { //$NON-NLS-1$
				search = JavaConversions.asJavaList(modelIndex.pages())
						.toArray(new String[modelIndex.pages().size()]);
			} else {
				search = null;
				search = modelIndex.search(term, options.get(type));
				System.out.println(String.format(
						"Searching for '%s' in %s returned %s results", //$NON-NLS-1$
						term, type, search.length));
			}
			return search;
		}
	}

	private final class SearchViewContentProvider implements
			ITreeContentProvider {
		@Override
		public Object[] getElements(final Object inputElement) {
			if (inputElement instanceof Map) {
				Object[] array = ((Map<?, ?>) inputElement).keySet().toArray(
						new Chapter[] {});
				Arrays.sort(array);
				return array;
			}
			Object[] elements = (Object[]) inputElement;
			return elements;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput,
				final Object newInput) {
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			List<Object> ids = chapters.get(parentElement);
			List<Page> pages = new ArrayList<Page>();
			for (Object object : ids) {
				Page page = object instanceof String ? page((String) object)
						: (Page) object;
				if (show.getSelectionIndex() == 0 || !page.done()) {
					pages.add(page);
				}
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

	@SuppressWarnings("unchecked")
	private String selected(Combo volumes) {
		return "PPN345572629_" //$NON-NLS-1$
				+ (volumes == null ? JavaConversions.asJavaList(Index.RF())
						.get(0) : ((List<String>) volumes.getData())
						.get(volumes.getSelectionIndex()));
	}

	private Page asPage(Object element) {
		return (Page) element;
	}

	private void load() {
		String current = selected(volumes);
		XmlDb db = DrcUiActivator.getDefault().db();
		if (content == null || !current.equals(last)) {
			loadData();
			allPages = new ArrayList<String>(
					JavaConversions.asJavaList(content.modelIndex.pages()));
			pingCollection(current, page(allPages.get(0)), db);
			Collections.sort(allPages);
		}
		last = current;
		Object[] pages = content.getPages(searchField.getText().trim()
				.toLowerCase());
		Arrays.sort(pages, comp);
		chapters = new TreeMap<Chapter, List<Object>>();
		boolean meta = true;
		try {
			mets = new MetsTransformer(current + ".xml", db); //$NON-NLS-1$
		} catch (NullPointerException x) {
			// No matadata available for selected volume
			meta = false;
		}
		for (Object page : pages) {
			int fileNumber = page instanceof Page ? ((Page) page).number()
					: new Page(null, (String) page).number();
			List<Chapter> chaptersForPage = meta ? /**/
			JavaConversions.asJavaList(mets.chapters(fileNumber, Count.File()))
					: Arrays.asList(new Chapter(0, 1, Messages.get().NoMeta));
			for (Chapter chapter : chaptersForPage) {
				List<Object> pagesInChapter = chapters.get(chapter);
				if (pagesInChapter == null) {
					pagesInChapter = new ArrayList<Object>();
					chapters.put(chapter, pagesInChapter);
				}
				pagesInChapter.add(page);
			}
		}
		viewer.setInput(chapters);
		updateResultCount(pages.length);
	}

	private final class SearchViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			switch (columnIndex) {
			case 0:
				return ""; //$NON-NLS-1$
			case 1:
				return isPage(element) ? volumes.getItem(volumes
						.getSelectionIndex()) : ""; //$NON-NLS-1$
			case 2:
				return isPage(element) && mets != null ? mets.label(asPage(
						element).number())
						+ "" : ""; //$NON-NLS-1$ //$NON-NLS-2$
			case 3: {
				if (isPage(element)) {
					String text = asPage(element).toText("|"); //$NON-NLS-1$
					return "Text: " //$NON-NLS-1$
							+ text.substring(0, Math.min(60, text.length()))
							+ "..."; //$NON-NLS-1$
				}
				return element.toString();
			}
			case 4:
				return isPage(element) ? lastModificationDate(JavaConversions
						.asJavaList(asPage(element).words())) : ""; //$NON-NLS-1$
			case 5:
				return isPage(element) ? asPage(element).tags().mkString(", ") : ""; //$NON-NLS-1$ //$NON-NLS-2$
			case 6:
				return isPage(element) ? asPage(element).comments().size() + "" : ""; //$NON-NLS-1$ //$NON-NLS-2$
			default:
				return element.toString();
			}
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
				return DrcUiActivator
						.getDefault()
						.loadImage(
								page.done() ? "icons/complete_status.gif" //$NON-NLS-1$
										: page.edits() == 0 ? "icons/page.gif" : "icons/edited.gif"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		}
	}

}
