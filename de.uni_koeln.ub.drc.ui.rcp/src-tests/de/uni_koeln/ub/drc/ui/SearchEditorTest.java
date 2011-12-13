package de.uni_koeln.ub.drc.ui;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.locator.LabeledTextLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

public class SearchEditorTest extends UITestCaseSWT {

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		IUIContext ui = getUI();
		ui.ensureThat(new WorkbenchLocator().hasFocus());
		ui.ensureThat(ViewLocator.forName("Welcome").isClosed());
	}

	public void testSearchEditor() throws Exception {
		IUIContext ui = getUI();
		ui.click(new LabeledTextLocator("218 hits for", new ViewLocator(
				"de.uni_koeln.ub.drc.ui.views.searchview")));
		ui.enterText("ils");
		ui.keyClick(WT.CR);
	}

}