package net.cupmanager.jangular.compiler.caching;

import java.util.concurrent.Callable;

import net.cupmanager.jangular.compiler.CompiledTemplate;

public interface CachingStrategy {
	public CompiledTemplate get(String templatePath, Callable<CompiledTemplate> compileFunctor);
}
