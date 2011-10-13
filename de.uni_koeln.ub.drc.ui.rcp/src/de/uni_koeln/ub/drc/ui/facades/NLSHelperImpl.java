package de.uni_koeln.ub.drc.ui.facades;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class NLSHelperImpl extends NLSHelper {

	@Override
	protected Object getMessagesInternal(final Class<?> clazz) {
		ClassLoader loader = clazz.getClassLoader();
		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME,
				Locale.getDefault(), loader);
		return internalGet(bundle, clazz);
	}

	@SuppressWarnings("rawtypes")
	private Object internalGet(final ResourceBundle bundle, final Class<?> clazz) {
		Object result;
		try {
			Constructor constructor = clazz.getDeclaredConstructor(null);
			constructor.setAccessible(true);
			result = constructor.newInstance(null);
		} catch (final Exception ex) {
			throw new IllegalStateException(ex.getMessage());
		}
		final Field[] fieldArray = clazz.getDeclaredFields();
		for (int i = 0; i < fieldArray.length; i++) {
			try {
				int mod = fieldArray[i].getModifiers();
				if (String.class.isAssignableFrom(fieldArray[i].getType())
						&& Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
					try {
						String value = bundle
								.getString(fieldArray[i].getName());
						if (value != null) {
							fieldArray[i].setAccessible(true);
							fieldArray[i].set(result, value);
						}
					} catch (final MissingResourceException mre) {
						fieldArray[i].setAccessible(true);
						fieldArray[i].set(result, "");
						mre.printStackTrace();
					}
				}
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
}
