package net.cupmanager.jangular.compiler;

public class NullCompilerCache implements CompilerCache {

	@Override
	public CompiledTemplate get(String template) {
		return null;
	}

	@Override
	public void save(String templatePath, CompiledTemplate compiled) {

	}
}
