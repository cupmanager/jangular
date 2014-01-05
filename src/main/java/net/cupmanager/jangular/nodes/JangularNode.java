package net.cupmanager.jangular.nodes;

import java.util.Collection;

import net.cupmanager.jangular.EvaluationContext;
import net.cupmanager.jangular.Scope;

public interface JangularNode {
	
	public Collection<String> getReferencedVariables();
	
	public void compileScope(Class<? extends Scope> parentScopeClass) throws Exception;
	
	public void eval(Scope scope, StringBuilder sb, EvaluationContext context);
	
}
