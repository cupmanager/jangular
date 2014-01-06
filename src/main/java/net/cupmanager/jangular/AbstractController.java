package net.cupmanager.jangular;

import java.lang.reflect.Method;


public abstract class AbstractController<T extends Scope> {
	
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
	
	public static Class<? extends Scope>  getScopeClass(Class<? extends AbstractController<?>> controllerClass) {
		Method[] methods = controllerClass.getMethods();
		for (Method m : methods) {
			if ("eval".equals(m.getName())) {
				if (m.getParameterTypes()[0] != Scope.class) {
					return (Class<? extends Scope>) m.getParameterTypes()[0];
				}
			}
		}
		return null;
	}
	
	public abstract void eval(T scope);
}
