package net.cupmanager.jangular.compiler;

import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.compiler.templateloader.TemplateLoaderException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.ControllerNotFoundException;
import net.cupmanager.jangular.exceptions.ParseException;

import org.attoparser.AttoParseException;
import org.xml.sax.SAXException;

public interface TemplateCompiler {
	public CompiledTemplate compile(String templatePath) throws TemplateLoaderException, ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException;
	public CompiledTemplate compile(String templatePath, Class<? extends Scope> scopeClass) throws TemplateLoaderException, ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException;
	
	public CompiledTemplate compile(InputStream is) throws ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException;
	public CompiledTemplate compile(InputStream is, Class<? extends Scope> scopeClass) throws ControllerNotFoundException, ParseException, NoSuchScopeFieldException, CompileExpressionException;
}
