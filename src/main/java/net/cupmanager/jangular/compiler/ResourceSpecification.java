package net.cupmanager.jangular.compiler;

import java.util.Collection;

public interface ResourceSpecification {
	public String getRootResource();
	public Collection<String> getResources();
	public ResourceSpecification tail();
}
