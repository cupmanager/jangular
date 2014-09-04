package net.cupmanager.jangular.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class QueueResourceSpecification implements ResourceSpecification {

	private List<String> paths = new ArrayList<String>();

	public QueueResourceSpecification(List<String> paths){
		this.paths = paths;
	}
	
	public QueueResourceSpecification(String... paths) {
		this.paths = Arrays.asList(paths);
	}
	
	public String head() {
		return paths.isEmpty() ? null : paths.get(0);
	}

	public QueueResourceSpecification tail() {
		return new QueueResourceSpecification(
				paths.isEmpty() ? paths : paths.subList(1, paths.size()) );
	}

	@Override
	public String getRootResource() {
		return head();
	}

	@Override
	public Collection<String> getResources() {
		return paths;
	}

	@Override
	public int hashCode() {
		return paths.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueueResourceSpecification other = (QueueResourceSpecification) obj;
		if (paths == null) {
			if (other.paths != null)
				return false;
		} else if (!paths.equals(other.paths))
			return false;
		return true;
	}
	
	

}
