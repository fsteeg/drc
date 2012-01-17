package de.uni_koeln.ub.drc.ui;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

public class ViewsEditorTest extends UITestCaseSWT {

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		IUIContext ui = getUI();
		ui.ensureThat(new WorkbenchLocator().hasFocus());
		ui.ensureThat(ViewLocator.forName("Welcome").isClosed());
	}

	public void testViewsEditor() throws Exception {
		IUIContext ui = getUI();
		ui.click(new CTabItemLocator("SearchView"));
		ui.click(new CTabItemLocator("SpecialCharacterView"));
		ui.click(new CTabItemLocator("WordView"));
		ui.click(new CTabItemLocator("TagView"));
		ui.click(new CTabItemLocator("CommentsView"));
	}
}