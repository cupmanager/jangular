package net.cupmanager.jangular;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.ConcreteTemplateCompiler;

import org.attoparser.AttoParseException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;


public class MinimalTest {
	
	@Test
	public void basic() throws ParserConfigurationException, SAXException, AttoParseException {
		String html = "<div>{{1+1}} == 2</div>";
		
		CompiledTemplate template = ConcreteTemplateCompiler.create()
			.compile(new ByteArrayInputStream(html.getBytes()));
		
		StringBuilder sb = new StringBuilder();
		template.eval(sb);
		String result = sb.toString();
		
		Assert.assertEquals(result, "<div>2 == 2</div>");
	}
}
