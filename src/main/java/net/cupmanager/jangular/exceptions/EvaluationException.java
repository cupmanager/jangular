package net.cupmanager.jangular.exceptions;

import net.cupmanager.jangular.nodes.JangularNode;

public class EvaluationException extends Exception {

	public EvaluationException(JangularNode node, Throwable cause) {
		super(cause);
	}

}
