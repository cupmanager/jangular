package net.cupmanager.jangular.compiler.templateloader;

import java.io.InputStream;


public class NullTemplateLoader extends AbstractTemplateLoader {
	
	public NullTemplateLoader() {}
	
	@Override
	public InputStream loadTemplate(String template) throws TemplateLoaderException {
		throw new TemplateLoaderException();
	}
	
	@Override
	public InputStream loadDirectiveTemplate(String template) throws TemplateLoaderException {
		throw new TemplateLoaderException();
	}
	
	
	
}
