package net.cupmanager.jangular.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.cupmanager.jangular.DirectiveRepository;
import net.cupmanager.jangular.DirectiveRepository.InlineDirectiveMatcher;
import net.cupmanager.jangular.nodes.CompositeNode;
import net.cupmanager.jangular.nodes.ConditionalNode;
import net.cupmanager.jangular.nodes.ControllerNode;
import net.cupmanager.jangular.nodes.ExpressionNode;
import net.cupmanager.jangular.nodes.JClassNode;
import net.cupmanager.jangular.nodes.JangularNode;
import net.cupmanager.jangular.nodes.RepeatNode;
import net.cupmanager.jangular.nodes.TextNode;

import org.attoparser.AttoParseException;
import org.attoparser.markup.AbstractStandardMarkupAttoHandler;
import org.attoparser.markup.MarkupParsingConfiguration;

public class CompilerMarkupHandler extends AbstractStandardMarkupAttoHandler {
	
	private static class ElementMemory {
		private String jif;
		private String jrepeat;
		public  Map<String,String> attrs;
		private String jcontroller;
		
		public ElementMemory(Map<String, String> attributes) {
			this.jif = attributes.get("j-if");
			this.jrepeat = attributes.get("j-repeat");
			this.jcontroller = attributes.get("j-controller");
			this.attrs = new HashMap<String,String>(attributes);
		}
		
		public boolean isRepeat() {
			return jrepeat != null;
		}

		public boolean isConditional() {
			return jif != null;
		}
		
		public boolean isController() {
			return jcontroller != null;
		}
	}
	
	
	/* Find expressions like {{......}} */
	private static Pattern expressionPattern = Pattern.compile("\\{\\{(.*?)\\}\\}");

	private Stack<CompositeNode> stack = new Stack<CompositeNode>();
	private Stack<ElementMemory> attrStack = new Stack<ElementMemory>();
	private DirectiveRepository directiveRepository;
	private JangularCompiler compiler;
	
	public CompilerMarkupHandler(JangularCompiler compiler, MarkupParsingConfiguration conf, DirectiveRepository directiveRepository) {
		super(conf);
		this.compiler = compiler;
		this.directiveRepository = directiveRepository;
		stack.push(new CompositeNode());
	}
	
	@Override
	public void handleAutoClosedElement(String elementName, int line, int col) throws AttoParseException {
		handleCloseElement(elementName, line, col);
	}
	
	private CompositeNode commonOpen(String elementName, Map<String,String> attributes) {
		if (attributes == null) {
			attributes = new HashMap<String, String>();
		}
		attrStack.push(new ElementMemory(attributes));
		CompositeNode node = new CompositeNode();
		stack.push(node);
		
		if( !shouldStripTag(elementName, attributes)) {
			node.add(new TextNode("<"+elementName));
			
			for (Map.Entry<String,String> entry : attributes.entrySet()) {
				String attributeName = entry.getKey();
				if( !attributeName.startsWith("j-") && !attributeName.equals("class") ){
					String text = " " + attributeName + "=\"" + entry.getValue() + "\"";
					parseText(text, node);
				}
			}
			
			String classValue = attributes.get("class");
			String jClassValue = attributes.get("j-class");
			
			if( classValue != null || jClassValue != null ){
				node.add(new TextNode(" class=\""));
			
				if( classValue != null ){
					parseText(classValue, node);
				}
				
				if (jClassValue != null) {
					node.add(new JClassNode(jClassValue));
				}
				
				node.add(new TextNode("\""));
			}
		}
		return node;
	}
	
	@Override
	public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) throws AttoParseException {
		CompositeNode node = commonOpen(elementName, attributes);
		addTagTextNode(node, ">", elementName, attributes);
	}

	private boolean shouldStripTag(String elementName, Map<String,String> attributes) {
		if ("j-if".equals(elementName)) {
			return true;
		} else if("j-else".equals(elementName)) {
			return true;
		} else if("j-else-if".equals(elementName)) {
			return true;
		} else if("j-controller".equals(elementName)) {
			return true;
		} else if("j-repeat".equals(elementName)) {
			return true;
		} else if(directiveRepository.hasDirective(elementName) ) {
			return true;
		}
		
		return false;
	}
	
	private void addTagTextNode(CompositeNode node, String text, String elementName, Map<String,String> attributes) {
		if( !shouldStripTag(elementName, attributes) ) {
			node.add(new TextNode(text));
		}
	}
	
	private void commonClose(String elementName, ElementMemory elementMemory, CompositeNode compositeNode) {
		
		compositeNode.optimize();
		JangularNode node = compositeNode;
		
		if ("j-if".equals(elementName)) {
			node = new ConditionalNode(elementMemory.attrs.get("test"), node);
		} else if ("j-else".equals(elementName) )  {
			CompositeNode parent = stack.peek();
			JangularNode sibling = parent.peek();
			if( sibling instanceof ConditionalNode ){
				ConditionalNode ifNode = (ConditionalNode)sibling;
				ifNode.setElse(node);
				return;
			} else {
				throw new RuntimeException("j-else must follow directly behind an j-if");
			}
		} else if ("j-else-if".equals(elementName) )  {
			CompositeNode parent = stack.peek();
			JangularNode sibling = parent.peek();
			if( sibling instanceof ConditionalNode ){
				ConditionalNode ifNode = (ConditionalNode)sibling;
				ifNode.addElseIf(elementMemory.attrs.get("test"),node);
				return;
			} else {
				throw new RuntimeException("j-else must follow directly behind an j-if");
			}
		} else if ("j-controller".equals(elementName)) {
			String controllerClassName = elementMemory.attrs.get("controller");
			node = new ControllerNode(controllerClassName, node);
		} else if ("j-repeat".equals(elementName)) {
			String repeatExpr = elementMemory.attrs.get("for");
			node = new RepeatNode(repeatExpr, node);
		} else {
			
			if (directiveRepository.hasDirective(elementName)) {
				Map<String,String> attributesObject = elementMemory.attrs;
				node = compiler.getDirectiveNode(elementName, attributesObject, node);
			}
			
			if (elementMemory.isController()) {
				node = new ControllerNode(elementMemory.jcontroller, node);
			}
			
			if (elementMemory.isConditional()) {
				node = new ConditionalNode(elementMemory.jif, node);
			}
			
			if (elementMemory.isRepeat()) {
				node = new RepeatNode(elementMemory.jrepeat, node);
			}
		}
		
		CompositeNode parent = stack.peek();
		parent.add(node);
	}
	
	@Override
	public void handleCloseElement(String elementName, int line, int col) throws AttoParseException {
		ElementMemory elementMemory = attrStack.pop();
		CompositeNode compositeNode = stack.pop();
		addTagTextNode(compositeNode, "</"+elementName+">", elementName, elementMemory.attrs);
		commonClose(elementName, elementMemory, compositeNode);
	}

	
	// Combination of start&end, but modified so it outputs empty elements ( <br/> for example )
	@Override
	public void handleStandaloneElement(String elementName, Map<String, String> attributes, int line, int col) throws AttoParseException {
		CompositeNode node = commonOpen(elementName, attributes);
		addTagTextNode(node, "/>", elementName, attributes);
		
		ElementMemory elementMemory = attrStack.pop();
		CompositeNode compositeNode = stack.pop();
		commonClose(elementName, elementMemory, compositeNode);
	}

	@Override
	public void handleText(char[] buffer, int offset, int len, int line, int col) throws AttoParseException {
		CompositeNode node = stack.peek();
		String text = String.copyValueOf(buffer, offset, len);
		parseText(text, node);
	}
	
	
	
	
	
	private static class Range {
		public int start;
		public int end;
		public List<JangularNode> nodes = new ArrayList<JangularNode>();
		
		public Range(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}
	private void parseText(String text, CompositeNode node) {
		List<InlineDirectiveMatcher> matchers = directiveRepository.getInlineDirectiveMatchers(text);
		
		List<Range> ranges = new ArrayList<Range>();
		
		for(InlineDirectiveMatcher matcher : matchers ){
			
			int start = 0;
			while(matcher.matcher.find(start)) {
				Range range = new Range(matcher.matcher.start(), matcher.matcher.end());
				range.nodes.add(compiler.getDirectiveNode(matcher.directiveClass, matcher.getAttributes(), null));
				ranges.add(range);
				
				start = matcher.matcher.end();
			}
		}
		
		Matcher m = expressionPattern.matcher(text);
		int start = 0;
		while(m.find(start)) {
			Range range = new Range(m.start(), m.end());
			range.nodes.add(new ExpressionNode(m.group(1)));
			ranges.add(range);
			
			start = m.end();
		}
		
		
		// Sort the ranges
		Collections.sort(ranges, new Comparator<Range>() {
			@Override
			public int compare(Range r1, Range r2) {
				return r1.start - r2.start;
			}
		});
		
		// Add nodes
		start = 0;
		for (Range range : ranges) {
			// Has to start after the previous range!
			if (range.start >= start) {
				
				// Add the text that was before range
				if (range.start > start) {
					node.add(new TextNode(text.substring(start, range.start)));
				}
				
				for (JangularNode n : range.nodes) { 
					node.add(n);
				}
				
				start = range.end;
			}
		}
		
		if (start < text.length()) {
			node.add(new TextNode(text.substring(start)));
		}
	}

	
	
	
	
	public CompositeNode getNode() {
		return stack.pop();
	}
}
