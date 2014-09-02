package net.cupmanager.jangular;

import java.util.concurrent.TimeUnit;

import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.CompilerConfiguration;
import net.cupmanager.jangular.compiler.TemplateCompiler;
import net.cupmanager.jangular.compiler.caching.GuavaCachingStrategy;
import net.cupmanager.jangular.compiler.templateloader.FileTemplateLoader;
import net.cupmanager.jangular.exceptions.CompileException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.util.IncludeDirective;
import net.cupmanager.jangular.util.QueueResourceSpecification;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;

public class IncludeTest {
	
	public static class AppScope extends Scope {
		public int i = 5;
	}
	
	
	@Test
    public void pleaseDontCrash() throws CompileException, EvaluationException
    {
		DirectiveRepository repo = new DirectiveRepository();
		repo.register(IncludeDirective.class);
		
		CompilerConfiguration conf = CompilerConfiguration.create()
				.withDirectives(repo)
				.withTemplateLoader(new FileTemplateLoader("templates/includetest"))
				.withDirectiveTemplateLoader(new FileTemplateLoader("templates/includetest/directives"))
				.withCaching(new GuavaCachingStrategy(CacheBuilder.newBuilder().maximumSize(1000)));
		
        TemplateCompiler compiler = TemplateCompiler.Builder.create(conf);
        
		CompiledTemplate template = compiler.compile(new QueueResourceSpecification("index.html", "section.html", "view.html"), AppScope.class);
		
		System.out.println("Compile took " + template.getCompileDuration(TimeUnit.MILLISECONDS) + " ms");
		template.printWarnings();
		
		// ----------
		
		StringBuilder sb = new StringBuilder();
		template.eval(createScope(), sb);
		System.out.println(sb);
    }

	private static AppScope createScope() {
		AppScope scope = new AppScope();
		return scope;
	}
	
}
