package net.cupmanager.jangular.compiler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ClassUtils;

public class CompilerSession {
	private List<String> warnings = new ArrayList<String>();

	public void printWarnings() {
		if (!warnings.isEmpty()) {
			System.err.println("The Jangular Compiler encountered the following warnings:\n");
			int i = 1;
			for( String warning : warnings ){
				System.err.println(i + " - "+warning);
				i++;
			}
		}
	}
	

	public void warn(String warning) {
		warnings.add(warning);
	}

	public void assertCasts(Field toField, Field fromField) {
		Class<?> toFieldClass = ClassUtils.primitiveToWrapper(toField.getType());
		Class<?> fromFieldClass = ClassUtils.primitiveToWrapper(fromField.getType());
		
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
