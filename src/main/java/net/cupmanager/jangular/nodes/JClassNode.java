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

public class JClassNode extends JangularNode {

	private Serializable expression;
	private ParserContext pc;
	private String stringExpression;

	public JClassNode(String expression) {
		this.stringExpression = expression;
	}
	
	
	private JClassNode() {
	}


	@Override
	public synchronized void eval(Scope scope, StringBuilder sb, EvaluationContext context, EvaluationSession session) {
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
		try {
			return CompiledExpression.getReferencedVariables(stringExpression);
		} catch (CompileExpressionException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
//		return pc.getInputs().keySet();
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
		JClassNode jc = new JClassNode();
		jc.expression = expression;
		jc.stringExpression = stringExpression;
		jc.pc = pc;
		return jc;
	}
}
