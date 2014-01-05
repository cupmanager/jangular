package net.cupmanager.jangular.nodes;

import java.util.Collection;
import java.util.Collections;

import net.cupmanager.jangular.Scope;

public class TextNode implements JangularNode {

	private String text;

	public TextNode(String text) {
		this.text = text;
	}

	public void eval(Scope scope, StringBuilder sb) {
		sb.append(text);
	}

	public void merge(TextNode textNode) {
		text = text + textNode.text;
	}

	public Collection<String> getReferencedVariables() {
		return Collections.emptySet();
	}

	public void compileScope(Class<? extends Scope> parentScopeClass) {

	}

	public boolean isWhitespace() {
		return text.trim().isEmpty();
	}

}
