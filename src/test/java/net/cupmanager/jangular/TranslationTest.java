package net.cupmanager.jangular;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.annotations.Context;
import net.cupmanager.jangular.annotations.Directive;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.annotations.Provides;
import net.cupmanager.jangular.annotations.TemplateText;
import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.CompilerConfiguration;
import net.cupmanager.jangular.compiler.ConcreteTemplateCompiler;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.nodes.JangularNode;

import org.attoparser.AttoParseException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TranslationTest {
	
	public static class Name {
		public String translate(String key) {
			return "translated("+key+")";
		}
	}
	
	
	// Have to put these nested classes outside (actually wanted this inside the TranslationTestController but 'mvn test' bails)
	public static class TranslationTestControllerScope extends Scope {
		public Name informationalName;
	}
	public static class TranslationTestController extends AbstractController<TranslationTestControllerScope> {

		@Override
		public void eval(TranslationTestControllerScope scope) {
			scope.informationalName = new Name();
		}
	}
	
	
	
	// Have to put these nested classes outside (actually wanted this inside the TranslationTestController but 'mvn test' bails)
	public static class TestTranslateDirectiveScope extends Scope {
		public @In Name name;
		public String translated;
	}
	
	@Directive("test-translate")
	@TemplateText("{{translated}}")
	public static class TestTranslateDirective extends AbstractDirective<TestTranslateDirectiveScope> {
		public @Context("Language") String lang;
		private JangularNode node;
		

		@Override
		public void compile(Map<String, String> attributesObject, JangularNode templateNode, JangularNode contentNode) {
			this.node = contentNode;
		}

		@Override
		public void eval(TestTranslateDirectiveScope scope) {
			StringBuilder sb = new StringBuilder();
			node.eval(scope, sb);
			String key = sb.toString();
			
			scope.translated = scope.name.translate(key);
		}
	}
	
	public static class TranslationTestEvalContext extends EvaluationContext {
		public @Provides("Language") String lang = "en";
	}
	
	@Test
    public void main() throws FileNotFoundException, ParserConfigurationException, SAXException, AttoParseException
    {
    	DirectiveRepository repo = new DirectiveRepository();
    	repo.register(TestTranslateDirective.class);
    	
        ConcreteTemplateCompiler compiler = ConcreteTemplateCompiler.create(
        		CompilerConfiguration.create()
        			.withDirectives(repo)
        			.withContextClass(TranslationTestEvalContext.class));
        
    	String template = "<div j-controller=\"net.cupmanager.jangular.TranslationTest$TranslationTestController\">"+
		    "<test-translate name=\"informationalName\">Web.Page.InformationalText</test-translate>"+
		"</div>";
		CompiledTemplate compiled = compiler.compile(new ByteArrayInputStream(template.getBytes()));
		System.out.println("Compile took " + compiled.getCompileDuration(TimeUnit.MILLISECONDS) + " ms");
		
		
		TranslationTestEvalContext context = new TranslationTestEvalContext();
		
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder();
		compiled.eval(sb, context);
		
		
		String expected = "<div>translated(Web.Page.InformationalText)</div>";
		String actual = sb.toString();
		Assert.assertEquals(expected, actual);
		System.out.println(sb);
    }
}
