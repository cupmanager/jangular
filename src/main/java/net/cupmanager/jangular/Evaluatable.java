package net.cupmanager.jangular;

import net.cupmanager.jangular.compiler.CompilerConfiguration.EmptyEvaluationContext;
import net.cupmanager.jangular.injection.EvaluationContext;

public abstract class Evaluatable {
	public abstract void eval(Scope scope, StringBuilder sb, EvaluationContext context);

	public void eval(Scope scope, StringBuilder sb) {
		eval(scope, sb, new EmptyEvaluationContext());
	}
	
	public void eval(StringBuilder sb, EvaluationContext context) {
		eval(new Scope(), sb, context);
	}
	
	public void eval(StringBuilder sb) {
		eval(new Scope(), sb, new EmptyEvaluationContext());
	}
}
