package net.cupmanager.jangular.nodes;

import java.util.Collection;

import net.cupmanager.jangular.Evaluatable;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.injection.EvaluationContext;


public abstract class JangularNode extends Evaluatable {
	
	public abstract Collection<String> getReferencedVariables();

	public abstract void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			CompilerSession session) throws Exception;

	public abstract void eval(Scope scope, StringBuilder sb, EvaluationContext context);
}
