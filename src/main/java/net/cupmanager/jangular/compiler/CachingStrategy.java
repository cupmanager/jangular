package net.cupmanager.jangular.compiler;

import java.util.concurrent.Callable;

public interface CachingStrategy {
	public CompiledTemplate get(String templatePath, Callable<CompiledTemplate> compileFunctor);
}
