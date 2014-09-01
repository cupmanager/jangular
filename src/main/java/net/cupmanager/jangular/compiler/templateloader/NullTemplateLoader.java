package net.cupmanager.jangular.compiler.templateloader;

import java.io.InputStream;


public class NullTemplateLoader implements TemplateLoader {
	
	public NullTemplateLoader() {}
	
	@Override
	public InputStream loadTemplate(Object template) throws TemplateLoaderException {
		throw new TemplateLoaderException("No availiable template loader");
	}

	@Override
	public long getLastModified(Object template) {
		return 0;
	}
	
}
