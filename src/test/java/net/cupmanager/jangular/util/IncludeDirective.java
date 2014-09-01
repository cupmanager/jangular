package net.cupmanager.jangular.util;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.cupmanager.jangular.AbstractDirective;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.Directive;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.compiler.CompilerContext;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoader;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.nodes.JangularNode;
import net.cupmanager.jangular.util.IncludeDirective.IncludeDirectiveScope;

@Directive("j-include")
public class IncludeDirective extends AbstractDirective<IncludeDirectiveScope> {

	private CompilerContext context;
	
	public static class IncludeDirectiveScope extends Scope {
		@In public int i;
		public int a=1;
		public List<Integer> list = Arrays.asList(new Integer[]{1,2,3,4,5});
	}
	
	

	@Override
	public void eval(IncludeDirectiveScope scope) throws EvaluationException {
		scope.a = (int) (Math.random() * 1500);
	}

	@Override
	public InputStream getDirectiveTemplateInputStream(TemplateLoader<String> loader) throws TemplateLoaderException {
		return loader.loadTemplate(context.resourceSpecification.getRootResource());
	}

	@Override
	public CompilerContext preCompile(CompilerContext context, JangularNode content) {
		this.context = context.tail();
		this.context.transcludeContent = content;
		return this.context;
	}
}
