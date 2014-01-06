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
	
	public DirectiveNode getDirectiveNode(String name, Map<String, String> attributesObject, JangularNode node) {
		Class<? extends AbstractDirective<?>> c = directiveRepository.get(name);
		String templateFile = c.getAnnotation(Template.class).value();
		
		try {
			CompositeNode compositeNode = internalCompile(new FileInputStream(templateFile));
			
			AbstractDirective<?> directiveInstance = c.newInstance();
			
			directiveInstance.compile(attributesObject, compositeNode);
			
			return new DirectiveNode(directiveInstance, compositeNode, attributesObject);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
