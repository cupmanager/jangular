package net.cupmanager.jangular.testing;

import net.cupmanager.jangular.AbstractController;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.testing.MinCtrl.MinCtrlScope;

public class MinCtrl extends AbstractController<MinCtrlScope> {
	/*@Inject */String url = "http://localhost/stuff";
	
	public static class MinCtrlScope extends Scope {
		public String info;
	}
	
	@Override
	public void eval(MinCtrlScope scope) {
		scope.info = url.toUpperCase();
	}
}
