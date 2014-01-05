package net.cupmanager.jangular;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Hello world!
 *
 */
public class VelocityTest 
{
	public static class Item {
		public String title = "Title";
		public List<Integer> nrs;
		
		public String getTitle() {
			return title;
		}
		public List<Integer> getNrs() {
			return nrs;
		}
	}
	
    public static void main( String[] args )
    {
        try {
			List<Integer> nrs = new ArrayList<Integer>();
			for( int i = 0; i < 10; i++ ) {
				nrs.add(i+1);
			}
			
			final List<Item> items = new ArrayList<Item>();
			for( int i = 0; i < 100; i++ ) {
				Item item = new Item();
				item.nrs = nrs;
				items.add(item);
			}
			
			
			
			VelocityEngine ve = new VelocityEngine();
			ve.init();
			
			Template t = ve.getTemplate("test.vm");
			
			VelocityContext ctx = new VelocityContext();
			ctx.put("items", items);
			
			
			StringWriter writer = new StringWriter();
			
			long start = System.currentTimeMillis();
			for( int i = 0; i < 1000; i++ ) {
				
				writer = new StringWriter();
				t.merge(ctx, writer);
				
			}
			
			long end =System.currentTimeMillis();
			
			System.out.println(writer);
			System.err.println( (end-start)/1000.0 + " ms per template" );

			
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
}
