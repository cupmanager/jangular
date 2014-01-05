package net.cupmanager.jangular.testing;

import java.util.List;

import net.cupmanager.jangular.AbstractController;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.annotations.Inject;
import net.cupmanager.jangular.testing.MinCtrl.MinCtrlScope;

public class MinCtrl extends AbstractController<MinCtrlScope> {
	public @Inject("URL") String url = "http://localhost/stuff";
	
	public static class MinCtrlScope extends Scope {
		public String info;
		public @In List items;
		public @In int i;
	}
	
	@Override
	public void eval(MinCtrlScope scope) {
		scope.info = url.toUpperCase();
		//scope.items.clear();
	}
}
