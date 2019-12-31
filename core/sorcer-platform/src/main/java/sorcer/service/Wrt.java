/*
 * Distribution Statement
 * 
 * This computer software has been developed under sponsorship of the United States Air Force Research Lab. Any further
 * distribution or use by anyone or any data contained therein, unless otherwise specifically provided for,
 * is prohibited without the written approval of AFRL/RQVC-MSTC, 2210 8th Street Bldg 146, Room 218, WPAFB, OH  45433
 * 
 * Disclaimer
 * 
 * This material was prepared as an account of work sponsored by an agency of the United States Government. Neither
 * the United States Government nor the United States Air Force, nor any of their employees, makes any warranty,
 * express or implied, or assumes any legal liability or responsibility for the accuracy, completeness, or usefulness
 * of any information, apparatus, product, or process disclosed, or represents that its use would not infringe privately
 * owned rights.
 */
package sorcer.service;

import sorcer.core.Tag;

import java.util.ArrayList;
import java.util.List;

public class Wrt extends ArrayList<String> {

	static final long serialVersionUID = 3305090121860355856L;

	public Wrt() {
		super();
	}

	public Wrt(int initialCapacity) {
		super(initialCapacity);
	}

	public Wrt(String... wrt) {
		for (String w : wrt)
			add(w);
	}

	public Wrt(List<String> wrtNames) {
		addAll(wrtNames);
	}


	public ArgSet asArgSet() {
		ArgSet as = new ArgSet();
		for (int i = 0; i < size(); i++) {
			as.add(new Tag(get(i)));
		}
		return as;
	}

	public String[] toArray() {
		String[] sa = new String[size()];
		return toArray(sa);
	}

}
