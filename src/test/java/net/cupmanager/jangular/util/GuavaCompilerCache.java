package net.cupmanager.jangular.util;

import net.cupmanager.jangular.compiler.CompiledTemplate;
import net.cupmanager.jangular.compiler.CompilerCache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class GuavaCompilerCache implements CompilerCache {
	
	private Cache<String, CompiledTemplate> cache; 
	
	public GuavaCompilerCache() {
		cache = CacheBuilder.newBuilder().maximumSize(1000).build();
	}
	
	@Override
	public CompiledTemplate get(String template) {
		return cache.getIfPresent(template);
	}

	@Override
	public void save(String templatePath, CompiledTemplate compiled) {
		cache.put(templatePath, compiled);
	}

}
