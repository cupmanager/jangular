package net.cupmanager.jangular.util;

import java.util.List;

import net.cupmanager.jangular.AbstractController;
import net.cupmanager.jangular.LargeTest.Item;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.annotations.Context;
import net.cupmanager.jangular.util.MinCtrl.MinCtrlScope;

public class MinCtrl extends AbstractController<MinCtrlScope> {
	public @Context("URL") String url = "http://localhost/stuff";
	
	public static class MinCtrlScope extends Scope {
		public String info;
		public @In List<Item> items;
		public @In int i;
	}
	
	@Override
	public void eval(MinCtrlScope scope) {
		scope.info = url.toUpperCase();
		//scope.items.clear();
	}
}
