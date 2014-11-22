package net.cupmanager.jangular.nodes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.injection.EvaluationContext;


public abstract class JangularNode {
	public static class EvaluationSession {
		public boolean debug = false;
		private String indentation = "";
		
		public static class CountDur {
			public long duration;
			public int count;
			
			public String toString() {
				return duration + " ms for " + count + " times";
			}
		}
		
		public Map<JangularNode, CountDur> durations = new HashMap<JangularNode, CountDur>();
		
		public void eval(JangularNode node, Scope scope, StringBuilder sb, EvaluationContext context) throws EvaluationException {
			indentation += "  ";
			long start = System.currentTimeMillis();
			if (debug) System.out.println(indentation + "Evaluating "+node);
			node.eval(scope, sb, context, this);
			long end = System.currentTimeMillis();
			if (debug) System.out.println(indentation + "Evaluating "+node+ " took "+(end-start)+"ms");
			
			if (!durations.containsKey(node)) {
				durations.put(node, new CountDur());
			}
			CountDur cd = durations.get(node);
			cd.duration += (end-start);
			cd.count++;
			
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
