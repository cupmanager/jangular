package net.cupmanager.jangular.expressions;

import net.cupmanager.jangular.Scope;

import org.mvel2.MVEL;
import org.mvel2.compiler.ExecutableStatement;

public class VariableExpression extends CompiledExpression {

	private ExecutableStatement expression;
	
	public VariableExpression(ExecutableStatement expression) {
		this.expression = expression;
	}

	@Override
	public Object eval(Scope scope) {
		return MVEL.executeExpression(expression,scope);
	}

	@Override
	public String evalToString(Scope scope) {
		return eval(scope).toString();
	}

}
