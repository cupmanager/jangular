package net.cupmanager.jangular;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.TranslationTest.TestTranslateDirective.TestTranslateDirectiveScope;
import net.cupmanager.jangular.TranslationTest.TranslationTestController.TranslationTestControllerScope;
import net.cupmanager.jangular.annotations.Directive;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.annotations.Inject;
import net.cupmanager.jangular.annotations.Provides;
import net.cupmanager.jangular.annotations.TemplateText;
import net.cupmanager.jangular.compiler.CompilerConfiguration;
import net.cupmanager.jangular.compiler.JangularCompiler;
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
	
	public static class TranslationTestScope extends Scope {}
	
	
	public static class TranslationTestController extends AbstractController<TranslationTestControllerScope> {
		public static class TranslationTestControllerScope extends Scope {
			public Name informationalName;
		}

		@Override
		public void eval(TranslationTestControllerScope scope) {
			scope.informationalName = new Name();
		}
	}
	
	
	
	@Directive("test-translate")
	@TemplateText("{{translated}}")
	public static class TestTranslateDirective extends AbstractDirective<TestTranslateDirectiveScope> {
		public @Inject("Language") String lang;
		private JangularNode node;
		
		public static class TestTranslateDirectiveScope extends Scope {
			public @In Name name;
			public String translated;
		}

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
    	
        JangularCompiler compiler = new JangularCompiler(CompilerConfiguration.create()
        		.withDirectives(repo)
        		.withContextClass(TranslationTestEvalContext.class));
        
    	long start = System.currentTimeMillis();
    	String template = "<div j-controller=\"net.cupmanager.jangular.TranslationTest$TranslationTestController\">"+
		    "<test-translate name=\"informationalName\">Web.Page.InformationalText</test-translate>"+
		"</div>";
		JangularNode node = compiler.compile(new ByteArrayInputStream(template.getBytes()), TranslationTestScope.class);
		
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
		
		String expected = "<div>translated(Web.Page.InformationalText)</div>";
		String actual = sb.toString();
		Assert.assertEquals(expected, actual);
		System.out.println(sb);
    }
}
