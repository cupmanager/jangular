package net.cupmanager.jangular.exceptions;

import net.cupmanager.jangular.nodes.JangularNode;

public class ControllerNotFoundException extends CompileException {

	public ControllerNotFoundException(String controllerClassName,
			JangularNode node, ClassNotFoundException e) {
		super(e);
	}

}
