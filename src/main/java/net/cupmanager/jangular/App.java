package net.cupmanager.jangular;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import net.cupmanager.jangular.nodes.JangularNode;
import net.cupmanager.jangular.testing.MatchTableDirective;

public class App {
	public static class Item {
		public String title = "Title";
		public List<Integer> nrs = new ArrayList<Integer>();
	}
	
	public static class AppScope extends Scope {
		public Object items;
	}
	
    public static void main( String[] args )
    {
    	
    	
    	DirectiveRepository repo = new DirectiveRepository();
    	repo.register(MatchTableDirective.class);
    	
        Compiler compiler = new Compiler(repo);
        
        try {
        	long start = System.currentTimeMillis();
			JangularNode node = compiler.compile(new FileInputStream("test.html"),AppScope.class);
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
			
			
			for( int i = 0; i < 500; i++ ) {
				sb = new StringBuilder();
				node.eval(scope, sb);
			}
			
			start = System.currentTimeMillis();
			int times = 100;
			for( int i = 0; i < times; i++ ) {
				sb = new StringBuilder();
				node.eval(scope, sb);
			}
			
			end = System.currentTimeMillis();
			
			
			System.out.println(sb);
			System.out.println( (end-start)/(float)times + " ms per template" );
			
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
}
