package net.cupmanager.jangular;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

import net.cupmanager.jangular.annotations.Template;
import net.cupmanager.jangular.annotations.TemplateText;
import net.cupmanager.jangular.compiler.CompilerContext;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoader;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.nodes.JangularNode;

public abstract class AbstractDirective<T extends Scope> {
	public void compile(Map<String, String> attributesObject, JangularNode templateNode, JangularNode contentNode) {
	}
	
	public Class<? extends Scope> getScopeClass() {
		return getScopeClass(this.getClass());
	}
	
	public static Class<? extends Scope> getScopeClass(Class<? extends AbstractDirective> directiveClass) {
		Method[] methods = directiveClass.getMethods();
		for (Method m : methods) {
			if ("eval".equals(m.getName())) {
				if (m.getParameterTypes()[0] != Scope.class) {
					return (Class<? extends Scope>) m.getParameterTypes()[0];
				}
			}
		}
		return null;
	}
	
	public void eval(T scope) throws EvaluationException {
		
	}

	public InputStream getDirectiveTemplateInputStream(TemplateLoader<String> loader) throws TemplateLoaderException {
		Template templateAnnotation = getClass().getAnnotation(Template.class);
		TemplateText templateTextAnnotation = getClass().getAnnotation(TemplateText.class);
		
		if (templateAnnotation != null) {
			String template = templateAnnotation.value();
			return loader.loadTemplate(template);
			
		} else if (templateTextAnnotation != null) {
			String templateText = templateTextAnnotation.value();
			return new ByteArrayInputStream(templateText.getBytes());
			
		} else {
			throw new TemplateLoaderException("Directive " + getClass().getName() + " doesn't have @Template or @TemplateText");
		}
	}

	public CompilerContext preCompile(CompilerContext context, JangularNode content) {
		CompilerContext newContext = context.clone();
		newContext.transcludeContent = content;
		return newContext;
	}
}
