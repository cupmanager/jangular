package net.cupmanager.jangular.nodes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import net.cupmanager.jangular.Scope;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;

public class JClassNode implements JangularNode {

	private Serializable expression;
	private ParserContext pc;

	public JClassNode(String expression) {
		this.pc = new ParserContext();
		this.expression = MVEL.compileExpression(expression,pc);
	}
	
	
	public void eval(Scope scope, StringBuilder sb) {
		Map<String, Boolean> result = (Map<String, Boolean>) MVEL.executeExpression(expression, scope);
		for (Map.Entry<String, Boolean> entry : result.entrySet()) {
			if (entry.getValue()) {
				sb.append(' ');
				sb.append(entry.getKey());
			}
		}
	}


	public Collection<String> getReferencedVariables() {
		return pc.getInputs().keySet();
	}


	public void compileScope(Class<? extends Scope> parentScopeClass) {
		
	}

}
