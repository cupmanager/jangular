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
import net.cupmanager.jangular.compiler.caching.CachingStrategy;
import net.cupmanager.jangular.compiler.caching.CachingTemplateCompiler;
import net.cupmanager.jangular.compiler.templateloader.AbstractTemplateLoader;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;
import net.cupmanager.jangular.exceptions.AttoParseExceptionWrapper;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.ControllerNotFoundException;
import net.cupmanager.jangular.exceptions.ParseException;
import net.cupmanager.jangular.nodes.CompositeNode;
import net.cupmanager.jangular.nodes.DirectiveNode;
import net.cupmanager.jangular.nodes.JangularNode;

import org.attoparser.AttoParseException;
import org.attoparser.IAttoParser;
import org.attoparser.markup.MarkupAttoParser;
import org.attoparser.markup.MarkupParsingConfiguration;
import org.attoparser.markup.MarkupParsingConfiguration.ElementBalancing;
import org.xml.sax.SAXException;

public class ConcreteTemplateCompiler implements TemplateCompiler {
	
	public static ConcreteTemplateCompiler create() {
		return new ConcreteTemplateCompiler();
	}
	
	public static ConcreteTemplateCompiler create(CompilerConfiguration conf) {
		return new ConcreteTemplateCompiler().withConfig(conf);
	}
	
	private CompilerConfiguration conf;

	private ConcreteTemplateCompiler() {
		this.conf = CompilerConfiguration.create();
	}
	
	
	
	private ConcreteTemplateCompiler copy() {
		ConcreteTemplateCompiler jc = new ConcreteTemplateCompiler();
		jc.conf = conf;
		return jc;
	}
	
	
	public ConcreteTemplateCompiler withConfig(CompilerConfiguration conf) {
		ConcreteTemplateCompiler cc = copy();
		cc.conf = conf;
		return cc;
	}
	
	public TemplateCompiler cached(CachingStrategy cachingStrategy) {
		return new CachingTemplateCompiler(this, cachingStrategy);
	}
	
	@Override
	public CompiledTemplate compile(String templatePath) throws TemplateLoaderException, ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException {
		return compile(templatePath, Scope.class);
	}
	
	
	@Override
	public CompiledTemplate compile(String templatePath, Class<? extends Scope> scopeClass) throws TemplateLoaderException, ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException {
		AbstractTemplateLoader templateLoader = conf.getTemplateLoader();
		InputStream is = templateLoader.loadTemplate(templatePath);
		CompiledTemplate compiled = compile(is, scopeClass);
		return compiled;
	}
	
	public CompiledTemplate compile(InputStream is) throws ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException{
		return compile(is, Scope.class);
	}
	
	public CompiledTemplate compile(InputStream is, Class<? extends Scope> scopeClass) throws ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException {
		CompilerSession session = new CompilerSession(conf.getClassLoader());
		
		long start = System.currentTimeMillis();
		CompositeNode n = internalCompile(is);
		
		n.compileScope(scopeClass, conf.getContextClass(), session);
		
		long end = System.currentTimeMillis();
		
		CompiledTemplate compiledTemplate = new CompiledTemplate(n);
		compiledTemplate.setDuration(end-start, TimeUnit.MILLISECONDS);
		compiledTemplate.setWarnings(session.getWarnings());
		return compiledTemplate;
	}
	
	
	CompositeNode internalCompile(InputStream is) throws ControllerNotFoundException, ParseException {
		IAttoParser parser = new MarkupAttoParser();
		
		MarkupParsingConfiguration parserConf = new MarkupParsingConfiguration();
		parserConf.setElementBalancing(ElementBalancing.REQUIRE_BALANCED);

		CompilerMarkupHandler handler = new CompilerMarkupHandler(this, parserConf, conf.getRepo());
		try {
			parser.parse(new InputStreamReader(is), handler);
		} catch (AttoParseException e) {
			if( e instanceof AttoParseExceptionWrapper ){
				((AttoParseExceptionWrapper)e).rethrowException();
			} else {
				throw new ParseException(e);
			}
		}
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
			throw new TemplateLoaderException("Directive " + c.getName() + " doesn't have @Template or @TemplateText");
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
