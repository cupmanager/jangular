package net.cupmanager.jangular;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.nodes.CompositeNode;

import org.apache.commons.lang.ClassUtils;
import org.attoparser.AttoParseException;
import org.attoparser.IAttoParser;
import org.attoparser.markup.MarkupAttoParser;
import org.attoparser.markup.MarkupParsingConfiguration;
import org.attoparser.markup.MarkupParsingConfiguration.ElementBalancing;
import org.xml.sax.SAXException;

public class JangularCompiler {
	private DirectiveRepository directiveRepository;
	private List<String> warnings = new ArrayList<String>();
	
	
	public JangularCompiler(DirectiveRepository directiveRepository) {
		this.directiveRepository = directiveRepository;
	}

	public CompositeNode compile(InputStream html, Class<? extends Scope> scopeClass, Class<? extends EvaluationContext> evaluationContextClass) 
			throws ParserConfigurationException, SAXException {
		CompositeNode n = internalCompile(html);
		try {
			n.compileScope(scopeClass, evaluationContextClass, this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		if( !warnings.isEmpty() ) {
			System.err.println("The Jangular Compiler encountered the following warnings:\n");
			int i = 1;
			for( String warning : warnings ){
				System.err.println(i + " - "+warning);
				i++;
			}
		}
		
		return n;
	}
	
	CompositeNode internalCompile(InputStream html) throws ParserConfigurationException, SAXException {
		IAttoParser parser = new MarkupAttoParser();
		
		MarkupParsingConfiguration conf = new MarkupParsingConfiguration();
		conf.setElementBalancing(ElementBalancing.REQUIRE_BALANCED);

		CompilerMarkupHandler handler = new CompilerMarkupHandler(conf, directiveRepository);
		
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

	public void warn(String warning) {
		warnings.add(warning);
	}

	public void assertCasts(Field toField, Field fromField) {
		Class<?> toFieldClass = ClassUtils.primitiveToWrapper(toField.getType());
		Class<?> fromFieldClass = ClassUtils.primitiveToWrapper(fromField.getType());
		
		if( !toFieldClass.isAssignableFrom(fromFieldClass)) {
			if( !fromFieldClass.isAssignableFrom(toFieldClass)) {
			
				throw new RuntimeException(String.format(
					"The @In-field %s (%s) in %s is not of the same type as in the parent scope (%s, %s)!",
					toField.getName(), toFieldClass.getName(), 
					toField.getDeclaringClass().getName(), 
					fromField.getDeclaringClass().getName(),
					fromFieldClass.getName()));
			} else {
				warn(String.format(
					"The @In-field %s (%s) in %s cannot be guaranteed to be of the same type as in the parent scope (%s, %s)."
					+"\nAn unchecked typecast from %s to %s will be performed at run time!",
					toField.getName(), toFieldClass.getName(), 
					toField.getDeclaringClass().getName(), 
					fromField.getDeclaringClass().getName(),
					fromFieldClass.getName(),
					fromFieldClass.getSimpleName(),
					toFieldClass.getSimpleName()));
			}
		}
	}
	
	

}
