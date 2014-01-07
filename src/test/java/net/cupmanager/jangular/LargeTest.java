package net.cupmanager.jangular;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.annotations.Provides;
import net.cupmanager.jangular.compiler.JangularCompiler;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.nodes.JangularNode;
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
    public void main() throws FileNotFoundException, ParserConfigurationException, SAXException, AttoParseException
    {
		DirectiveRepository repo = new DirectiveRepository();
    	repo.register(MatchTableDirective.class);
    	repo.register(InlineTranslationDirective.class);
    	
        JangularCompiler compiler = new JangularCompiler(repo);
        
    	long start = System.currentTimeMillis();
		JangularNode node = compiler.compile(new FileInputStream("templates/test/largetest.html"), AppScope.class, AppEvalContext.class);
		
		long end = System.currentTimeMillis();
		System.out.println("Compile took " + (end-start) + " ms");
		
		
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
		StringBuilder sb = new StringBuilder();
		
		AppEvalContext context = new AppEvalContext();
		context.greeting = "Hejsan";
		context.URL = "http://localhost/wahatever";
		
		for( int i = 0; i < 1000; i++ ) {
			sb = new StringBuilder();
			node.eval(scope, sb, context);
		}
		
		start = System.currentTimeMillis();
		int times = 100;
		for( int i = 0; i < times; i++ ) {
			sb = new StringBuilder();
			node.eval(scope, sb, context);
		}
		
		end = System.currentTimeMillis();
		
		
//		System.out.println(sb);
		System.out.println( (end-start)/(float)times + " ms per template" );
    }
}
