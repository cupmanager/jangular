package net.cupmanager.jangular.compiler.templateloader;

import java.io.InputStream;
import java.util.Collection;


public interface TemplateLoader<T> {
	public boolean exists(T template);
	public InputStream loadTemplate(T template) throws TemplateLoaderException;
	public long getLastModified(Collection<? extends T> template);
}
