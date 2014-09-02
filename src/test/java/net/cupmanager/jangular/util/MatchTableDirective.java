package net.cupmanager.jangular.util;

import java.util.Map;

import net.cupmanager.jangular.AbstractDirective;
import net.cupmanager.jangular.LargeTest.Item;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.Directive;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.annotations.Context;
import net.cupmanager.jangular.annotations.Template;
import net.cupmanager.jangular.nodes.JangularNode;
import net.cupmanager.jangular.util.MatchTableDirective.MatchTableScope;

@Directive("cm-matchtable")
@Template("matchtable.html")
public class MatchTableDirective extends AbstractDirective<MatchTableScope> {
	
	public @Context String hej;
	
	public static class MatchTableScope extends Scope {
		@In public Item item;
		@In public Object no;
		public boolean includeheaders;
		public String hej;
	}
	
	@Override
	public void compile(Map<String,String> attrs, JangularNode templateNode, JangularNode contentNode) {
		
	}
	
	@Override
	public void eval(MatchTableScope scope) {
		scope.hej = hej;
	}
	
}
