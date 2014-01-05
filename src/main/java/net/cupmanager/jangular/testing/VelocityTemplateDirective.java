package net.cupmanager.jangular.testing;

import java.io.StringWriter;
import java.util.Map;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.Directive;
import net.cupmanager.jangular.annotations.Template;
import net.cupmanager.jangular.annotations.Variable;
import net.cupmanager.jangular.nodes.CompositeNode;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

@Directive("cm-velocitytemplate")
@Template("velocity.html")
public class VelocityTemplateDirective extends AbstractDirective {
	private CompositeNode content;

	@Override
	public void compile(Map<String,String> attrs, CompositeNode content) {
		this.content = content;
	}
	
//    public Scope link(Scope scope) {
//        /*  default:  scope.* = attrs.*   */
////      scope.put("matches", attrs.get("matches"))
//
//        // scope.get("1+1") => null
//    	
//    	return scope.newSubScope();
//    }
    
    
    @Variable("text")
    public String getText(Scope scope) {
    	StringBuilder sb = new StringBuilder();
    	content.eval(scope, sb);
    	StringWriter sw = new StringWriter();
    	Velocity.evaluate(scope, sw, "", sb.toString());
    	return sw.toString();
    }
}
