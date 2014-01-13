package net.cupmanager.jangular.exceptions;

public class CompileExpressionException extends CompileException {

	public CompileExpressionException(org.mvel2.CompileException cause) {
		super(cause);
	}

	private static final long serialVersionUID = -2584587101295214215L;

}
