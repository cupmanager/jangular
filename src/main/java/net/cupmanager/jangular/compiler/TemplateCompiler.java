package net.cupmanager.jangular.compiler;

import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;

import org.attoparser.AttoParseException;
import org.xml.sax.SAXException;

public interface TemplateCompiler {
	public CompiledTemplate compile(String templatePath) throws ParserConfigurationException, SAXException, AttoParseException, TemplateLoaderException;
	public CompiledTemplate compile(String templatePath, Class<? extends Scope> scopeClass) throws ParserConfigurationException, SAXException, AttoParseException, TemplateLoaderException;
	
	public CompiledTemplate compile(InputStream is)  throws ParserConfigurationException, SAXException, AttoParseException;
	public CompiledTemplate compile(InputStream is, Class<? extends Scope> scopeClass)  throws ParserConfigurationException, SAXException, AttoParseException;
}
