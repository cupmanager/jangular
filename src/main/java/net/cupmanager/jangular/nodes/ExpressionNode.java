package net.cupmanager.jangular.nodes;

import java.util.Collection;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.expressions.CompiledExpression;
import net.cupmanager.jangular.injection.EvaluationContext;

public class ExpressionNode extends JangularNode {

	private String expression;
	private CompiledExpression compiledExpression;
	
	public ExpressionNode(String expression) {
		this.expression = expression;;
	}
	
	private ExpressionNode() {
	}

	@Override
	public void eval(Scope scope, StringBuilder sb, EvaluationContext context, EvaluationSession session) {
		try {
			Object obj = compiledExpression.eval(scope);
			if (obj == null) {
				sb.append("[null]");
			} else {
				sb.append(obj.toString());
			}
//			sb.append(compiledExpression.evalToString(scope));
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.err.println("Exception in ExpressionNode: " + expression);
			throw e;
		}
		
	}

	@Override
	public Collection<String> getReferencedVariables() throws CompileExpressionException {
		return CompiledExpression.getReferencedVariables(expression);
	}

	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			CompilerSession session) throws CompileExpressionException {
		compiledExpression = CompiledExpression.compile(expression, parentScopeClass, session);
	}
	

	@Override
	public JangularNode clone() {
		ExpressionNode en = new ExpressionNode();
		en.expression = expression;
		en.compiledExpression = compiledExpression;
		return en;
	}
}
