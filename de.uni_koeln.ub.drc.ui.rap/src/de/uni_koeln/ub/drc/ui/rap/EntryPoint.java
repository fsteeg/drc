package de.uni_koeln.ub.drc.ui.rap;

import org.eclipse.rwt.lifecycle.IEntryPoint;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class EntryPoint implements IEntryPoint {
	
	public static final String ID = EntryPoint.class.getName().toLowerCase();

	@Override
	public int createUI() {
		Display display = PlatformUI.createDisplay();
		WorkbenchAdvisor advisor = new ApplicationWorkbenchAdvisor();
		return PlatformUI.createAndRunWorkbench( display, advisor );
	}

}
