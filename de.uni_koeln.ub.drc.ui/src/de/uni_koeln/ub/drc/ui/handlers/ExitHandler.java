package de.uni_koeln.ub.drc.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

public class ExitHandler extends AbstractHandler {

	public static final String ID = ExitHandler.class.getName().toLowerCase();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// HandlerUtil.getActiveWorkbenchWindow(event).close();
		return PlatformUI.getWorkbench().close();
	}

}
