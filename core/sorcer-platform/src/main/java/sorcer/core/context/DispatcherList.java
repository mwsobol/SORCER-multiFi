package sorcer.core.context;

import sorcer.service.Dispatcher;
import sorcer.service.Identifiable;

import java.util.ArrayList;

public class DispatcherList extends ArrayList<Dispatcher> {

	public enum Type { MODEL, ROuTINE, TRANS, MADO };

	private String name;

	private Type type;

	public DispatcherList() {

	}
	public DispatcherList(Dispatcher... dispatchers) {
		for(Dispatcher disp : dispatchers) {
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

	public Dispatcher select(String domain) {
		for (Dispatcher disp : this) {
			if (((Identifiable)disp).getName().equals(domain)) {
				return disp;
			}
		}
		return null;
	}

	public Dispatcher set(Dispatcher domainDispatcher) {
		Dispatcher existing = select(((Identifiable)domainDispatcher).getName());
		if (existing != null) {
			remove(existing);
		}
		add(domainDispatcher);
		return domainDispatcher;
	}

	public void remove(String domain) {
		Dispatcher existing = select(domain);
		if (existing != null) {
			remove(existing);
		}
	}

}
