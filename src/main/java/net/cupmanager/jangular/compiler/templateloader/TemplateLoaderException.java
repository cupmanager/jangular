package net.cupmanager.jangular.compiler.templateloader;

import java.io.FileNotFoundException;

import net.cupmanager.jangular.exceptions.CompileException;

public class TemplateLoaderException extends CompileException {

	private static final long serialVersionUID = -3001898707409655007L;

	public TemplateLoaderException(FileNotFoundException e) {
		super(e);
	}

	public TemplateLoaderException(String message) {
		super(message);
	}

	public TemplateLoaderException(Throwable cause) {
		super(cause);
	}

}
