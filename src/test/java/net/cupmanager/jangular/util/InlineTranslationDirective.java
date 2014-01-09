package net.cupmanager.jangular.util;

import java.util.Map;

import net.cupmanager.jangular.AbstractDirective;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.annotations.Context;
import net.cupmanager.jangular.annotations.InlineDirective;
import net.cupmanager.jangular.annotations.TemplateText;
import net.cupmanager.jangular.nodes.JangularNode;
import net.cupmanager.jangular.util.InlineTranslationDirective.TranslationScope;

@TemplateText("{{translation}}")
@InlineDirective("\\[\\[(?<key>.*?)(?:::(?<defaultTranslation>.*?))?\\]\\]")
public class InlineTranslationDirective extends AbstractDirective<TranslationScope> {
	
	public @Context("Language") String language;
	
	public static class TranslationScope extends Scope {
		@In public String key;
		@In public String defaultTranslation;
		public String translation;
	}
	
	@Override
	public void compile(Map<String, String> attributesObject, JangularNode templateNode, JangularNode contentNode) {
		
	}
	
	@Override
	public void eval(TranslationScope scope) {
		scope.translation = "translated("+language+"," + scope.key + ")";
	}
	
}
