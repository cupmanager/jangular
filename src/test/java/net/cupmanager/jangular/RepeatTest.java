package net.cupmanager.jangular;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.ConcreteTemplateCompiler;
import net.cupmanager.jangular.exceptions.CompileException;
import net.cupmanager.jangular.exceptions.EvaluationException;

import org.attoparser.AttoParseException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;


public class RepeatTest {
	
	public static class Item {
		public String title;
		public Item(String t) { title=t; }
	}
	public static class RepeatTestScope extends Scope {
		public List<Item> items = new ArrayList<Item>();
	}
	
	private static RepeatTestScope createScope() {
		RepeatTestScope scope = new RepeatTestScope();
		scope.items.add(new Item("Orange"));
		scope.items.add(new Item("Apple"));
		scope.items.add(new Item("Banana"));
		return scope;
	}
	
	@Test
	public void basic() throws CompileException, EvaluationException {
		String template = "<div j-repeat=\"item in items\">{{$index}}: {{item.title}}</div>";
		CompiledTemplate compiled = ConcreteTemplateCompiler.create()
			.compile(new ByteArrayInputStream(template.getBytes()), RepeatTestScope.class);
		
		StringBuilder sb = new StringBuilder();
		compiled.eval(createScope(), sb);
		String html = sb.toString();
		
		Assert.assertEquals(html, "<div>0: Orange</div><div>1: Apple</div><div>2: Banana</div>");
	}
	
	@Test
	public void asTag() throws CompileException, EvaluationException {
		String template = "<j-repeat for=\"item in items\">{{$index}}: {{item.title}}</j-repeat>";
		CompiledTemplate compiled = ConcreteTemplateCompiler.create()
			.compile(new ByteArrayInputStream(template.getBytes()), RepeatTestScope.class);
		
		StringBuilder sb = new StringBuilder();
		compiled.eval(createScope(), sb);
		String html = sb.toString();
		
		Assert.assertEquals(html, "0: Orange1: Apple2: Banana");
	}
}
