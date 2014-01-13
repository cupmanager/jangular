package net.cupmanager.jangular;

import java.io.ByteArrayInputStream;

import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.ConcreteTemplateCompiler;
import net.cupmanager.jangular.exceptions.CompileException;
import net.cupmanager.jangular.exceptions.EvaluationException;

import org.junit.Assert;
import org.junit.Test;


public class MinimalTest {
	
	@Test
	public void basic() throws CompileException, EvaluationException {
		String html = "<div>{{1+1}} == 2</div>";
		
		CompiledTemplate template = ConcreteTemplateCompiler.create()
			.compile(new ByteArrayInputStream(html.getBytes()));
		
		StringBuilder sb = new StringBuilder();
		template.eval(sb);
		String result = sb.toString();
		
		Assert.assertEquals(result, "<div>2 == 2</div>");
	}
}
