package de.uni_koeln.ub.drc.ui.facades;

import org.eclipse.rwt.RWT;

import de.uni_koeln.ub.drc.ui.Messages;
import de.uni_koeln.ub.drc.ui.facades.NLSHelper;


public class NLSHelperImpl extends NLSHelper {
	
	@Override
	protected Object getMessagesInternal(final Class<?> clazz) {
		return RWT.NLS.getISO8859_1Encoded(BUNDLE_NAME, Messages.class);
	}

}
