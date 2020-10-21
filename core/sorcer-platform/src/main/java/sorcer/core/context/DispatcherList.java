package sorcer.core.context;

import sorcer.core.Dispatcher;
import sorcer.service.Dispatch;
import sorcer.service.Identifiable;

import java.util.ArrayList;

public class DispatcherList extends ArrayList<Dispatch> {

	public enum Type { MODEL, ROuTINE, TRANS, MADO };

	private String name;

	private Type type;

	public DispatcherList() {

	}
	public DispatcherList(Dispatch... dispatchers) {
		for(Dispatch disp : dispatchers) {
			add(disp);
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

	public Dispatch select(String domain) {
		for (Dispatch disp : this) {
			if (((Identifiable)disp).getName().equals(domain)) {
				return disp;
			}
		}
		return null;
	}

	public Dispatch set(Dispatch domainDispatcher) {
		Dispatch existing = select(((Identifiable)domainDispatcher).getName());
		if (existing != null) {
			remove(existing);
		}
		add(domainDispatcher);
		return domainDispatcher;
	}

	public void remove(String domain) {
		Dispatch existing = select(domain);
		if (existing != null) {
			remove(existing);
		}
	}

}
