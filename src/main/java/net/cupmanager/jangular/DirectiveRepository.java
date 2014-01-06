package net.cupmanager.jangular;

import java.util.HashMap;
import java.util.Map;

import net.cupmanager.jangular.annotations.Directive;

public class DirectiveRepository {
	private Map<String, Class<? extends AbstractDirective<?>>> directives = new HashMap<>();
	
	public void register(Class<? extends AbstractDirective<?>> directive) {
		Directive a = directive.getAnnotation(Directive.class);
		directives.put(a.value(), directive);
	}

	public boolean hasDirective(String qName) {
		return directives.containsKey(qName);
	}

	public Class<? extends AbstractDirective<?>> get(String name) {
		return directives.get(name);
	}

}
