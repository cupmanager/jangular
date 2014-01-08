package net.cupmanager.jangular;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.annotations.Provides;
import net.cupmanager.jangular.compiler.JangularCompiler;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.nodes.JangularNode;
import net.cupmanager.jangular.util.InlineTranslationDirective;

import org.attoparser.AttoParseException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class InlineTranslationTest {
	
	public static class TranslationTestScope extends Scope {}
	
	public static class TranslationTestEvalContext extends EvaluationContext {
		public @Provides("Language") String lang = "en";
	}
	
	@Test
    public void main() throws FileNotFoundException, ParserConfigurationException, SAXException, AttoParseException
    {
    	DirectiveRepository repo = new DirectiveRepository();
    	repo.register(InlineTranslationDirective.class);
    	
        JangularCompiler compiler = new JangularCompiler(repo);
        
    	long start = System.currentTimeMillis();
    	String template = "<div>"+
		    "{{1+2}} [['Key.For.Translation']] {{2+2}} yes!"+
		"</div>";
		JangularNode node = compiler.compile(new ByteArrayInputStream(template.getBytes()), TranslationTestScope.class, TranslationTestEvalContext.class);
		
		long end = System.currentTimeMillis();
		System.out.println("Compile took " + (end-start) + " ms");
		
		
		List<Integer> nrs = new ArrayList<Integer>();
		for( int i = 0; i < 10; i++ ) {
			nrs.add(i+1);
		}
		
		
		TranslationTestScope scope = new TranslationTestScope();
		TranslationTestEvalContext context = new TranslationTestEvalContext();
		
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder();
		node.eval(scope, sb, context);
		
		end = System.currentTimeMillis();
		
		String expected = "<div>3 translated(Key.For.Translation) 4 yes!</div>";
		String actual = sb.toString();
		Assert.assertEquals(expected, actual);
		System.out.println(sb);
    }
}
