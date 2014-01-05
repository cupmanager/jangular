package net.cupmanager.jangular.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.cupmanager.jangular.JangularCompiler;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.expressions.CompiledExpression;
import net.cupmanager.jangular.injection.EvaluationContext;

public class ConditionalNode implements JangularNode {

	private String condition;
	private CompiledExpression compiledCondition;
	private JangularNode node;
	private JangularNode elseNode;

	private List<JangularNode> elseIfNodes;
	private List<String> elseIfConditions;
	private CompiledExpression[] elseIfCompiledConditions;
	
	
	public ConditionalNode(String condition, JangularNode node) {
		this.condition = condition;
		this.node = node;
		
	}
	
	
	@Override
	public void eval(Scope scope, StringBuilder sb, EvaluationContext context) {
		Boolean result = (Boolean) compiledCondition.eval(scope);
		if (result) {
			node.eval(scope, sb, context);
		} else {
			
			if( elseIfCompiledConditions != null ){
				for( int i = 0; i < elseIfCompiledConditions.length; i++){
					result = (Boolean) elseIfCompiledConditions[i].eval(scope);
					if( result ){
						elseIfNodes.get(i).eval(scope, sb, context);
						return;
					}
				}
			}
			
			
			if( elseNode != null ){
				elseNode.eval(scope, sb, context);
			}
		}
		
		
	}


	@Override
	public Collection<String> getReferencedVariables() {
		Set<String> vars = new HashSet<String>();
		
		vars.addAll(CompiledExpression.getReferencedVariables(condition));
		
		if(elseIfConditions != null) {
			for(String condition : elseIfConditions){
				vars.addAll(CompiledExpression.getReferencedVariables(condition));
			}
		}
		
		vars.addAll(node.getReferencedVariables());
		if( elseNode != null ){
			vars.addAll(elseNode.getReferencedVariables());
		}
		
		return vars;
	}


	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass, Class<? extends EvaluationContext> evaluationContextClass, JangularCompiler compiler) throws Exception {
		compiledCondition = CompiledExpression.compile(condition, parentScopeClass);
		
		if( elseIfNodes != null){
			elseIfCompiledConditions = new CompiledExpression[elseIfConditions.size()];
			for(int i = 0; i < elseIfConditions.size(); i++){
				elseIfCompiledConditions[i] = CompiledExpression.compile(elseIfConditions.get(i), parentScopeClass);
				elseIfNodes.get(i).compileScope(parentScopeClass, evaluationContextClass, compiler);
			}
		}
		
		node.compileScope(parentScopeClass, evaluationContextClass, compiler);

		if (elseNode != null) {
			elseNode.compileScope(parentScopeClass, evaluationContextClass, compiler);
		}
	}

	public void addElseIf(String condition, JangularNode elseIfNode) {
		if( this.elseNode != null ){
			throw new RuntimeException("j-if cannot have j-else-if after j-else!");
		}
		
		if( elseIfNodes == null ){
			elseIfNodes = new ArrayList<JangularNode>();
			elseIfConditions = new ArrayList<String>();
		}
		
		elseIfNodes.add(elseIfNode);
		elseIfConditions.add(condition);
	}

	public void setElse(JangularNode elseNode) {
		if( this.elseNode != null ){
			throw new RuntimeException("j-if cannot have two j-else!");
		}
		this.elseNode = elseNode;
	}

}
