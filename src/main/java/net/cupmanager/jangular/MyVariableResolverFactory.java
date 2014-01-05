package net.cupmanager.jangular;

import java.util.Set;

import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.SimpleValueResolver;

public abstract class MyVariableResolverFactory implements VariableResolverFactory {
	
	public VariableResolver getVariableResolver(String name) {
		Object value = getValue(name);
		if (value != null) {
			return new SimpleValueResolver(value);
		} else {
			VariableResolverFactory next = getNextFactory();
			if (next != null) {
				return next.getVariableResolver(name);
			}
		}
		return null;
	}

	protected abstract Object getValue(String key);

	public boolean isTarget(String name) {
		return getValue(name) != null;
	}

	public boolean isResolveable(String name) {
		if (name == null) return false;
		if (isTarget(name)) {
			return true;
		} else {
			VariableResolverFactory next = getNextFactory();
			if (next != null) {
				return next.isResolveable(name);
			}
		}
		return false;
	}

	
	
	
	
	
	
	
	
	public Set<String> getKnownVariables() {
		return null;
	}
	
	public VariableResolver createVariable(String name, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	public VariableResolver createIndexedVariable(int index, String name, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	public VariableResolver createVariable(String name, Object value, Class<?> type) {
		// TODO Auto-generated method stub
		return null;
	}

	public VariableResolver createIndexedVariable(int index, String name, Object value, Class<?> typee) {
		// TODO Auto-generated method stub
		return null;
	}

	public VariableResolver setIndexedVariableResolver(int index, VariableResolver variableResolver) {
		// TODO Auto-generated method stub
		return null;
	}

	public VariableResolverFactory getNextFactory() {
		return null;
	}

	public VariableResolverFactory setNextFactory(VariableResolverFactory resolverFactory) {
		return null;
	}

	public VariableResolver getIndexedVariableResolver(int index) {
		return null;
	}

	public int variableIndexOf(String name) {
		return 0;
	}

	public boolean isIndexedFactory() {
		return false;
	}

	private boolean f;
	public boolean tiltFlag() {
		return f;
	}

	public void setTiltFlag(boolean tilt) {
		f = tilt;
	}
	
}