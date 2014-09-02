package net.cupmanager.jangular.compiler.templateloader;

import java.lang.reflect.Field;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.exceptions.CompileException;

public class NoSuchScopeFieldException extends CompileException {

	public NoSuchScopeFieldException(
			Class<? extends Scope> parentScopeClass,
			Class<? extends Scope> scopeClass,
			String fieldName){
		super(String.format("The scope %s does not have the field [%s, unknown type] that is required by %s",
				parentScopeClass.getCanonicalName(),
				fieldName,
				scopeClass.getCanonicalName()));
	}
	
	public NoSuchScopeFieldException(
			Class<? extends Scope> parentScopeClass,
			Class<? extends Scope> scopeClass,
			Field field){
		super(String.format("The scope %s does not have the field [%s of type %d] that is required by %s",
				parentScopeClass.getCanonicalName(),
				field.getName(),
				field.getType().getCanonicalName(),
				scopeClass.getCanonicalName()));
	}
	
	public NoSuchScopeFieldException(Throwable cause) {
		super(cause);
	}

}
