package net.cupmanager.jangular.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.expressions.CompiledExpression;
import net.cupmanager.jangular.injection.EvaluationContext;

public class ConditionalNode extends JangularNode {

	private String condition;
	private CompiledExpression compiledCondition;
	private JangularNode node;
	private JangularNode elseNode;

	private List<JangularNode> elseIfNodes;
	private List<String> elseIfConditions;
	private CompiledExpression[] elseIfCompiledConditions;
	
	
	@Override
	public JangularNode clone() {
		ConditionalNode cn = new ConditionalNode();
		cn.condition = condition;
		cn.compiledCondition = compiledCondition;
		cn.node = node.clone();
		cn.elseNode = elseNode == null ? null : elseNode.clone();
		cn.elseIfNodes = new ArrayList<JangularNode>();
		for (JangularNode n : elseIfNodes) {
			cn.elseIfNodes.add(n.clone());
		}
		cn.elseIfConditions = new ArrayList<String>(elseIfConditions);
		cn.elseIfCompiledConditions = elseIfCompiledConditions;
		return cn;
	}
	private ConditionalNode() {}
	
	public ConditionalNode(String condition, JangularNode node) {
		this.condition = condition;
		this.node = node;
		
	}
	
	
	@Override
	public void eval(Scope scope, StringBuilder sb, EvaluationContext context, EvaluationSession session)
			throws EvaluationException {
		Boolean result = (Boolean) compiledCondition.eval(scope);
		if (result) {
			session.eval(node, scope, sb, context);
		} else {
			
			if( elseIfCompiledConditions != null ){
				for( int i = 0; i < elseIfCompiledConditions.length; i++){
					result = (Boolean) elseIfCompiledConditions[i].eval(scope);
					if( result ){
						session.eval(elseIfNodes.get(i), scope, sb, context);
						return;
					}
				}
			}
			
			
			if( elseNode != null ){
				session.eval(elseNode, scope, sb, context);
			}
		}
		
		
	}


	@Override
	public Collection<String> getReferencedVariables() throws CompileExpressionException {
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
	public void compileScope(Class<? extends Scope> parentScopeClass, Class<? extends EvaluationContext> evaluationContextClass, CompilerSession session) throws NoSuchScopeFieldException, CompileExpressionException {
		compiledCondition = CompiledExpression.compile(condition, parentScopeClass, session);
		
		if( elseIfNodes != null){
			elseIfCompiledConditions = new CompiledExpression[elseIfConditions.size()];
			for(int i = 0; i < elseIfConditions.size(); i++){
				elseIfCompiledConditions[i] = CompiledExpression.compile(elseIfConditions.get(i), parentScopeClass, session);
				elseIfNodes.get(i).compileScope(parentScopeClass, evaluationContextClass, session);
			}
		}
		
		node.compileScope(parentScopeClass, evaluationContextClass, session);

		if (elseNode != null) {
			elseNode.compileScope(parentScopeClass, evaluationContextClass, session);
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
