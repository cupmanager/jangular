package net.cupmanager.jangular.testing;

import java.lang.reflect.Method;
import java.util.Map;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.nodes.CompositeNode;

public abstract class AbstractDirective<T extends Scope> {
	public void compile(Map<String, String> attributesObject, CompositeNode content) {
	}
	
	public Class<? extends Scope> getScopeClass() {
		Method[] methods = this.getClass().getMethods();
		for (Method m : methods) {
			if ("eval".equals(m.getName())) {
				if (m.getParameterTypes()[0] != Scope.class) {
					return (Class<? extends Scope>) m.getParameterTypes()[0];
				}
			}
		}
		return null;
	}
	
	public void eval(T scope) {
		
	}
}
