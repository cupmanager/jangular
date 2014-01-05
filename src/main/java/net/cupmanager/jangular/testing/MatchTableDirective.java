package net.cupmanager.jangular.testing;

import java.util.Map;

import net.cupmanager.jangular.App.Item;
import net.cupmanager.jangular.AbstractDirective;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.Directive;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.annotations.Template;
import net.cupmanager.jangular.nodes.CompositeNode;
import net.cupmanager.jangular.testing.MatchTableDirective.MatchTableScope;

@Directive("cm-matchtable")
@Template("matchtable.html")
public class MatchTableDirective extends AbstractDirective<MatchTableScope> {
	
//	@Inject
//	WebRequest webRequest;
	
	public static class MatchTableScope extends Scope {
		@In public Item item;
		public boolean includeheaders;
	}
	
	@Override
	public void compile(Map<String,String> attrs, CompositeNode content) {
		
	}
	
	@Override
	public void eval(MatchTableScope scope) {
		
	}
	
}
