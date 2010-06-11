/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.handlers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import de.uni_koeln.ub.drc.data.Page;

/**
 * Handles page export, hooked into the menu via Application.xmi.
 * @author Fabian Steeg (fsteeg)
 */
public final class ExportHandler {
  private List<Page> pages;

  /* This one is required for the binding */
  @CanExecute boolean canExecute(@Named( IServiceConstants.ACTIVE_PART ) MContext context) {
    Object selected = context.getContext().get(IServiceConstants.SELECTION);
    if (!(selected instanceof List && ((List<?>) selected).get(0) instanceof Page)) {
      return false;
    }
    @SuppressWarnings( "unchecked" ) /* Safe, since we checked above */
    List<Page> selectedPages = (List<Page>) selected;
    pages = selectedPages;
    return pages != null;
  }

  /* This one is required for menu and tool bar items */
  @Inject public void setSelection(
      @Optional @Named( IServiceConstants.SELECTION ) final List<Page> pages) {
    this.pages = pages;
  }

  @Execute public void execute(final IWorkbench workbench) {
    Shell shell = Display.getCurrent().getActiveShell();
    if (pages == null) {
      MessageDialog.openError(shell, "No pages selected", "Please select pages to export");
      return;
    }
    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
    dialog.setFilterNames(new String[] { "Text files" });
    dialog.setFilterExtensions(new String[] { "*.txt" });
    String location = dialog.open();
    if (location != null) {
      save(pages, location);
      String pagesString = pages.size() == 1 ? "page" : "pages";
      MessageDialog.openInformation(shell, "Export",
          String.format("%s " + pagesString + " exported to %s", pages.size(), location));
    }
  }

  private void save(final List<Page> pages, final String location) {
    String fullText = concat(pages);
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(location, "UTF-8");
      writer.write(fullText);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      writer.close();
    }
  }

  private String concat(final List<Page> pages) {
    StringBuilder builder = new StringBuilder();
    for (Page page : pages) {
      builder.append(page.id()).append("\n\n").append(page.toText("\n\n"))
          .append("\n\n");
    }
    return builder.toString();
  }

}