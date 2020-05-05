package sorcer.core.context;

import sorcer.service.Context;

import java.util.ArrayList;

public class ContextList extends ArrayList<Context> {

	public enum Type { MODEL, TRANS, MADO };

	private String name;

	private Type type;

	public ContextList () {

	}
	public ContextList (Context... contexts) {
		for(Context cxt : contexts) {
			add(cxt);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Context select(String domain) {
		for (Context cxt : this) {
			if (cxt.getName().equals(domain)) {
				return cxt;
			}
		}
		return null;
	}

	public Context set(Context domainContext) {
		Context existing = select(domainContext.getName());
		if (existing != null) {
			remove(existing);
		}
		add(domainContext);
		return domainContext;
	}

	public void remove(String domain) {
		Context existing = select(domain);
		if (existing != null) {
			remove(existing);
		}
	}

}
