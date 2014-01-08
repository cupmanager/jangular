package net.cupmanager.jangular;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.compiler.CompilerConfiguration;
import net.cupmanager.jangular.compiler.JangularCompiler;
import net.cupmanager.jangular.nodes.JangularNode;

import org.attoparser.AttoParseException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;


public class MinimalTest {
	
	@Test
	public void basic() throws ParserConfigurationException, SAXException, AttoParseException {
		String template = "<div>{{1+1}} == 2</div>";
		
		JangularNode node = new JangularCompiler(CompilerConfiguration.create())
			.compile(new ByteArrayInputStream(template.getBytes()));
		
		StringBuilder sb = new StringBuilder();
		node.eval(sb);
		String html = sb.toString();
		
		Assert.assertEquals(html, "<div>2 == 2</div>");
	}
}
