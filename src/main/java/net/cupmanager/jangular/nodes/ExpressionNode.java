package net.cupmanager.jangular.nodes;

import java.util.Collection;

import net.cupmanager.jangular.EvaluationContext;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.expressions.CompiledExpression;

public class ExpressionNode implements JangularNode {

	private String expression;
	private CompiledExpression compiledExpression;
	
	public ExpressionNode(String expression) {
		this.expression = expression;;
	}
	
	@Override
	public void eval(Scope scope, StringBuilder sb, EvaluationContext context) {
		sb.append(compiledExpression.evalToString(scope));
	}

	@Override
	public Collection<String> getReferencedVariables() {
		return CompiledExpression.getReferencedVariables(expression);
	}

	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass) {
		compiledExpression = CompiledExpression.compile(expression, parentScopeClass);
	}

}
