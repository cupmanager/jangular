package net.cupmanager.jangular.nodes;

import java.util.Collection;

import net.cupmanager.jangular.JangularCompiler;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.injection.EvaluationContext;


public interface JangularNode {
	
	public Collection<String> getReferencedVariables();

	public void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			JangularCompiler compiler) throws Exception;

	public void eval(Scope scope, StringBuilder sb, EvaluationContext context);
	
	
	
}
