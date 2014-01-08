package net.cupmanager.jangular.compiler.templateloader;

import java.io.FileNotFoundException;

public class TemplateLoaderException extends Exception {

	private static final long serialVersionUID = -3001898707409655007L;

	public TemplateLoaderException(FileNotFoundException e) {
		super(e);
	}

	public TemplateLoaderException() {
	}

}
