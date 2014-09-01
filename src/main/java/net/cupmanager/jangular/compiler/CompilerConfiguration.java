package net.cupmanager.jangular.compiler;

import net.cupmanager.jangular.DirectiveRepository;
import net.cupmanager.jangular.JangularClassLoader;
import net.cupmanager.jangular.compiler.caching.CachingStrategy;
import net.cupmanager.jangular.compiler.caching.NullCachingStrategy;
import net.cupmanager.jangular.compiler.templateloader.NullTemplateLoader;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoader;
import net.cupmanager.jangular.injection.EvaluationContext;

public class CompilerConfiguration {
	public static CompilerConfiguration create() {
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.init();
		return cc;
	}
	
	public static class EmptyEvaluationContext extends EvaluationContext {}
	
	private DirectiveRepository repo;
	private TemplateLoader templateLoader;
	private TemplateLoader directiveTemplateLoader;
	private Class<? extends EvaluationContext> contextClass;
	private ClassLoader classLoader;
	private CachingStrategy cachingStrategy;
	
	private CompilerConfiguration() {}
	
	private void init() {
		this.repo = new DirectiveRepository();
		this.templateLoader = new NullTemplateLoader();
		this.directiveTemplateLoader = null;
		this.contextClass = EmptyEvaluationContext.class;
		this.cachingStrategy = new NullCachingStrategy();
		this.classLoader = new JangularClassLoader();
	}
	
	private CompilerConfiguration copy() {
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.repo = repo;
		cc.templateLoader = templateLoader;
		cc.directiveTemplateLoader = directiveTemplateLoader;
		cc.contextClass = contextClass;
		cc.cachingStrategy = cachingStrategy;
		cc.classLoader = classLoader;
		return cc;
	}
	

	public CompilerConfiguration withDirectives(DirectiveRepository repo) {
		CompilerConfiguration cc = copy();
		cc.repo = repo;
		return cc;
	}
	
	public CompilerConfiguration withCaching(CachingStrategy cachingStrategy) {
		CompilerConfiguration cc = copy();
		cc.cachingStrategy = cachingStrategy;
		return cc;
	}
	
	public CompilerConfiguration withTemplateLoader(TemplateLoader templateLoader) {
		CompilerConfiguration cc = copy();
		cc.templateLoader = templateLoader;
		return cc;
	}
	public CompilerConfiguration withDirectiveTemplateLoader(TemplateLoader templateLoader) {
		CompilerConfiguration cc = copy();
		cc.directiveTemplateLoader = templateLoader;
		return cc;
	}
	
	public CompilerConfiguration withContextClass(Class<? extends EvaluationContext> contextClass) {
		CompilerConfiguration cc = copy();
		cc.contextClass = contextClass;
		return cc;
	}
	
	public CompilerConfiguration withClassLoader(ClassLoader classLoader) {
		CompilerConfiguration cc = copy();
		cc.classLoader = classLoader;
		return cc;
	}
	
	
	
	public TemplateLoader getTemplateLoader() {
		return templateLoader;
	}
	public TemplateLoader getDirectiveTemplateLoader() {
		return directiveTemplateLoader != null 
				? directiveTemplateLoader : templateLoader;
	}
	
	public CachingStrategy getCachingStrategy() {
		return cachingStrategy;
	}
	
	public DirectiveRepository getRepo() {
		return repo;
	}

	public Class<? extends EvaluationContext> getContextClass() {
		return contextClass;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
}
