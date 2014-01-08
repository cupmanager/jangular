package net.cupmanager.jangular;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.compiler.CompilerConfiguration;
import net.cupmanager.jangular.compiler.JangularCompiler;
import net.cupmanager.jangular.nodes.JangularNode;

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
		public List<Item> items = new ArrayList<>();
	}
	
	private static RepeatTestScope createScope() {
		RepeatTestScope scope = new RepeatTestScope();
		scope.items.add(new Item("Orange"));
		scope.items.add(new Item("Apple"));
		scope.items.add(new Item("Banana"));
		return scope;
	}
	
	@Test
	public void basic() throws ParserConfigurationException, SAXException, AttoParseException {
		String template = "<div j-repeat=\"item in items\">{{$index}}: {{item.title}}</div>";
		JangularNode node = new JangularCompiler(CompilerConfiguration.create())
			.compile(new ByteArrayInputStream(template.getBytes()), RepeatTestScope.class);
		
		StringBuilder sb = new StringBuilder();
		node.eval(createScope(), sb);
		String html = sb.toString();
		
		Assert.assertEquals(html, "<div>0: Orange</div><div>1: Apple</div><div>2: Banana</div>");
	}
	
	@Test
	public void asTag() throws ParserConfigurationException, SAXException, AttoParseException {
		String template = "<j-repeat for=\"item in items\">{{$index}}: {{item.title}}</j-repeat>";
		JangularNode node = new JangularCompiler(CompilerConfiguration.create())
			.compile(new ByteArrayInputStream(template.getBytes()), RepeatTestScope.class);
		
		StringBuilder sb = new StringBuilder();
		node.eval(createScope(), sb);
		String html = sb.toString();
		
		Assert.assertEquals(html, "0: Orange1: Apple2: Banana");
	}
}
