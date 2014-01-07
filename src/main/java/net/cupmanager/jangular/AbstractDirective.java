package net.cupmanager.jangular;

import java.lang.reflect.Method;
import java.util.Map;

import net.cupmanager.jangular.nodes.JangularNode;

public abstract class AbstractDirective<T extends Scope> {
	public void compile(Map<String, String> attributesObject, JangularNode content) {
	}
	
	public Class<? extends Scope> getScopeClass() {
		return getScopeClass(this.getClass());
	}
	
	public static Class<? extends Scope> getScopeClass(Class<? extends AbstractDirective> directiveClass) {
		Method[] methods = directiveClass.getMethods();
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
