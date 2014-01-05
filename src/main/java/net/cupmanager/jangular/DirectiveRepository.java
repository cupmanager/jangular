package net.cupmanager.jangular;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.App.AppScope;
import net.cupmanager.jangular.annotations.Directive;
import net.cupmanager.jangular.annotations.Template;
import net.cupmanager.jangular.nodes.CompositeNode;
import net.cupmanager.jangular.nodes.DirectiveNode;
import net.cupmanager.jangular.nodes.JangularNode;
import net.cupmanager.jangular.testing.AbstractDirective;

import org.xml.sax.SAXException;

public class DirectiveRepository {
	private Map<String, Class<? extends AbstractDirective>> directives = new HashMap<String, Class<? extends AbstractDirective>>();
	
	public void register(Class<? extends AbstractDirective> directive) {
		Directive a = directive.getAnnotation(Directive.class);
		directives.put(a.value(), directive);
	}

	public boolean hasDirective(String qName) {
		return directives.containsKey(qName);
	}

	public JangularNode getDirectiveNode(String name, Map<String, String> attributesObject, JangularNode node) {
		Class<? extends AbstractDirective> c = directives.get(name);
		String templateFile = c.getAnnotation(Template.class).value();
		
		try {
			CompositeNode compositeNode = new Compiler(this).internalCompile(new FileInputStream(templateFile));
			
			AbstractDirective directiveInstance = c.newInstance();
			
			directiveInstance.compile(attributesObject, compositeNode);
			
			return new DirectiveNode(directiveInstance, compositeNode, attributesObject);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
}
