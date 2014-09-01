package net.cupmanager.jangular.compiler.templateloader;

import java.io.InputStream;


public interface TemplateLoader<T> {
	public InputStream loadTemplate(T template) throws TemplateLoaderException;
	public long getLastModified(T template);
}
