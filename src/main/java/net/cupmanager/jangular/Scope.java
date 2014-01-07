package net.cupmanager.jangular;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.cupmanager.jangular.annotations.In;

public class Scope {

	public static List<String> getScopeIns(Class<? extends Scope> scopeClass) {
		Field[] fields = scopeClass.getFields();
		
		List<String> ins = new ArrayList<String>();
		for( Field f : fields ) {
			if( f.getAnnotation(In.class) != null ){
				ins.add(f.getName());
			}
		}
		return ins;
	}
	
	
}
