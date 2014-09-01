package net.cupmanager.jangular.compiler.caching;

import java.util.concurrent.Callable;

import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.ResourceSpecification;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.ControllerNotFoundException;
import net.cupmanager.jangular.exceptions.ParseException;

public class NullCachingStrategy implements CachingStrategy {

	@Override
	public CompiledTemplate get(ResourceSpecification spec, long lastModified, Callable<CompiledTemplate> compileFunctor) throws ControllerNotFoundException, ParseException, NoSuchScopeFieldException,
			CompileExpressionException, TemplateLoaderException {
		try {
			return compileFunctor.call();
		} catch (Exception cause) {
			if( cause instanceof RuntimeException ){
				throw (RuntimeException)cause;
			} else if (cause instanceof ControllerNotFoundException ){
				throw (ControllerNotFoundException)cause;
			} else if (cause instanceof ParseException ){
				throw (ParseException)cause;
			} else if (cause instanceof NoSuchScopeFieldException ){
				throw (NoSuchScopeFieldException)cause;
			} else if (cause instanceof CompileExpressionException ){
				throw (CompileExpressionException)cause;
			} else if (cause instanceof TemplateLoaderException ){
				throw (TemplateLoaderException)cause;
			} else {
				throw new TemplateLoaderException(cause);
			}
		}
	}

}
