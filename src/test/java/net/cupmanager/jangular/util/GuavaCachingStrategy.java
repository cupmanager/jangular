package net.cupmanager.jangular.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import net.cupmanager.jangular.compiler.CachingStrategy;
import net.cupmanager.jangular.compiler.CompiledTemplate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class GuavaCachingStrategy implements CachingStrategy {

	private Cache<String, CompiledTemplate> cache;

//	public GuavaCachingStrategy(Cache<String, CompiledTemplate> cache) {
//		this.cache = cache;
//	}

	public GuavaCachingStrategy(CacheBuilder builder) {
		cache = builder.build();
	}

	@Override
	public CompiledTemplate get(String templatePath, Callable<CompiledTemplate> compileFunctor) {
		try {
			return cache.get(templatePath, compileFunctor);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
