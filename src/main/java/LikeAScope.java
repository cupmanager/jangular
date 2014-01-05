import net.cupmanager.jangular.Scope;



public class LikeAScope extends Scope{
	public int $index;
	public Object varName;
	public int name;
	
	
	public void set(LikeAScopeParent parent, int $index, Object[] varName) {
		this.name = (Integer)varName[1];
		this.$index = $index;
		this.varName = varName;
	}
}
