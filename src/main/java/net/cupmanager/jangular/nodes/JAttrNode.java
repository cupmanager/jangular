package net.cupmanager.jangular.nodes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.expressions.CompiledExpression;
import net.cupmanager.jangular.injection.EvaluationContext;

import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;

public class JAttrNode extends JangularNode {

	private Serializable expression;
	private ParserContext pc;
	private String stringExpression;

	public JAttrNode(String expression) {
		this.stringExpression = expression;
	}
	
	
	private JAttrNode() {
	}


	@Override
	public synchronized void eval(Scope scope, StringBuilder sb, EvaluationContext context, EvaluationSession session) {
		Map<String, Object> result = (Map<String, Object>) MVEL.executeExpression(expression, scope);
		for (Map.Entry<String, Object> entry : result.entrySet()) {
			Object v = entry.getValue();
			if (v != null) {
				if (v instanceof Boolean) {
					if ((Boolean)v) {
						v = entry.getKey();
					} else {
						v = null;
					}
				}
				if (v != null) {
					sb.append(" ");
					sb.append(entry.getKey());
					sb.append("=\"");
					sb.append(v);
					sb.append("\"");
				}
			}
		}
	}


	@Override
	public Collection<String> getReferencedVariables() {
		try {
			return CompiledExpression.getReferencedVariables(stringExpression);
		} catch (CompileExpressionException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
	}


	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			CompilerSession session) {
		ParserConfiguration conf = new ParserConfiguration();
		conf.setClassLoader(session.getClassLoader());
		this.pc = new ParserContext(conf);
		this.expression = MVEL.compileExpression(stringExpression,pc);
	}
	

	@Override
	public JangularNode clone() {
		JAttrNode jc = new JAttrNode();
		jc.expression = expression;
		jc.stringExpression = stringExpression;
		jc.pc = pc;
		return jc;
	}
}
