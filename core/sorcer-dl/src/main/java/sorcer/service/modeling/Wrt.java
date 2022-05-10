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
package sorcer.service.modeling;

import sorcer.service.*;

import java.util.List;

/**
 * @author Mike Sobolewski
 * The version of Wrt (with respect to) for sensitivity dependencios
 * to indicate copmnet discilmes used in transciscplinary name spaces.
 * May 5, 2022
 */
public class Wrt extends ArgSet {

	static final long serialVersionUID = 3305090121860355856L;

	public Wrt() {
		super();
		paths = new Paths();
	}

	public Wrt(Paths paths) {
		super();
		this.paths = paths;;
	}

	public Wrt(String... wrts) {
		this();
		for (String w : wrts) {
			paths.add(new Path(w));
		}
	}

	public Wrt(Slot... infos) {
		this();
		for (Slot info : infos) {
			paths.add(new Path(info.getName(), info.getDomain()));
		}
	}

	public Wrt(Path... objects) {
		this();
		for (Path arg : objects) {
			paths.add(arg);
		}
	}

	public Path getPath(String name) {
		for (Path p : paths) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public Wrt(List<String> names) {
		this();
		for (String w : names) {
			paths.add(new Path(w));
		}
	}

	public List<String> getWrtNames() {
		return paths.getPathNames();
	}

	public Wrt add(String name) {
			paths.add(new Path(name));
		return this;
	}

	public Wrt addAll(List<String> names) {
		for (String n : names) {
			paths.add(new Path(n));
		}
		return this;
	}

	public Wrt addAll(String... names) {
		for (String n : names) {
			paths.add(new Path(n));
		}
		return this;
	}

	public Wrt addAll(Wrt... wrts) {
		for (Wrt w : wrts) {
			paths.addAll(w.paths);
		}
		return this;
	}

	public static ArgSet asArgSet(Wrt wrt) {
		ArgSet as = new ArgSet();
		as.paths = wrt.paths;
		as.addAll(wrt);
		return as;
	}

	public static Wrt asWrt(ArgSet argSet) {
		Wrt wrt = new Wrt();
		wrt.paths = argSet.paths;
		wrt.addAll(argSet);
		return wrt;
	}

	public static Wrt asWrt(ArgList array) {
		Wrt wrt = new Wrt();
		for (Arg arg : array) {
			wrt.add(arg);
			wrt.paths.add(new Path(arg.getName()));
		}
		return wrt;
	}

	public static Wrt asWrt(Object[] array) {
		Wrt wrt = new Wrt();
		for (Object arg : array) {
			if (arg instanceof String) {
				wrt.paths.add(new Path(( String ) arg));
			} else if (arg instanceof Path) {
				wrt.paths.add(( Path ) arg);
			} else if (arg instanceof Evaluation) {
				wrt.add((Arg)arg);
				wrt.paths.add(new Path(((Arg)arg).getName()));
			}
		}
		return wrt;
	}

	public Path[] toArray() {
		Path[] sa = new Path[size()];
		return toArray(sa);
	}

}
