package net.cupmanager.jangular.compiler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.AbstractDirective;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.Template;
import net.cupmanager.jangular.annotations.TemplateText;
import net.cupmanager.jangular.compiler.templateloader.AbstractTemplateLoader;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;
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
	
	private CompilerConfiguration conf;

	public JangularCompiler(CompilerConfiguration conf) {
		this.conf = conf;
	}
	
	public CompiledTemplate compile(String templatePath) throws ParserConfigurationException, SAXException, AttoParseException, TemplateLoaderException {
		return compile(templatePath, Scope.class);
	}
	
	public CompiledTemplate compile(String templatePath, Class<? extends Scope> scopeClass) throws ParserConfigurationException, SAXException, AttoParseException, TemplateLoaderException {
		CompiledTemplate cached = conf.getCache().get(templatePath);
		if (cached != null) {
			return cached;
		} else {
			AbstractTemplateLoader templateLoader = conf.getTemplateLoader();
			InputStream is = templateLoader.loadTemplate(templatePath);
			CompiledTemplate compiled = compile(is, scopeClass);
			conf.getCache().save(templatePath, compiled);
			return compiled;
		}
	}
	
	
	public CompiledTemplate compile(InputStream is)  throws ParserConfigurationException, SAXException, AttoParseException {
		return compile(is, Scope.class);
	}
	public CompiledTemplate compile(InputStream is, Class<? extends Scope> scopeClass)  throws ParserConfigurationException, SAXException, AttoParseException {
		CompilerSession session = new CompilerSession(conf.getClassLoader());
		
		long start = System.currentTimeMillis();
		CompositeNode n = internalCompile(is);
		try {
			n.compileScope(scopeClass, conf.getContextClass(), session);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		long end = System.currentTimeMillis();
		
		CompiledTemplate compiledTemplate = new CompiledTemplate(n);
		compiledTemplate.setDuration(end-start, TimeUnit.MILLISECONDS);
		compiledTemplate.setWarnings(session.getWarnings());
		return compiledTemplate;
	}
	
	
	CompositeNode internalCompile(InputStream is) throws ParserConfigurationException, SAXException, AttoParseException {
		IAttoParser parser = new MarkupAttoParser();
		
		MarkupParsingConfiguration parserConf = new MarkupParsingConfiguration();
		parserConf.setElementBalancing(ElementBalancing.REQUIRE_BALANCED);

		CompilerMarkupHandler handler = new CompilerMarkupHandler(this, parserConf, conf.getRepo());
		parser.parse(new InputStreamReader(is), handler);
		
		CompositeNode n = handler.getNode();
		n.optimize();
		return n;
	}
	
	public DirectiveNode getDirectiveNode(String name, Map<String, String> attributes, JangularNode content) {
		Class<? extends AbstractDirective<?>> c = conf.getRepo().get(name);
		return getDirectiveNode(c, attributes, content);
	}
	
	private InputStream getDirectiveTemplateInputStream(Class<? extends AbstractDirective<?>> c) throws TemplateLoaderException {
		Template templateAnnotation = c.getAnnotation(Template.class);
		TemplateText templateTextAnnotation = c.getAnnotation(TemplateText.class);
		
		if (templateAnnotation != null) {
			String template = templateAnnotation.value();
			return conf.getTemplateLoader().loadDirectiveTemplate(template);
			
		} else if (templateTextAnnotation != null) {
			String templateText = templateTextAnnotation.value();
			return new ByteArrayInputStream(templateText.getBytes());
			
		} else {
			throw new RuntimeException("Directive " + c.getName() + " doesn't have @Template or @TemplateText");
		}
	}
	
	public DirectiveNode getDirectiveNode(Class<? extends AbstractDirective<?>> c, Map<String, String> attributes, JangularNode content) {
		try {
			InputStream is = getDirectiveTemplateInputStream(c);
			JangularNode templateNode =	internalCompile(is);
			
			AbstractDirective<?> directiveInstance = c.newInstance();
			
			directiveInstance.compile(attributes, templateNode, content);
			
			return new DirectiveNode(directiveInstance, templateNode, attributes);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
