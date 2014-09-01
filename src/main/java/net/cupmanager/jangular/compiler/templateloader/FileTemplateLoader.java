package net.cupmanager.jangular.compiler.templateloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class FileTemplateLoader implements TemplateLoader<String> {
	
	private String base;

	public FileTemplateLoader(String base) {
		this.base = base;
	}

	@Override
	public InputStream loadTemplate(String template) throws TemplateLoaderException {
		try {
			return new FileInputStream(new File(base, template));
		} catch (FileNotFoundException e) {
			throw new TemplateLoaderException(e);
		}
	}
	
	@Override
	public long getLastModified(String template) {
		return new File(base, template).lastModified();
	}
	
}
