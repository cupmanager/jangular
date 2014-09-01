package net.cupmanager.jangular.compiler;

import java.io.InputStream;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.ControllerNotFoundException;
import net.cupmanager.jangular.exceptions.ParseException;

public interface TemplateCompiler {
	public static class Builder {
		public static TemplateCompiler create() {
			return new ConcreteTemplateCompiler();
		}
		
		public static TemplateCompiler create(CompilerConfiguration conf) {
			return new ConcreteTemplateCompiler(conf);
		}
	}
	
	public CompiledTemplate compile(String templatePath) throws TemplateLoaderException, ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException;
	public CompiledTemplate compile(String templatePath, Class<? extends Scope> scopeClass) throws TemplateLoaderException, ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException;
	
	public CompiledTemplate compile(InputStream is) throws ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException;
	public CompiledTemplate compile(InputStream is, Class<? extends Scope> scopeClass) throws ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException;
}
