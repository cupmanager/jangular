package net.cupmanager.jangular;


public class ConstantExpression extends CompiledExpression {

	private String stringValue;
	private Object value;

	public ConstantExpression(Object value) {
		this.value = value;
		this.stringValue = ""+value; 
	}
	
	@Override
	public Object eval(Scope scope) {
		return value;
	}
	
	@Override
	public String evalToString(Scope scope) {
		return stringValue;
	}

}
