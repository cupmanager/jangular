package net.cupmanager.jangular.nodes;

import java.util.Collection;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.injection.EvaluationContext;


public abstract class JangularNode {
	public static class EvaluationSession {
		private boolean debug = false;
		private String indentation = "";
		public void eval(JangularNode node, Scope scope, StringBuilder sb, EvaluationContext context) throws EvaluationException {
			indentation += "  ";
			long start = System.currentTimeMillis();
			if (debug) System.out.println(indentation + "Evaluating "+node);
			node.eval(scope, sb, context, this);
			long end = System.currentTimeMillis();
			if (debug) System.out.println(indentation + "Evaluating "+node+ " took "+(end-start)+"ms");
			indentation = indentation.substring(2);
		}
	}
	
	
	
	public abstract Collection<String> getReferencedVariables() throws CompileExpressionException;

	public abstract void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			CompilerSession session) throws NoSuchScopeFieldException, CompileExpressionException;

	protected abstract void eval(Scope scope, StringBuilder sb, EvaluationContext context, EvaluationSession session)
			throws EvaluationException;
	
	public abstract JangularNode clone();
}
