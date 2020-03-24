package sorcer.core.context;

import sorcer.service.Context;

import java.util.ArrayList;

public class ContextList extends ArrayList<Context> {

	public enum Type { MODEL, TRANS, MADO };

	private String name;

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

	private Type type;

}
