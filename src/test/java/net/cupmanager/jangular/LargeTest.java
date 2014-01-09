package net.cupmanager.jangular;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.annotations.Provides;
import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.CompilerConfiguration;
import net.cupmanager.jangular.compiler.JangularCompiler;
import net.cupmanager.jangular.compiler.templateloader.FileTemplateLoader;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.util.GuavaCompilerCache;
import net.cupmanager.jangular.util.InlineTranslationDirective;
import net.cupmanager.jangular.util.MatchTableDirective;

import org.attoparser.AttoParseException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class LargeTest {
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
    public void pleaseDontCrash() throws FileNotFoundException, ParserConfigurationException, SAXException, AttoParseException, TemplateLoaderException
    {
		DirectiveRepository repo = new DirectiveRepository();
		repo.register(MatchTableDirective.class);
		repo.register(InlineTranslationDirective.class);
		
		CompilerConfiguration conf = CompilerConfiguration.create()
				.withDirectives(repo)
				.withTemplateLoader(new FileTemplateLoader("templates/test", "templates/test/directives"))
				.withContextClass(AppEvalContext.class);
		
		conf = conf.withCache(new GuavaCompilerCache());
		
        JangularCompiler compiler = new JangularCompiler(conf);
        
		CompiledTemplate template = compiler.compile("largetest.html", AppScope.class);
		
		/* These will hit the cache immediately */
		template = compiler.compile("largetest.html", AppScope.class);
		template = compiler.compile("largetest.html", AppScope.class);
		template = compiler.compile("largetest.html", AppScope.class);
		template = compiler.compile("largetest.html", AppScope.class);
		
		System.out.println("Compile took " + template.getCompileDuration(TimeUnit.MILLISECONDS) + " ms");
		template.printWarnings();
		
		// ----------
		
		AppEvalContext context = new AppEvalContext();
		context.greeting = "Hejsan";
		context.URL = "http://localhost/wahatever";
		
		AppScope scope = createScope();
		
		StringBuilder sb = new StringBuilder();
		
		// Warm-up
		for( int i = 0; i < 100; i++ ) {
			sb = new StringBuilder();
			template.eval(scope, sb, context);
		}
		
		// Benchmark! 
		long start = System.currentTimeMillis();
		int times = 100;
		for( int i = 0; i < times; i++ ) {
			sb = new StringBuilder();
			template.eval(scope, sb, context);
		}
		long end = System.currentTimeMillis();
		
		
		System.out.println(sb);
		System.out.println( (end-start)/(float)times + " ms per template" );
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
