package net.cupmanager.jangular.nodes;

import java.util.Collection;
import java.util.Collections;

import net.cupmanager.jangular.JangularCompiler;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.injection.EvaluationContext;

public class TextNode implements JangularNode {

	private String text;

	public TextNode(String text) {
		this.text = text;
	}

	@Override
	public void eval(Scope scope, StringBuilder sb, EvaluationContext context) {
		sb.append(text);
	}

	public void merge(TextNode textNode) {
		text = text + textNode.text;
	}

	@Override
	public Collection<String> getReferencedVariables() {
		return Collections.emptySet();
	}

	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			JangularCompiler compiler) {

	}

	public boolean isWhitespace() {
		return text.trim().isEmpty();
	}

}
