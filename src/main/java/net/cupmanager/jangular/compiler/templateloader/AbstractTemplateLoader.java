package net.cupmanager.jangular.compiler.templateloader;

import java.io.InputStream;


public abstract class AbstractTemplateLoader {
	public abstract InputStream loadTemplate(String template) throws TemplateLoaderException;
	
	public InputStream loadDirectiveTemplate(String template) throws TemplateLoaderException {
		return loadTemplate(template);
	}
}
