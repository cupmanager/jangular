package net.cupmanager.jangular.nodes;

import java.util.Collection;
import java.util.Collections;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.injection.EvaluationContext;

public class TextNode extends JangularNode {

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
			CompilerSession session) {

	}

	public boolean isWhitespace() {
		return text.trim().isEmpty();
	}

	@Override
	public JangularNode clone() {
		return new TextNode(text);
	}

}
