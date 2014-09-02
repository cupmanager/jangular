package net.cupmanager.jangular.compiler.templateloader;

import java.io.InputStream;
import java.util.Collection;


public class NullTemplateLoader implements TemplateLoader {
	
	public NullTemplateLoader() {}
	
	@Override
	public InputStream loadTemplate(Object template) throws TemplateLoaderException {
		throw new TemplateLoaderException("No availiable template loader");
	}

	@Override
	public long getLastModified(Collection template) {
		return 0;
	}

	@Override
	public boolean exists(Object template) {
		return false;
	}
	
}
