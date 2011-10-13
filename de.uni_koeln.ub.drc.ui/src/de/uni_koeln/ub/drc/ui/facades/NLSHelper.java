package de.uni_koeln.ub.drc.ui.facades;

import de.uni_koeln.ub.drc.ui.Messages;
import de.uni_koeln.ub.drc.ui.util.ImplementationLoader;

public abstract class NLSHelper {

	protected static final String BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	public final static NLSHelper IMPL;

	static {
		IMPL = (NLSHelper) ImplementationLoader.newInstance(NLSHelper.class);
	}

	public static Messages getMessages(final Class<?> clazz) {
		return (Messages) IMPL.getMessagesInternal(clazz);
	}

	protected abstract Object getMessagesInternal(final Class<?> clazz);

}
