package net.cupmanager.jangular.compiler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Primitives;

public class CompilerSession {
	private List<String> warnings = new ArrayList<String>();
	
	private ClassLoader classLoader;
	
	public CompilerSession(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	
	public void warn(String warning) {
		warnings.add(warning);
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	public void assertCasts(Field toField, Field fromField) {
		Class<?> toFieldClass = Primitives.wrap(toField.getType());
		Class<?> fromFieldClass = Primitives.wrap(fromField.getType());
		
		if( !toFieldClass.isAssignableFrom(fromFieldClass)) {
			if( !fromFieldClass.isAssignableFrom(toFieldClass)) {
			
				throw new RuntimeException(String.format(
					"The @In-field %s (%s) in %s is not of the same type as in the parent scope (%s, %s)!",
					toField.getName(), toFieldClass.getName(), 
					toField.getDeclaringClass().getName(), 
					fromField.getDeclaringClass().getName(),
					fromFieldClass.getName()));
			} else {
				warn(String.format(
					"The @In-field %s (%s) in %s cannot be guaranteed to be of the same type as in the parent scope (%s, %s)."
					+"\nAn unchecked typecast from %s to %s will be performed at run time!",
					toField.getName(), toFieldClass.getName(), 
					toField.getDeclaringClass().getName(), 
					fromField.getDeclaringClass().getName(),
					fromFieldClass.getName(),
					fromFieldClass.getSimpleName(),
					toFieldClass.getSimpleName()));
			}
		}
	}
	
}
