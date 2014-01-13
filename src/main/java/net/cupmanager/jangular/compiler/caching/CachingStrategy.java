package net.cupmanager.jangular.compiler.caching;

import java.util.concurrent.Callable;

import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.ControllerNotFoundException;
import net.cupmanager.jangular.exceptions.ParseException;

public interface CachingStrategy {
	public CompiledTemplate get(String templatePath, Callable<CompiledTemplate> compileFunctor) throws ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException, TemplateLoaderException ;
}
