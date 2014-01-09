package net.cupmanager.jangular.compiler;

import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;

import org.attoparser.AttoParseException;
import org.xml.sax.SAXException;

public class CachingTemplateCompiler implements TemplateCompiler {
	private TemplateCompiler compiler;
	private CachingStrategy cachingStrategy;

	public CachingTemplateCompiler(TemplateCompiler compiler, CachingStrategy cachingStrategy) {
		this.compiler = compiler;
		this.cachingStrategy = cachingStrategy;
	}

	@Override
	public CompiledTemplate compile(String templatePath) throws ParserConfigurationException, SAXException, AttoParseException, TemplateLoaderException {
		return compile(templatePath, Scope.class);
	}

	@Override
	public CompiledTemplate compile(final String templatePath, final Class<? extends Scope> scopeClass) throws ParserConfigurationException, SAXException, AttoParseException, TemplateLoaderException {
		return cachingStrategy.get(templatePath, new Callable<CompiledTemplate>() {
			@Override
			public CompiledTemplate call() throws Exception {
				return compiler.compile(templatePath, scopeClass);
			}
		});
	}

	@Override
	public CompiledTemplate compile(InputStream is) throws ParserConfigurationException, SAXException, AttoParseException {
		return compiler.compile(is);
	}

	@Override
	public CompiledTemplate compile(InputStream is, Class<? extends Scope> scopeClass) throws ParserConfigurationException, SAXException, AttoParseException {
		return compiler.compile(is, scopeClass);
	}

}
