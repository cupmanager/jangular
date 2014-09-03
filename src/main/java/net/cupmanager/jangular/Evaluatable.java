package net.cupmanager.jangular;

import net.cupmanager.jangular.compiler.CompilerConfiguration.EmptyEvaluationContext;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.nodes.JangularNode.EvaluationSession;

public abstract class Evaluatable {
	public abstract EvaluationSession eval(Scope scope, StringBuilder sb, EvaluationContext context)
			throws EvaluationException;

	public EvaluationSession eval(Scope scope, StringBuilder sb) throws EvaluationException {
		return eval(scope, sb, new EmptyEvaluationContext());
	}
	
	public EvaluationSession eval(StringBuilder sb, EvaluationContext context) throws EvaluationException {
		return eval(new Scope(), sb, context);
	}
	
	public EvaluationSession eval(StringBuilder sb) throws EvaluationException {
		return eval(new Scope(), sb, new EmptyEvaluationContext()) ;
	}
}
