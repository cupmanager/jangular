package net.cupmanager.jangular.nodes;

import java.util.Collection;

import net.cupmanager.jangular.Evaluatable;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.injection.EvaluationContext;


public abstract class JangularNode extends Evaluatable {
	
	public abstract Collection<String> getReferencedVariables() throws CompileExpressionException;

	public abstract void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			CompilerSession session) throws NoSuchScopeFieldException, CompileExpressionException;

	public abstract void eval(Scope scope, StringBuilder sb, EvaluationContext context)
			throws EvaluationException;
}
