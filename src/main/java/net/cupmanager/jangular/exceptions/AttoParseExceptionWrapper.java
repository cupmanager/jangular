package net.cupmanager.jangular.exceptions;

import org.attoparser.AttoParseException;

public class AttoParseExceptionWrapper extends AttoParseException {

	private static final long serialVersionUID = 6118986477368774197L;
	
	private ControllerNotFoundException controllerNotFoundException;

	public AttoParseExceptionWrapper(ControllerNotFoundException e) {
		this.controllerNotFoundException = e;
	}

	public void rethrowException() throws ControllerNotFoundException {
		if( controllerNotFoundException != null ){
			throw controllerNotFoundException;
		}
	}
	
}
