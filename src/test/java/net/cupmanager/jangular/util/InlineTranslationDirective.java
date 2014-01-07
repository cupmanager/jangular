package net.cupmanager.jangular.util;

import java.util.Map;

import net.cupmanager.jangular.AbstractDirective;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.annotations.InlineDirective;
import net.cupmanager.jangular.annotations.Template;
import net.cupmanager.jangular.nodes.JangularNode;
import net.cupmanager.jangular.util.InlineTranslationDirective.TranslationScope;

@Template("{{translation}}")
@InlineDirective("\\[\\[(?<key>.*?)(?:::(?<defaultTranslation>.*?))?\\]\\]")
public class InlineTranslationDirective extends AbstractDirective<TranslationScope> {
	
	//public @Inject Language language;
	
	public static class TranslationScope extends Scope {
		@In public String key;
		@In public String defaultTranslation;
		public String translation;
	}
	
	@Override
	public void compile(Map<String, String> attributesObject, JangularNode content) {
		
	}
	
	@Override
	public void eval(TranslationScope scope) {
		scope.translation = scope.key;
	}
	
}
