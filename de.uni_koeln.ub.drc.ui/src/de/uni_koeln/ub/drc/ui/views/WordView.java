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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.quui.sinist.XmlDb;

import de.uni_koeln.ub.drc.data.Modification;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.User;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.Messages;
import de.uni_koeln.ub.drc.ui.views.WordViewModel.WordViewContentProvider;
import de.uni_koeln.ub.drc.ui.views.WordViewModel.WordViewLabelProvider;

/**
 * View containing details for the currently selected word.
 * @author Fabian Steeg (fsteeg)
 */
public final class WordView {

  private Word word;
  private TableViewer viewer;
  private Page page;
  private Text text;

  @Inject
  public WordView(final Composite parent) {
    initTableViewer(parent);
    GridLayoutFactory.fillDefaults().generateLayout(parent);
  }

  @Inject
  public void setSelection(@Optional @Named( IServiceConstants.ACTIVE_SELECTION ) final Text text) {
    Word word = null;
    Page page = null;
    if (text != null && (word = (Word) text.getData(Word.class.toString())) != null
        && (page = (Page) text.getData(Page.class.toString())) != null) {
      this.text = text;
      this.word = word;
      this.page = page;
    }
    setTableInput();
  }

  @Inject
  public void setSelection(
      @Optional @Named( IServiceConstants.ACTIVE_SELECTION ) final List<Page> pages) {
    if (pages != null && pages.size() > 0) {
      Page page = pages.get(0);
      System.out.println(Messages.SettingPage + page);
      this.page = page;
    }
  }

  private void initTableViewer(final Composite parent) {
    viewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
    initTable();
    viewer.setContentProvider(new WordViewContentProvider());
    viewer.setLabelProvider(new WordViewLabelProvider());
    setTableInput();
  }

  private void initTable() {
    final int[] columns = new int[] { 185, 300, 200, 50, 70, 70, 70 };
    createColumn(Messages.Form, columns[0], viewer);
    createColumn(Messages.Author, columns[1], viewer);
    createColumn(Messages.Date, columns[2], viewer);
    createColumn(Messages.Votes, columns[3], viewer);
    createColumn(Messages.Upvote, columns[4], viewer);
    createColumn(Messages.Downvote, columns[5], viewer);
    createColumn(Messages.Revert, columns[6], viewer);
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
      TableHelper.clearWidgets(viewer.getTable());
      viewer.setInput(WordViewModel.CONTENT.getDetails(word));
      addWidgets();
    }
  }

  private void addWidgets() {
    TableItem[] items = viewer.getTable().getItems();
    for (int i = 0; i < items.length; i++) {
      final TableItem item = items[i];
      final int index = i;
      final String author = ((Modification) item.getData()).author();
      Link link = TableHelper.insertLink(viewer.getTable(), item, author, 1);
      Button up = addVoteButton(item, index, Vote.UP, 4);
      Button down = addVoteButton(item, index, Vote.DOWN, 5);
      Button rev = addRevertButton(item, index, 6);
      item.setData(new Widget[] { up, down, rev, link });
    }
  }

  private Button addRevertButton(final TableItem item, final int index, int col) {
    final Modification modification = (Modification) viewer.getData(index + ""); //$NON-NLS-1$
    if (!word.history().top().equals(modification)) { // no revert for most recent modification
      Button button = createButton(item, DrcUiActivator.instance().loadImage("icons/revert.gif"), //$NON-NLS-1$
          col);
      button.setEnabled(!word.isLocked());
      button.addSelectionListener(new SelectionListener() {
        @Override
        public void widgetSelected(final SelectionEvent e) {
          /*
           * If we revert to a previous modification the currently most recent modification is voted
           * down, and the modification that we are reverting to is voted up:
           */
          User currentUser = DrcUiActivator.instance().currentUser();
          vote(word.history().top(), currentUser, Vote.DOWN);
          vote(modification, currentUser, Vote.UP);
          MessageDialog.openInformation(item.getParent().getShell(), Messages.Reverted, Messages.RevertedTo
              + modification);
          text.setText(modification.form());
        }

        @Override
        public void widgetDefaultSelected(final SelectionEvent e) {}
      });
      return button;
    }
    return null;
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

  private Button addVoteButton(final TableItem item, final int index, final Vote vote, int col) {
    Button button = createButton(item,
        vote == Vote.UP ? DrcUiActivator.instance().loadImage("icons/up.gif") : DrcUiActivator //$NON-NLS-1$
            .instance().loadImage("icons/down.gif"), col); //$NON-NLS-1$
    button.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        Modification modification = (Modification) viewer.getData(index + ""); //$NON-NLS-1$
        if (currentUserMayVote(modification)) {
          vote(modification, DrcUiActivator.instance().currentUser(), vote);
          MessageDialog.openInformation(item.getParent().getShell(), Messages.Vote + vote, Messages.Voted
              + modification + ": " + vote); //$NON-NLS-1$
          text.setEditable(!word.isLocked());
        }
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {}
    });
    return button;
  }

  private void vote(Modification modification, User voter, Vote vote) {
    if (!modification.voters().contains(voter.id())) {
      XmlDb db = DrcUiActivator.instance().db();
      XmlDb userDb = DrcUiActivator.instance().userDb();
      User author = User.withId(userDb, modification.author());
      vote.update(modification, author, voter);
      page.saveToDb(db);
      voter.save(userDb);
      author.save(userDb);
      setTableInput();
    }
  }

  private boolean currentUserMayVote(Modification modification) {
    User user = DrcUiActivator.instance().currentUser();
    if (modification.author().equals(user.id())) {
      MessageDialog.openWarning(viewer.getControl().getShell(), Messages.CannotVoteForOwnShort,
          Messages.CannotVoteForOwnLong);
      return false;
    }
    if (modification.voters().contains(user.id())) {
      MessageDialog.openWarning(viewer.getControl().getShell(), Messages.CanVoteOnlyOnceShort,
          Messages.CanVoteOnlyOnceLong);
      return false;
    }
    return true;
  }

  private Button createButton(final TableItem item, final Image label, int columnIndex) {
    TableEditor editor = new TableEditor(viewer.getTable());
    Button button = new Button(viewer.getTable(), SWT.PUSH | SWT.FLAT);
    button.setImage(label);
    button.pack();
    editor.minimumWidth = 15;
    editor.minimumHeight = 15;
    editor.horizontalAlignment = SWT.LEFT;
    editor.setEditor(button, item, columnIndex);
    return button;
  }
}
