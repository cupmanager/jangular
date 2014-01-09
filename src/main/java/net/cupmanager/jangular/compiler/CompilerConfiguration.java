package net.cupmanager.jangular.compiler;

import net.cupmanager.jangular.DirectiveRepository;
import net.cupmanager.jangular.JangularClassLoader;
import net.cupmanager.jangular.compiler.templateloader.AbstractTemplateLoader;
import net.cupmanager.jangular.compiler.templateloader.NullTemplateLoader;
import net.cupmanager.jangular.injection.EvaluationContext;

public class CompilerConfiguration {
	public static CompilerConfiguration create() {
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.init();
		return cc;
	}
	
	public static class EmptyEvaluationContext extends EvaluationContext {}
	
	private DirectiveRepository repo;
	private AbstractTemplateLoader templateLoader;
	private Class<? extends EvaluationContext> contextClass;
	private ClassLoader classLoader;
	private CompilerCache cache;
	
	private CompilerConfiguration() {}
	
	private void init() {
		this.repo = new DirectiveRepository();
		this.templateLoader = new NullTemplateLoader();
		this.contextClass = EmptyEvaluationContext.class;
		this.classLoader = new JangularClassLoader();
		this.cache = new NullCompilerCache();
	}
	
	private CompilerConfiguration copy() {
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.repo = repo;
		cc.templateLoader = templateLoader;
		cc.contextClass = contextClass;
		cc.classLoader = classLoader;
		cc.cache = cache;
		return cc;
	}
	
	
	public CompilerConfiguration withDirectives(DirectiveRepository repo) {
		CompilerConfiguration cc = copy();
		cc.repo = repo;
		return cc;
	}
	
	public CompilerConfiguration withTemplateLoader(AbstractTemplateLoader templateLoader) {
		CompilerConfiguration cc = copy();
		cc.templateLoader = templateLoader;
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
	
	public CompilerConfiguration withCache(CompilerCache cache) {
		CompilerConfiguration cc = copy();
		cc.cache = cache;
		return cc;
	}
	
	
	
	
	
	public AbstractTemplateLoader getTemplateLoader() {
		return templateLoader;
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
	
	public CompilerCache getCache() {
		return cache;
	}
	
}
