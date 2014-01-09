package net.cupmanager.jangular;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.annotations.Provides;
import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.CompilerConfiguration;
import net.cupmanager.jangular.compiler.ConcreteTemplateCompiler;
import net.cupmanager.jangular.compiler.TemplateCompiler;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.util.InlineTranslationDirective;

import org.attoparser.AttoParseException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class InlineTranslationTest {
	
	public static class TranslationTestScope extends Scope {}
	
	public static class TranslationTestEvalContext extends EvaluationContext {
		public @Provides("Language") String lang = "english";
	}
	
	@Test
    public void main() throws FileNotFoundException, ParserConfigurationException, SAXException, AttoParseException
    {
    	DirectiveRepository repo = new DirectiveRepository();
    	repo.register(InlineTranslationDirective.class);
    	
        TemplateCompiler compiler = ConcreteTemplateCompiler.create()
    		.withConfig(CompilerConfiguration.create()
        		.withDirectives(repo)
        		.withContextClass(TranslationTestEvalContext.class));
        
    	String html = "<div>"+
		    "{{1+2}} [['Key.For.Translation']] {{2+2}} yes!"+
		"</div>";
		CompiledTemplate template = compiler.compile(new ByteArrayInputStream(html.getBytes()), TranslationTestScope.class);
		System.out.println("Compile took " + template.getCompileDuration(TimeUnit.MILLISECONDS) + " ms");
		
		TranslationTestScope scope = new TranslationTestScope();
		TranslationTestEvalContext context = new TranslationTestEvalContext();
		
		StringBuilder sb = new StringBuilder();
		template.eval(scope, sb, context);
		
		String expected = "<div>3 translated(english,Key.For.Translation) 4 yes!</div>";
		String actual = sb.toString();
		Assert.assertEquals(expected, actual);
		System.out.println(sb);
    }
}
