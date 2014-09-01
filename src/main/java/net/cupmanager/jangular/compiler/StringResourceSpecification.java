package net.cupmanager.jangular.compiler;

import java.util.Collection;
import java.util.Collections;

public class StringResourceSpecification implements ResourceSpecification {

	private String path;

	public StringResourceSpecification(String path) {
		this.path = path;
	}
	
	@Override
	public String getRootResource() {
		return path;
	}

	@Override
	public Collection<String> getResources() {
		return Collections.singleton(path);
	}
	
	public int hashCode(){
		return path.hashCode();
	}
	
	public boolean equals(Object object){
		if (object instanceof StringResourceSpecification) {
			StringResourceSpecification other = (StringResourceSpecification) object;
			return other.path.equals(path);
		} else {
			return false;
		}
	}

	@Override
	public ResourceSpecification tail() {
		return null;
	}

}
