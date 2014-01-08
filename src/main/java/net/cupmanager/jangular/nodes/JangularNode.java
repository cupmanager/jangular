package net.cupmanager.jangular.nodes;

import java.util.Collection;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerConfiguration.EmptyEvaluationContext;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.injection.EvaluationContext;


public abstract class JangularNode {
	
	public abstract Collection<String> getReferencedVariables();

	public abstract void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			CompilerSession session) throws Exception;

	public abstract void eval(Scope scope, StringBuilder sb, EvaluationContext context);

	public void eval(Scope scope, StringBuilder sb) {
		eval(scope, sb, new EmptyEvaluationContext());
	}
	
	
	
}
