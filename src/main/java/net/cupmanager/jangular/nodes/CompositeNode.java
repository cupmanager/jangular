package net.cupmanager.jangular.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.injection.EvaluationContext;

public class CompositeNode extends JangularNode {
	
	private List<JangularNode> nodes;
	private JangularNode[] fastnodes;
	
	@Override
	public JangularNode clone() {
		CompositeNode cn = new CompositeNode();
		
		if (nodes == null) {
			cn.nodes = null; 
		} else {
			cn.nodes = new ArrayList<JangularNode>();
			for (JangularNode n : nodes) {
				cn.nodes.add(n.clone());
			}
		}
		
		if (fastnodes == null) {
			cn.fastnodes = null;
		} else {
			cn.fastnodes = new JangularNode[fastnodes.length];
			for (int i=0; i<fastnodes.length; i++) {
				cn.fastnodes[i] = fastnodes[i].clone();
			}
			
		}
		
		return cn;
	}
	
	public CompositeNode(List<JangularNode> nodes) {
		this.nodes = nodes;
	}
	
	public CompositeNode() {
		this(new ArrayList<JangularNode>());
	}
	
	@Override
	public void eval(Scope scope, StringBuilder sb, EvaluationContext context)
			throws EvaluationException {
		for (JangularNode node : fastnodes) {
			node.eval(scope, sb, context);
		}
	}

	public void add(JangularNode node) {
		nodes.add(node);
	}

	public void optimize() {
		optimizeCompositeNodes();
		optimizeTextNodes();
		
		fastnodes = nodes.toArray(new JangularNode[nodes.size()]);
		nodes = null;
	}
	
	private void optimizeCompositeNodes() {
		ListIterator<JangularNode> it = nodes.listIterator();
		while (it.hasNext()) {
			JangularNode node = it.next();
			if (node instanceof CompositeNode) {
				
				it.remove();
				for (JangularNode n : ((CompositeNode)node).fastnodes) {
					it.add(n);
				}
				
			}
		}
	}
	
	private void optimizeTextNodes() {
		ListIterator<JangularNode> it = nodes.listIterator();
		TextNode currentTextNode = null;
		while (it.hasNext()) {
			JangularNode node = it.next();
			if (node instanceof TextNode) {
				TextNode textNode = (TextNode) node;
				if (currentTextNode != null) {
					currentTextNode.merge(textNode);
					it.remove();
				} else {
					currentTextNode = textNode;
				}
			} else {
				currentTextNode = null;
			}
		}
	}

	@Override
	public Collection<String> getReferencedVariables() throws CompileExpressionException {
		Set<String> variables = new HashSet<String>();
		for (JangularNode node : fastnodes) {
			variables.addAll(node.getReferencedVariables());
		}
		return variables;
	}

	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass, 
			CompilerSession session) throws NoSuchScopeFieldException, CompileExpressionException {
		for (JangularNode node : fastnodes) {
			node.compileScope(parentScopeClass, evaluationContextClass, session);
		}
	}

	public JangularNode peek() {
		for(int i = nodes.size()-1; i >= 0; i--){
			JangularNode node = nodes.get(i);
			if( node instanceof TextNode ){
				TextNode textNode = (TextNode)node;
				if( !textNode.isWhitespace() ){
					return textNode;
				}
			} else {
				return node;
			}
		}	
		return null;
	}

}
