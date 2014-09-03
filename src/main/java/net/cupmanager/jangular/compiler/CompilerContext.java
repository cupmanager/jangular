package net.cupmanager.jangular.compiler;

import net.cupmanager.jangular.nodes.JangularNode;

public class CompilerContext {
	
	public CompilerContext(ResourceSpecification spec) {
		this.resourceSpecification = spec;
	}

	public ResourceSpecification resourceSpecification;
	public JangularNode transcludeContent;

	public CompilerContext tail() {
		CompilerContext context = new CompilerContext(resourceSpecification.tail());
		
		return context;
	}
	
}
