package net.cupmanager.jangular;

import java.lang.reflect.Field;

import net.cupmanager.jangular.annotations.Inject;

import org.apache.commons.collections.map.MultiKeyMap;

public class EvaluationContext {

	private MultiKeyMap injects = new MultiKeyMap();
	
	public void inject(Object injectionTarget) {
		Field[] fields = injectionTarget.getClass().getDeclaredFields();
		for (Field field : fields) {
			Inject injectAnnotation = field.getAnnotation(Inject.class);
			if (injectAnnotation != null) {
				Object inject = injects.get(field.getType(), injectAnnotation.value());
				if (inject == null) {
					throw new RuntimeException("Could not inject a " + field.getType().getName() + " into " + injectionTarget.getClass() + 
							". The class "+field.getType().getName()+" has not been registered in EvaluationContext with a context of \""+injectAnnotation.value()+"\"");
				}
				try {
					field.setAccessible(true);
					field.set(injectionTarget, inject);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void add(Class<?> c, Object object) {
		add(c, "", object);
	}
	
	public void add(Class<?> c, String context, Object object) {
		injects.put(c, context, object);
	}
	
}
