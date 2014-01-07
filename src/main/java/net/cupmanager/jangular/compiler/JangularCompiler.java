package net.cupmanager.jangular.compiler;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.AbstractDirective;
import net.cupmanager.jangular.DirectiveRepository;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.Template;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.nodes.CompositeNode;
import net.cupmanager.jangular.nodes.DirectiveNode;
import net.cupmanager.jangular.nodes.ExpressionNode;
import net.cupmanager.jangular.nodes.JangularNode;

import org.attoparser.AttoParseException;
import org.attoparser.IAttoParser;
import org.attoparser.markup.MarkupAttoParser;
import org.attoparser.markup.MarkupParsingConfiguration;
import org.attoparser.markup.MarkupParsingConfiguration.ElementBalancing;
import org.xml.sax.SAXException;

public class JangularCompiler {
	private DirectiveRepository directiveRepository;
	
	public JangularCompiler(DirectiveRepository directiveRepository) {
		this.directiveRepository = directiveRepository;
	}
	
	public static class EmptyEvaluationContext extends EvaluationContext {
		
	}
	
	public CompositeNode compile(InputStream html, Class<? extends Scope> scopeClass)  throws ParserConfigurationException, SAXException, AttoParseException {
		return compile(html, scopeClass, EmptyEvaluationContext.class);
	}
	
	public CompositeNode compile(InputStream html, Class<? extends Scope> scopeClass, Class<? extends EvaluationContext> evaluationContextClass) 
			throws ParserConfigurationException, SAXException, AttoParseException {
		
		CompilerSession session = new CompilerSession();
		CompositeNode n = internalCompile(html);
		try {
			n.compileScope(scopeClass, evaluationContextClass, session);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		session.printWarnings();
		
		return n;
	}
	
	CompositeNode internalCompile(InputStream html) throws ParserConfigurationException, SAXException, AttoParseException {
		IAttoParser parser = new MarkupAttoParser();
		
		MarkupParsingConfiguration conf = new MarkupParsingConfiguration();
		conf.setElementBalancing(ElementBalancing.REQUIRE_BALANCED);

		CompilerMarkupHandler handler = new CompilerMarkupHandler(this, conf, directiveRepository);
		parser.parse(new InputStreamReader(html), handler);
		
		CompositeNode n = handler.getNode();
		n.optimize();
		return n;
	}
	
	public DirectiveNode getDirectiveNode(String name, Map<String, String> attributes, JangularNode content) {
		Class<? extends AbstractDirective<?>> c = directiveRepository.get(name);
		return getDirectiveNode(c, attributes, content);
	}
	
	public DirectiveNode getDirectiveNode(Class<? extends AbstractDirective<?>> c, Map<String, String> attributes, JangularNode content) {
		
		String template = c.getAnnotation(Template.class).value();
		
		try {
			JangularNode templateNode = null;
			if( template.matches("\\{\\{(.*?)\\}\\}")) {
				templateNode = new ExpressionNode(template.substring(2, template.length()));
			} else {
				templateNode =	internalCompile(new FileInputStream(template));
			}
			
			AbstractDirective<?> directiveInstance = c.newInstance();
			
			directiveInstance.compile(attributes, templateNode);
			
			return new DirectiveNode(directiveInstance, templateNode, attributes);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
