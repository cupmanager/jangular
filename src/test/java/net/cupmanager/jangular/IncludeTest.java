package net.cupmanager.jangular;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.cupmanager.jangular.annotations.Provides;
import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.CompilerConfiguration;
import net.cupmanager.jangular.compiler.TemplateCompiler;
import net.cupmanager.jangular.compiler.caching.GuavaCachingStrategy;
import net.cupmanager.jangular.compiler.templateloader.FileTemplateLoader;
import net.cupmanager.jangular.exceptions.CompileException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.util.IncludeDirective;
import net.cupmanager.jangular.util.InlineTranslationDirective;
import net.cupmanager.jangular.util.MatchTableDirective;
import net.cupmanager.jangular.util.QueueResourceSpecification;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;

public class IncludeTest {
	public static class Item {
		public String title = "Title";
		public List<Integer> nrs = new ArrayList<Integer>();
	}
	
	public static class AppScope extends Scope {
		public List<Item> items;
		public int i = 5;
	}
	
	
	public static class AppEvalContext extends EvaluationContext {
		public @Provides("Greeting") String greeting;
		public @Provides("URL") String URL;
		public @Provides String defaultString = "default string";
		
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
		List<Integer> nrs = new ArrayList<Integer>();
		for( int i = 0; i < 10; i++ ) {
			nrs.add(i+1);
		}
		
		final List<Item> _items = new ArrayList<Item>();
		for( int i = 0; i < 100; i++ ) {
			Item item = new Item();
			item.nrs = nrs;
			_items.add(item);
		}
		
		
		AppScope scope = new AppScope();
		scope.items = _items;
		return scope;
	}
	
}
