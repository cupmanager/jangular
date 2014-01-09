package net.cupmanager.jangular.compiler;

public interface CompilerCache {
	public CompiledTemplate get(String template);
	public void save(String templatePath, CompiledTemplate compiled);
}
