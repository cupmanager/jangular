package net.cupmanager.jangular;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cupmanager.jangular.annotations.Directive;
import net.cupmanager.jangular.annotations.InlineDirective;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;

public class DirectiveRepository {
	
	public static class InlineDirectiveMatcher {
		
		public final Matcher matcher;
		public final Class<? extends AbstractDirective<?>> directiveClass;
		
		public InlineDirectiveMatcher(Matcher matcher, Class<? extends AbstractDirective<?>> directiveClass) {
			this.matcher = matcher;
			this.directiveClass = directiveClass;
		}
		
		public Map<String,String> getAttributes() {
			Map<String,String> attributes = new HashMap<String,String>();
			Class<? extends Scope> scopeClass = AbstractDirective.getScopeClass(directiveClass);
			for(String varName : Scope.getScopeIns(scopeClass) ) {
				String value = matcher.group(varName);
				if( value != null ){
					attributes.put(varName, value);
				} else {
					attributes.put(varName, "null");
				}
			}
			return attributes;
		}
	}
	
	private static class InlineDirectiveItem {
		
		public final Pattern pattern;
		public final Class<? extends AbstractDirective<?>> directiveClass;
		
		public InlineDirectiveItem(String pattern, Class<? extends AbstractDirective<?>> directiveClass) {
			this.pattern = Pattern.compile(pattern);
			this.directiveClass = directiveClass;
		}

	}
	
	private Map<String, Class<? extends AbstractDirective<?>>> directives = new HashMap<String, Class<? extends AbstractDirective<?>>>();
	private List<InlineDirectiveItem> inlineDirectives = new ArrayList<InlineDirectiveItem>();
	
	public void register(Class<? extends AbstractDirective<?>> directive) {
		Directive a = directive.getAnnotation(Directive.class);
		if( a != null ) {
			directives.put(a.value(), directive);
		}
		
		InlineDirective b = directive.getAnnotation(InlineDirective.class);
		if( b != null ) {
			inlineDirectives.add(new InlineDirectiveItem(b.value(), directive));
		}
	}

	public boolean hasDirective(String qName) {
		return directives.containsKey(qName);
	}

	public Class<? extends AbstractDirective<?>> get(String name) {
		return directives.get(name);
	}
	
	public List<InlineDirectiveMatcher> getInlineDirectiveMatchers(String text) {
		List<InlineDirectiveMatcher> matchers = new ArrayList<InlineDirectiveMatcher>();
		for( InlineDirectiveItem directive : inlineDirectives ){
			Matcher m = directive.pattern.matcher(text);
			matchers.add(new InlineDirectiveMatcher(m, directive.directiveClass));
		}
		return matchers;
	}
	
	
}
