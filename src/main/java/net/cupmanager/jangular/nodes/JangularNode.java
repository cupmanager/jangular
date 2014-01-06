package net.cupmanager.jangular.nodes;

import java.util.Collection;

import net.cupmanager.jangular.JangularCompiler;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.injection.EvaluationContext;


public abstract class JangularNode {
	
	public abstract Collection<String> getReferencedVariables();

	public abstract void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			JangularCompiler compiler) throws Exception;

	public abstract void eval(Scope scope, StringBuilder sb, EvaluationContext context);
	
	
	
}
