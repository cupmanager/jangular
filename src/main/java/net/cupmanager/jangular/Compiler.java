package net.cupmanager.jangular;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.App.AppScope;
import net.cupmanager.jangular.nodes.CompositeNode;

import org.attoparser.AttoParseException;
import org.attoparser.IAttoParser;
import org.attoparser.markup.MarkupAttoParser;
import org.attoparser.markup.MarkupParsingConfiguration;
import org.attoparser.markup.MarkupParsingConfiguration.ElementBalancing;
import org.xml.sax.SAXException;

public class Compiler {
	
	
	private DirectiveRepository directiveRepository;
	
	public Compiler(DirectiveRepository directiveRepository) {
		this.directiveRepository = directiveRepository;
	}
	

	public CompositeNode compile(InputStream html, Class<? extends Scope> scopeClass) throws ParserConfigurationException, SAXException {
		CompositeNode n = internalCompile(html);
		try {
			n.compileScope(scopeClass);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return n;
	}
	

	CompositeNode internalCompile(InputStream html) throws ParserConfigurationException, SAXException {
		IAttoParser parser = new MarkupAttoParser();
		
		MarkupParsingConfiguration conf = new MarkupParsingConfiguration();
		conf.setElementBalancing(ElementBalancing.REQUIRE_BALANCED);

		MyMarkupHandler handler = new MyMarkupHandler(conf, directiveRepository);
		
		try {
			parser.parse(new InputStreamReader(html), handler);
		} catch (AttoParseException e) {
			e.printStackTrace();
		}
		
		CompositeNode n = handler.getNode();
		n.optimize();
		return n;
	}
	
	
	
	
	
	
	
	
	
	public static <T> Class<T> loadScopeClass(byte[] b, String className) {
		
		Class<T> clazz = null;
		try {
			ClassLoader loader = ClassLoader.getSystemClassLoader();
			Class<ClassLoader> cls = (Class<ClassLoader>) Class.forName("java.lang.ClassLoader");
			java.lang.reflect.Method method = cls.getDeclaredMethod(
					"defineClass", new Class[] { String.class, byte[].class,
							int.class, int.class });

			
			method.setAccessible(true);
			try {
				Object[] args = new Object[] { className, b, new Integer(0),
						new Integer(b.length) };
				clazz = (Class<T>) method.invoke(loader, args);
			} finally {
				method.setAccessible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return (Class<T>) clazz;
	}
	
	

}
