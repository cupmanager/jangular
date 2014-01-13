package net.cupmanager.jangular;

import net.cupmanager.jangular.compiler.CompilerConfiguration;
import net.cupmanager.jangular.compiler.ConcreteTemplateCompiler;
import net.cupmanager.jangular.compiler.templateloader.FileTemplateLoader;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;
import net.cupmanager.jangular.exceptions.CompileException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.ControllerNotFoundException;
import net.cupmanager.jangular.exceptions.EvaluationException;

import org.junit.Before;
import org.junit.Test;

public class ExceptionTests {
	public static class Item {
		public String title = "Title";
	}
	
	public static class AppScope extends Scope {
		public Item item;
	}

	private ConcreteTemplateCompiler compiler;
	
	@Before
	public void createCompiler() {
		CompilerConfiguration conf = CompilerConfiguration.create()
				.withTemplateLoader(new FileTemplateLoader("templates/test/crash"));
		
		this.compiler = ConcreteTemplateCompiler.create(conf);
	}
	
	@Test(expected=CompileExpressionException.class)
    public void noField() throws CompileException, EvaluationException {
		compiler.compile("badfield.html", AppScope.class);
    }
	
	
	@Test(expected=ControllerNotFoundException.class)
    public void noController() throws CompileException, EvaluationException {
		compiler.compile("badcontroller.html", AppScope.class);
    }
	
	@Test(expected=TemplateLoaderException.class)
    public void noTemplate() throws CompileException, EvaluationException {
		compiler.compile("nosuchfile.html", AppScope.class);
    }
	
//	private static AppScope createScope() {
//		final List<Item> _items = new ArrayList<Item>();
//		for( int i = 0; i < 100; i++ ) {
//			Item item = new Item();
//			_items.add(item);
//		}
//		
//		
//		AppScope scope = new AppScope();
//		scope.item = new Item();
//		scope.item.title = "Hejsan";
//		return scope;
//	}
	
}
