package net.cupmanager.jangular.compiler.templateloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class FileTemplateLoader extends AbstractTemplateLoader {
	
	private String base;
	private String directiveBase;

	public FileTemplateLoader(String base) {
		this(base, base);
	}
	
	public FileTemplateLoader(String base, String directiveBase) {
		this.base = base;
		this.directiveBase = directiveBase;
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
	public InputStream loadDirectiveTemplate(String template) throws TemplateLoaderException {
		try {
			return new FileInputStream(new File(directiveBase, template));
		} catch (FileNotFoundException e) {
			throw new TemplateLoaderException(e);
		}
	}
	
	
	
}
