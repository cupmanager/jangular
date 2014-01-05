package net.cupmanager.jangular.nodes;

import java.util.Collection;

import net.cupmanager.jangular.CompiledExpression;
import net.cupmanager.jangular.Scope;

public class ExpressionNode implements JangularNode {

	private String expression;
	private CompiledExpression compiledExpression;
	
	public ExpressionNode(String expression) {
		this.expression = expression;;
	}
	
	public void eval(Scope scope, StringBuilder sb) {
		sb.append(compiledExpression.evalToString(scope));
	}

	public Collection<String> getReferencedVariables() {
		return CompiledExpression.getReferencedVariables(expression);
	}

	public void compileScope(Class<? extends Scope> parentScopeClass) {
		compiledExpression = CompiledExpression.compile(expression, parentScopeClass);
	}

}
