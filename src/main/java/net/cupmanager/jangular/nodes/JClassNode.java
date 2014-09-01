package net.cupmanager.jangular.nodes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.injection.EvaluationContext;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;

public class JClassNode extends JangularNode {

	private Serializable expression;
	private ParserContext pc;

	public JClassNode(String expression) {
		this.pc = new ParserContext();
		this.expression = MVEL.compileExpression(expression,pc);
	}
	
	
	private JClassNode() {
	}


	@Override
	public void eval(Scope scope, StringBuilder sb, EvaluationContext context) {
		Map<String, Boolean> result = (Map<String, Boolean>) MVEL.executeExpression(expression, scope);
		for (Map.Entry<String, Boolean> entry : result.entrySet()) {
			if (entry.getValue()) {
				sb.append(' ');
				sb.append(entry.getKey());
			}
		}
	}


	@Override
	public Collection<String> getReferencedVariables() {
		return pc.getInputs().keySet();
	}


	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			CompilerSession session) {
		
	}
	

	@Override
	public JangularNode clone() {
		JClassNode jc = new JClassNode();
		jc.expression = expression;
		jc.pc = pc;
		return jc;
	}
}
