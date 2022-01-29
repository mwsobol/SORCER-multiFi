/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.co;

import net.jini.id.Uuid;
import org.rioproject.resolver.Artifact;
import org.rioproject.resolver.ResolverException;
import org.rioproject.resolver.ResolverHelper;
import sorcer.Operator;
import sorcer.co.tuple.*;
import sorcer.core.Index;
import sorcer.core.Tag;
import sorcer.core.SorcerConstants;
import sorcer.core.context.*;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.context.model.ent.*;
import sorcer.core.plexus.FiEntry;
import sorcer.core.provider.DatabaseStorer;
import sorcer.core.service.Collaboration;
import sorcer.core.signature.LocalSignature;
import sorcer.core.signature.NetletSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.netlet.ServiceScripter;
import sorcer.service.*;
import sorcer.service.ContextDomain;
import sorcer.service.modeling.*;
import sorcer.service.modeling.Functionality.Type;
import sorcer.util.*;
import sorcer.util.bdb.objects.UuidObject;
import sorcer.util.url.sos.SdbUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Collections;
import java.util.concurrent.Callable;

import static sorcer.ent.operator.invoker;
import static sorcer.ent.operator.req;

/**
 * Created by Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class operator extends Operator {

	private static int count = 0;

	public static <T1> Tuple1<T1> x(T1 x1 ){
		return new Tuple1<T1>( x1 );
	}

	public static <T1,T2> Tuple2<T1,T2> x(T1 x1, T2 x2 ){
		return new Tuple2<T1,T2>( x1, x2 );
	}

	public static Slot<String,Object> fiVal(String x1, Object x2 ){
		return new Slot(x1, x2 );
	}

	public static <String,O> Slot<String,O> slot(String x1, O x2 ){
		return new Slot<String,O>(x1, x2 );
	}

	public static <T1,T2,T3> Tuple3<T1,T2,T3> x(T1 x1, T2 x2, T3 x3 ){
		return new Tuple3<T1,T2,T3>( x1, x2, x3 );
	}

	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> x(T1 x1, T2 x2, T3 x3, T4 x4 ){
		return new Tuple4<T1,T2,T3,T4>( x1, x2, x3, x4 );
	}

	public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> x(T1 x1, T2 x2, T3 x3, T4 x4, T5 x5 ){
		return new Tuple5<T1,T2,T3,T4,T5>( x1, x2, x3, x4, x5 );
	}

	public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> t(T1 x1, T2 x2, T3 x3, T4 x4, T5 x5 ){
		return new Tuple5<T1,T2,T3,T4,T5>( x1, x2, x3, x4, x5 );
	}

	public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> x(T1 x1, T2 x2, T3 x3, T4 x4, T5 x5, T6 x6 ){
		return new Tuple6<T1,T2,T3,T4,T5,T6>( x1, x2, x3, x4, x5, x6 );
	}

	public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> t(T1 x1, T2 x2, T3 x3, T4 x4, T5 x5, T6 x6 ){
		return new Tuple6<T1,T2,T3,T4,T5,T6>( x1, x2, x3, x4, x5, x6 );
	}

	public static Tie tie(String discipline, String var) {
		return new Tie(discipline, var);
	}

    public static Tie tie(String discipline) {
        return new Tie(discipline, null);
    }

    public static Coupling cplg( String var, String fromDiscipline, String toDiscipline) {
        return new Coupling(tie(fromDiscipline, var), tie(toDiscipline, var));
    }

    public static Coupling cplg(Tie from, Tie to) {
		return new Coupling(from, to);
	}

    public static <T> List<T> inCotextValues(Context<T> context) throws ContextException {
		return ((ServiceContext)context).getInValues();
	}

	public static <T> List<T> inContextPaths(Context<T> context) throws ContextException {
		return ((ServiceContext)context).getInPaths();
	}

	public static <T> List<T> outContextValues(Context<T> context) throws ContextException {
		return ((ServiceContext)context).getOutValues();
	}

	public static <T> List<T> outContextPaths(Context<T> context) throws ContextException {
		return ((ServiceContext)context).getOutPaths();
	}

	public static Context.Out outPaths(Object... elems) {
        Context.Out pl = new Context.Out(elems.length);
        for (Object o : elems) {
            if (o instanceof String) {
                pl.add(new Path((String)o));
            } else if  (o instanceof Path) {
                pl.add(((Path)o));
            }
        }
        if (elems.length == 1) {
            if (elems[0] instanceof Path) {
                pl.setName(((Path) elems[0]).path);
            } else {
                pl.setName((String) elems[0]);
            }
        }
        return pl;
	}

    public static Name cxtName(String name) {
        return contextName(name);
    }

    public static Name contextName(String name) {
	    return new Name(name);
    }

	public static Context.Out outPaths(Name name, Object... elems) {
		Context.Out out = outPaths(elems);
		out.setName(name.getName());
		return out;
	}

	public static Context.In inPaths(Object... elems) {
		List<Path> pl = new ArrayList(elems.length);
		for (Object o : elems) {
			if (o instanceof String) {
				pl.add(new Path((String)o));
			} else if  (o instanceof Path) {
				pl.add(((Path)o));
			}
		}
		Path[]  pa = new Path[pl.size()];
		return new Context.In(pl.toArray(pa));
	}

	public static ServiceSignature.Read read(Object... elems) {
		List<Path> pl = new ArrayList(elems.length);
		for (Object o : elems) {
			if (o instanceof String) {
				pl.add(new Path((String)o));
			} else if  (o instanceof Path) {
				pl.add(((Path)o));
			}
		}
		Path[]  pa = new Path[pl.size()];
		return new ServiceSignature.Read(pl.toArray(pa));
	}

	public static ServiceSignature.Write write(Object... elems) {
		List<Path> pl = new ArrayList(elems.length);
		for (Object o : elems) {
			if (o instanceof String) {
				pl.add(new Path((String)o));
			} else if  (o instanceof Path) {
				pl.add(((Path)o));
			}
		}
		Path[]  pa = new Path[pl.size()];
		return new ServiceSignature.Write(pl.toArray(pa));
	}

	public static ServiceSignature.Append append(Object... elems) {
		List<Path> pl = new ArrayList(elems.length);
		for (Object o : elems) {
			if (o instanceof String) {
				pl.add(new Path((String)o));
			} else if  (o instanceof Path) {
				pl.add(((Path)o));
			}
		}
		Path[]  pa = new Path[pl.size()];
		return new ServiceSignature.Append(pl.toArray(pa));
	}

    public static Signature.State state(Object... elems) {
        List<Path> pl = new ArrayList(elems.length);
        for (Object o : elems) {
            if (o instanceof String) {
                pl.add(new Path((String)o));
            } else if  (o instanceof Path) {
                pl.add(((Path)o));
            }
        }
        Path[]  pa = new Path[pl.size()];
        return new Signature.State(pl.toArray(pa));
    }

	public static Path filePath(String filename) {
		if(Artifact.isArtifact(filename)) {
			try {
				URL url = ResolverHelper.getResolver().getLocation(filename, "ntl");
				File file = new File(url.toURI());
				return new Path(file.getPath());
			} catch (ResolverException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return new Path (filename);
	}

	public static Object[] typeArgs(Object... args) {
		return args;
	}

	public static <T> T[] array(T... elems) {
		return elems;
	}

//	public static double[] vector(double... elems) {
//		return elems;
//	}

	public static int[] vector(int... elems) {
		return elems;
	}

	public static Object[] objects(Object... elems) {
		return elems;
	}

	public static <T> T[] array(List<T> list) {
		T[] na = (T[]) Array.newInstance(list.get(0).getClass(), list.size());
		return list.toArray(na);

	}

	public static Set<Object> bag(Object... elems) {
		return new HashSet<Object>(list(elems));
	}

	public static <T> Set<T> set(T... elems) {
		return new HashSet<T>(list(elems));
	}

	public static <T> List<T> list(T... elems) {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, elems);
		return list;
	}

	public static List<Object> vector(Object... elems) {
        List<Object> list = new ArrayList<>();
        Collections.addAll(list, elems);
        return list;
	}

    public static Row column(DataTable table, String column) throws ContextException {
        return new Row(table.getRowNames(), table.getColumn(column));
    }

    public static Row row(DataTable table, int index) throws ContextException {
        return new Row(table.getColumnNames(), table.getRow(index));
    }

    public static Row row(DataTable table, String row) throws ContextException {
		return new Row(table.getColumnNames(), table.getRow(row));
	}

	public static Row row(Entry...  entries) {
	    return new Row(entries);
    }

	public static Row row(Context context) throws ConfigurationException {
		Iterator it = ((ServiceContext)context).keyIterator();
		Row row = new Row();
		row.setName(context.getName());
		String path = null;
		Object obj;
		List<String> columnNames = new ArrayList<>();
		List<Object> rowValues = new ArrayList<>();
		while (it.hasNext()) {
			path = (String) it.next();
			obj = ((ServiceContext)context).get(path);
			columnNames.add(path);
			if (obj == null) {
				rowValues.add(Context.none);
			} else {
				rowValues.add(obj);
			}
		}
		row. setColumnIdentifiers(columnNames);
		row.addRow(rowValues);
		return row;
	}

    public static ServiceContext rowContext(Row row) {
        ServiceContext cxt = new ServiceContext();
        List<String> identifires = row.getColumnIdentifiers();
        List<Object> values = row.getRow(0);
        for (int i = 0; i < identifires.size(); i++) {
            cxt.put(identifires.get(i), values.get(i));
        }
        return cxt;
    }

	public static ServiceContext argsContext(sorcer.eo.operator.Args args) {
		ServiceContext cxt = new ServiceContext();
		for (int i = 0; i < args.args.length; i++) {
			if (args.args[i] instanceof String) {
				cxt.put((String) args.args[i], Context.none);
			} else if (args.args[i] instanceof Identifiable) {
				cxt.put(((Identifiable) args.args[i]).getName(), args.args[i]);
			}
		}
		return cxt;
	}

	public static boolean isCkpt(MultiFiSlot slot) {
		return slot.getCheckpoint() != null;
	}

	public static boolean isCkpt(ContextDomain slot) {
		return ((MultiFiSlot)slot).getCheckpoint() != null;
	}

	public static Checkpoint ckpt(String name) {
		return new Checkpoint(name);
	}

	public static Checkpoint ckpt(String name, int iteration) {
		return new Checkpoint(name, iteration);
	}

	public static Checkpoint ckpt() {
		return new Checkpoint();
	}

	public static Checkpoint ckpt(boolean status) {
		return new Checkpoint(status);
	}

	public static List<Object> values(Row response) {
		return response.getValues();
	}

	public static List<String> names(Row response) {
		return response.getNames();
	}

	public static List<Object> values(Object... elems) {
        List<Object> list = new ArrayList<>();
        Collections.addAll(list, elems);
        return list;
	}

	public static List<String> header(String... elems) {
		List<String> out = new Header<String>(elems.length);
		for (String each : elems) {
			out.add(each);
		}
		return out;
	}

	public static Tokens os(Object... items) {
		Tokens out = new Tokens(items.length);
		for (Object each : items) {
			out.add(each);
		}
		out.setType("OS");
		return out;
	}

	public static Tokens app(Object... items) {
		Tokens out = new Tokens(items.length);
		for (Object each : items) {
			out.add(each);
		}
		out.setType("APP");
		return out;
	}

	public static Tokens match(Object... items) {
		Tokens out = new Tokens(items.length);
		for (Object each : items) {
			out.add(each);
		}
		out.setType("LIST");
		return out;
	}


	public static List<String> names(String... elems) {
        List<String> list = new Header<>();
        Collections.addAll(list, elems);
		return list;
	}

	public static List<String> names(List<String>... nameLists) {
		List<String> out = new Header<String>();
		for (List<String> each : nameLists) {
			out.addAll(each);
		}
		return out;
	}

	public static RC rc(Integer numRows, Integer numCols, boolean rowMajor) {
		return new RC(numRows, numCols, rowMajor);
	}

	public static RC rc(Integer numRows, Integer numCols) {
		return new RC(numRows, numCols);
	}

	public static <T1, T2> Tuple2<T1, T2> assoc(T1 x1, T2 x2) {
		return new Tuple2(x1, x2);
	}

	public static <T1, T2> Tuple2<T1, T2> assn(T1 x1, T2 x2) {
		return new Tuple2(x1, x2);
	}

	public static String path(List<String> attributes) {
		if (attributes.size() == 0)
			return null;
		if (attributes.size() > 1) {
			StringBuilder spr = new StringBuilder();
			for (int i = 0; i < attributes.size() - 1; i++) {
				spr.append(attributes.get(i)).append(SorcerConstants.CPS);
			}
			spr.append(attributes.get(attributes.size() - 1));
			return spr.toString();
		}
		return attributes.get(0);
	}

    public static Path from(String path) {
        Path p = new Path(path);
        p.direction = Signature.Direction.FROM;
        return p;
    }

	public static Path to(String path) {
		Path p = new Path(path);
		p.direction = Signature.Direction.TO;
		return p;
	}

    public static Path prePath(String path) {
        Path p = new Path(path);
        p.type = Path.Type.PRE;
        return p;
    }

    public static Path periPath(String path) {
        Path p = new Path(path);
        p.type = Path.Type.PERI;
        return p;
    }

    public static Path postPath(String path) {
        Path p = new Path(path);
        p.type = Path.Type.POST;
        return p;
    }

	public static Path path(String path) {
        Path p = new Path(path);
        p.type = Path.Type.PERI;
        return p;
	}

	public static Path path(String path, Object info, Path... paths) {
		if (info instanceof Path && paths.length == 0) {
			Path p = new Path(path);
			p.dirPath = (Path) info;
			p.type = Path.Type.MAP;
			return p;
		} else if (info instanceof String && paths.length == 1) {
            Path p = new Path(path, info, Path.Type.MAP);
            p.dirPath = paths[0];
            return p;
        } else if (paths.length > 1) {
            Path p = new Path(path);
            p.dirPath = (Path) info;
            p.type = Path.Type.MAP;
            for (Path dp : paths) {
                if (dp.type.equals(dp.type.PRE)) {
                    p.prePaths.add(dp);
                } else if (dp.type.equals(dp.type.POST)) {
                    p.postPaths.add(dp);
                }
            }
        }
		return new Path(path, info);
	}

	public static Path map(String path, Object info) {
		return new Path(path, info, Path.Type.MAP);
	}

	public static <T> Value<T> predVal(String path, String domain, T value) {
		Value ent = new Value<T>(path, value);
		ent.setDomain(domain);
		ent.setType(Type.DOMAIN_PRED);
		return ent;
	}

	public static <T> Value<T> predVal(String domainPath, T value) {
	    String pn = domainPath;
	    String domain = null;
        if (domainPath.indexOf("$") > 0) {
            int ind = domainPath.indexOf("$");
            pn = domainPath.substring(0, ind);
            domain = domainPath.substring(ind + 1);
        }
        Value ent = null;
        if (domain != null) {
            ent = predVal(pn, domain, value);
        } else {
            ent = new Value<T>(pn, value);
        }
		ent.setType(Type.PRED);
		return ent;
	}

	public static <T> Value<T> depVal(String path, String domain, T value) {
		Value ent = new Value<T>(path, value);
		ent.setDomain(domain);
		ent.setType(Type.DOMAIN_DEP);
		return ent;
	}

	public static <T> Value<T> depVal(String domainPath, T value) {
		String pn = domainPath;
		String domain = null;
		if (domainPath.indexOf("$") > 0) {
			int ind = domainPath.indexOf("$");
			pn = domainPath.substring(0, ind);
			domain = domainPath.substring(ind + 1);
		}
		Value ent = null;
		if (domain != null) {
			ent = depVal(pn, domain, value);
		} else {
			ent = new Value<T>(pn, value);
			ent.setType(Type.DEP);
		}
		return ent;
	}

	public static <T> Value<T> val(String path, String domain, T value) {
		Value ent = new Value<T>(path, value);
		ent.setDomain(domain);
		ent.setType(Type.DOMAIN_CONSTANT);
		return ent;
	}

	public static <T> Value<T> val(String path, T value) {
		Value ent = null;
		if (value instanceof Value) {
            ent = new Value<T>(path, (T) ((Value)value).getOut());
        } else {
            ent = new Value<T>(path, value);
        }
        ent.setType(Type.VAL);
        return ent;
	}

	public static <T> Value<T> val(String path, Entry... subvalues) {
		Value ent = new Value<T>(path);
		ent.appendSubvalues(subvalues);
		ent.setType(Type.SUPERVAL);
		return ent;
	}

	public static <T> Value<T> val(Path path, T value) {
        Value ent = null;
        if (value instanceof Value) {
            ent = new Value<T>(path.path, (T) ((Value)value).getOut());
            ent.setImpl(value);
        } else {
            ent = new Value<T>(path.path, value);
        }
		ent.annotation(path.info.toString());
		ent.setType(Type.VAL);
		return ent;
	}

	public static Config config(Object path, Setup... entries) {
		Config ent = new Config(path.toString(), entries);
		ent.setValid(false);
		ent.setType(Type.CONFIG);
		return ent;
	}

	public static Setup setup(Object aspect, Context value) {
		Setup ent = new Setup(aspect.toString(), value);
		ent.isValid(false);
//		ent.setType(Type.INPUT);
		return ent;
	}

	public static Setup setup(Object aspect, Entry... entries) throws ContextException {
		return setup(aspect, null, null, entries);
	}

	public static Setup setup(Object aspect, String entryName, Entry... entries) throws ContextException {
		return setup(aspect, entryName, null, entries);
	}

	public static Setup setup(Object aspect, String entryName, String fiName, Entry... entries) throws ContextException {
		if (entries != null && entries.length > 0) {
			ServiceContext cxt;
			if (entryName == null)
				cxt = new ServiceContext(aspect.toString());
			else
				cxt = new ServiceContext(entryName);

			if (fiName != null) {
				cxt.setSubjectPath(fiName);
			}

			for (Entry e : entries) {
				cxt.put(e.getName(), e.getOut());
			}
			cxt.setValid(false);
			return new Setup(aspect.toString(), cxt);
		} else {
			return new Setup(aspect.toString(), null);
		}
	}

	public static List<Setup> setups(Object aspect, List<String> entryName, String fiName, Entry... entries) throws ContextException {
		List<Setup> setups   = new ArrayList<>();
		Setup setup = null;
		for (String entName : entryName) {
			setup = setup(aspect, entName, fiName, entries);
			setups.add(setup);
		}
		return setups;
	}

    public static Uuid id(Mogram mogram) {
        return ((ServiceMogram)mogram).getId();
    }

    public static void setId(Mogram mogram, Uuid id) {
		((ServiceMogram)mogram).setId(id);
    }

	public static Entry in(Entry... entries) {
		for (Entry entry : entries) {
			entry.setType(Type.INPUT);
		}
		return entries[0];
	}

	public static Entry out(Entry... entries) {
		for (Entry entry : entries) {
			entry.setType(Type.OUTPUT);
		}
		return entries[0];
	}

	public static Entry inout(Entry... entries) {
		for (Entry entry : entries) {
			entry.setType(Type.INOUT);
		}
		return entries[0];
	}

	public static Object annotation(Entry entry) {
        return entry.annotation();
    }

	public static Signature.Direction direction(Entry entry) {
		Object ann = entry.annotation();
		if (ann instanceof String)
			return Signature.Direction.fromString((String) ann);
		else
			return (Signature.Direction) ann;
	}

    public static boolean isSorcerLambda(Class clazz) {
		Class[] types = { EntryCollable.class, ValueCallable.class, Client.class,
				ConditionCallable.class, Callable.class };
		for (Class cl : types) {
			if (clazz == cl) {
				return true;
			}
		}
		return false;
	}

    public static ExecDependency dep(String path, Path... paths) {
		return new ExecDependency(path, Arrays.asList(paths));
	}

	public static ExecDependency dep(String path, Conditional condition, Path... paths) {
        ExecDependency de = new ExecDependency(path, condition, Arrays.asList(paths));
        de.setType(Type.CONDITION);
        return de;
	}

	public static ExecDependency dep(String path, Fidelity fi, Path... paths) {
		ExecDependency de = new ExecDependency(path, Arrays.asList(paths));
		de.annotation(fi);
		de.setType(Functionality.Type.FIDELITY);
        return de;
	}

	public static class ExecDeps implements Arg {
		private static final long serialVersionUID = 1L;

		public String name;

		public ExecDependency[] deps;

		public ExecDeps(ExecDependency[] deps) {
			this.deps = deps;
		}

		@Override
		public String getName() {
			return name;
		}

		public Functionality.Type getType() {
		    if (deps.length > 0) {
                return deps[0].getType();
            }
            return Functionality.Type.NONE;
        }

		@Override
		public Object execute(Arg... args) throws ServiceException, RemoteException {
			return this;
		}
	}

	public static Object subject(Context context) {
		return context.getSubjectValue();
	}

	public static Collaboration collab(Context context) {
		Object subject = context.getSubjectValue();
		if (subject instanceof Collaboration) {
			return (Collaboration)subject;
		} else {
			return null;
		}
	}

	public static Paths disciplines(String... disciplines) {
		Paths paths = new Paths(disciplines);
		paths.type = Type.DISCIPLINE;
		return paths;
	}

	public static Paths domains(String... domains) {
		Paths paths = new Paths(domains);
		paths.type = Type.DISCIPLINE;
		return paths;
	}

	public static Paths regions(String... disciplines) {
		Paths paths = new Paths(disciplines);
		paths.type = Type.REGION;
		return paths;
	}

	public static ExecDeps deps(ExecDependency... deps) {
		return new ExecDeps(deps);
	}

	public static ExecDeps deps(String name, ExecDependency... deps) {
		ExecDeps out = new ExecDeps(deps);
		out.name = name;
		return out;
	}

	public static ExecDependency dep(String path, Conditional condition, List<Path> paths) {
        ExecDependency de = new ExecDependency(path, condition, paths);
        de.setType(Type.CONDITION);
        return de;
	}

	public static ExecDependency idfDep(String fiName, List<Path> paths) {
		ExecDependency de =  new ExecDependency(fiName, paths);
		de.setType(Type.IDF);
		return de;
	}

	public static ExecDependency mdfDep(String fiName, List<Path> paths) {
		ExecDependency de =  new ExecDependency(fiName, paths);
		de.setType(Type.MDF);
		return de;
	}

	public static ExecDependency fiDep(String fiName, List<Path> paths) {
		ExecDependency de =  new ExecDependency(fiName, paths);
		de.setType(Type.FIDELITY);
		return de;
	}

	public static ExecDependency domDep(String path, List<Path> paths) {
        ExecDependency de =  new ExecDependency(path, paths);
        de.setType(Type.DOMAIN);
        return de;
    }

    public static ExecDependency domDep(String path, Fidelity fi, List<Path> paths) {
        ExecDependency de = new ExecDependency(path, paths);
        de.annotation(fi);
        de.setType(Type.DOMAIN);
        return de;
    }

    public static ExecDependency mdlDep(Evaluation... evaluations) {
        ExecDependency de = new ExecDependency();
        de.setImpl(evaluations);
        de.setType(Type.MODEL);
        return de;
    }

    public static ExecDependency mdlDep(String path, Evaluation... evaluations) {
        ExecDependency de = new ExecDependency(path);
        de.setImpl(evaluations);
        de.setType(Type.MODEL);
        return de;
    }

	public static ExecDependency dep(String path, List<Path> paths) {
        ExecDependency de =  new ExecDependency(path, paths);
        de.setType(Type.FUNCTION);
        return de;
	}

    public static ExecDependency dep(String path, Fidelity fi, List<Path> paths) {
        ExecDependency de = new ExecDependency(path, paths);
        de.annotation(fi);
        de.setType(Type.FIDELITY);
        return de;
    }

	public static ExecDependency dep(Paths svrPaths, Fidelity fi, List<Path> paths) {
		ExecDependency de = new ExecDependency(svrPaths, paths);
		de.annotation(fi);
		de.setType(Type.FIDELITY);
		return de;
	}

	public static Value<Object> val(String path) {
		Value ent = new Value(path, null);
		ent.setType(Type.VAL);
		return ent;
	}

	public static Object val(Setup ent, String path) throws ContextException {
		return  ent.getContextValue(path);
	}

    public static <T> OutputValue<T> outVal(String path, T value) {
		if (value instanceof String && ((String)value).indexOf('|') > 0) {
			OutputValue oe =  outVal(path, null);
			oe.annotation(value);
			return oe;
		}
		OutputValue ent = new OutputValue(path, value, 0);
		if (value instanceof Class)
			ent.setValClass((Class) value);
		return ent;
	}

	public static <T> OutputValue<T> outVal(String path, T value, String annotation) {
		OutputValue oe =  outVal(path, value);
		oe.annotation(annotation);
		return oe;
	}

	public static class DataSlot<T> extends Slot<String, T> implements  Valuation<T> {
		private static final long serialVersionUID = 1L;

		DataSlot(String path, T value) {
			T v = value;
			if (v == null)
				v = (T) Context.none;

			this.setKey(path);
			this.set(v);
		}

		@Override
		public T valuate(Arg... args) throws ContextException {
			return out;
		}

		@Override
		public void set(Object value) {
			out = (T) value;
		}

	}

	public static DataSlot data(Object data) {
		return new DataSlot(Context.DSD_PATH, data);
	}

	public static <T> OutputValue<T> outVal(String path, T value, int index) {
		return new OutputValue(path, value, index);
	}

	public static <T> OutputValue<T> dbOutVal(String path, T value) {
		return new OutputValue(path, value, true, 0);
	}

	public static InputValue input(String path) {
		return new InputValue(path, null, 0);
	}

	public static OutputValue outVal(String path) {
		return new OutputValue(path, null, 0);
	}

	public static InputValue inVal(String path) {
		return new InputValue(path, null, 0);
	}

	public static Function at(String path, Object value) {
		return new Function(path, value, 0);
	}

	public static Function at(String path, Object value, int index) {
		return new Function(path, value, index);
	}

	public static <T> InputValue<T> inVal(String path, T value) {
		return new InputValue(path, value, 0);
	}

	public static <T> InputValue<T> inVal(Fidelity<Path> multiFipath, T value) {
		InputValue mpe = new InputValue(multiFipath.getSelect().getName(), value, 0);
		mpe.setMultiFiPath(multiFipath);
		multiFipath.setName(multiFipath.getSelect().path);
		multiFipath.setPath(mpe.getName());
		return mpe;
	}

	public static <T> InputValue<T> dbInVal(String path, T value, String annotation) {
		InputValue<T> ie = new InputValue(path, value, true, 0);
		ie.annotation(annotation);
		return ie;
	}

	public static <T> InputValue<T> dbInVal(String path, T value) {
		return new InputValue(path, value, true, 0);
	}

	public static <T> InputValue<T> inVal(String path, T value, int index) {
		return new InputValue(path, value, index);
	}

	public static <T> InputValue<T> inVal(String path, T value, String annotation) {
		InputValue<T> ie = inVal(path, value);
		ie.annotation(annotation);
		return ie;
	}

	public static <T> InputValue<T> inVal(String path, T value, Class valClass, String annotation) {
		InputValue<T> ie = new InputValue(path, value, 0);
		if (valClass != null)
			ie.setValClass(valClass);
		ie.annotation(annotation);
		return ie;
	}

	public static <T> InputValue<T> inVal(String path, T value, Class valClass) {
		return inVal(path, value, valClass, null);
	}

	public static <S extends Setter> boolean isPersistent(S setter) {
		return setter.isPersistent();
	}

	public static URL storeVal(Object object) throws EvaluationException {
		URL dburl = null;
		Entry entry = null;
		try {
			if (object instanceof Entry) {
				entry = (Entry)	object;
				Object obj = entry.getData();
				if (SdbUtil.isSosURL(obj)) {
					dburl = (URL) obj;
					SdbUtil.update(dburl, entry.getOut());
				}
				else {
					entry.setPersistent(true);
					dburl = SdbUtil.store(entry.getOut());
				}
			}
			entry.setOut(null);
			entry.setImpl(dburl);
			entry.setValid(true);
			return dburl;
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	}

	public static URL store(Object object) throws EvaluationException {
		try {
			if (object instanceof UuidObject)
				return SdbUtil.store(object);
			else  {
				return SdbUtil.store(new UuidObject(object));
			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	}

	public static URL dbURL() throws MalformedURLException {
		return new URL(Sorcer.getDatabaseStorerUrl());
	}

	public static URL dsURL() throws MalformedURLException {
		return new URL(Sorcer.getDataspaceStorerUrl());
	}

	public static void dbURL(Object object, URL dbUrl)
			throws MalformedURLException {
		if (object instanceof Prc)
			((Prc) object).setDbURL(dbUrl);
		else if (object instanceof ServiceContext)
			((ServiceContext) object).setDbUrl("" + dbUrl);
		else
			throw new MalformedURLException("Can not set URL to: " + object);
	}

	public static URL dbURL(Object object) throws MalformedURLException {
		if (object instanceof Prc)
			return ((Prc) object).getDbURL();
		else if (object instanceof ServiceContext)
			return new URL(((ServiceContext) object).getDbUrl());
		return null;
	}

	public static Object retrieve(URL url) throws IOException {
		return url.getContent();
	}

	public static URL update(Object object) throws ServiceException,
		SignatureException, RemoteException {
		return SdbUtil.update(object);
	}

	public static List<String> list(URL url) throws ServiceException, SignatureException {
		return SdbUtil.list(url);
	}

	public static List<String> list(DatabaseStorer.Store store) throws ServiceException, SignatureException {
		return SdbUtil.list(store);
	}

	public static URL delete(Object object) throws ServiceException, SignatureException, RemoteException {
		return SdbUtil.delete(object);
	}

	public static int clear(DatabaseStorer.Store type) throws ServiceException, SignatureException, RemoteException {
		return SdbUtil.clear(type);
	}

	public static int size(Collection collection) {
		return collection.size();
	}

	public static int size(Fi fidelity) {
		return fidelity.getSelects().size();
	}

	public static int size(Model model) {
		return ((ServiceContext)model).size();
	}

	public static int size(Context context) {
		return context.size();
	}

	public static int size(Map map) {
		return map.size();
	}

	public static int size(DatabaseStorer.Store type) throws ServiceException,
		SignatureException, RemoteException {
		return SdbUtil.size(type);
	}

	public static <T extends Entry> T persistent(T entry) {
		entry.setPersistent(true);
		return entry;
	}

	public static <T> Entry<T> dbVal(String path) {
		Entry<T> e = new Prc<T>(path);
		e.setPersistent(true);
		return e;
	}

	public static <T> Entry<T> dbVal(String path, T value) throws EvaluationException {
		Prc<T> e = new Prc<T>(path, value);
		e.setPersistent(true);
		if (SdbUtil.isSosURL(value)) {
			e.setImpl(value);
			e.setOut(null);
		}
		return e;
	}

    public static URL storeVal(ContextDomain context, String path) throws EvaluationException {
		URL dburl = null;
		try {
			Object v = context.asis(path);
			if (SdbUtil.isSosURL(v))
				return (URL) v;
			else if (v instanceof Setter && v instanceof Evaluation) {
				Object nv = ((Evaluation)v).asis();
				if (SdbUtil.isSosURL(nv)) {
					return (URL) nv;
				}
				((Entry) v).setPersistent(true);
				((Evaluation)v).evaluate();
				dburl = (URL) ((Evaluation)v).asis();
			} else if (context.asis(path) instanceof Entry) {
				Entry dbe = new Entry(path, context.asis(path));
				dbe.setPersistent(true);
				dbe.getValue();
				((Context)context).putValue(path, dbe);
				dburl = (URL) dbe.asis();
			} else {
				dburl = store(v);
				((Context)context).putValue(path, dburl);
			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
		return dburl;
	}

	public static StrategyEntry strategyEnt(String name, Strategy strategy) {
		return new StrategyEntry(name, strategy);
	}

	public static <K, V> String key(Tuple2<K, V> entry) {
		return entry.getName();
	}

	public static <K, V> String path(Tuple2<K, V> entry) {
		return entry.getName();
	}

	public static <K, V> V val(Tuple2<K, V> entry) {
		return entry.value();
	}

	public static <V> V val(Entry<V> entry) throws ContextException {
		return entry.getData();
	}

	public static <V> String key(Entry<V> entry) {
		return entry.getName();
	}

	public static <V> String path(Entry<V> entry) {
		return entry.getName();
	}

    public static DataTable dataTable(Object... elems)  {
        int count = elems.length;
        DataTable out = null;
        int start = 0;
        List<Object> columnIds = null;
        if ((elems[0] instanceof Header)) {
            start = 1;
            columnIds = (List)elems[0];
        }
        List<Integer> rowIds = new ArrayList<>(count - 1);
        out = new DataTable(columnIds, count - 1);
        for (int i = start; i < count; i++) {
            out.addRow((List) elems[i]);
            rowIds.add(i - 1);
        }
        out.setRowIdentifiers(rowIds);
        return out;
    }

	public static OutType out(Type type) {
		return new OutType(type);
	}

	public static FilterId fId(String id) {
		return filterId(id);
	}

	public static FilterId filtId(String id) {
		return filterId(id);
	}

	public static FilterId filterId(String id) {
		return fId(id, null);
	}

	public static FilterId fId(String id, Object info) {
		return new FilterId(id, info);
	}

    public static String fi(Object object) {
	    if (object instanceof Entry) {
	        return ((Entry)object).fiName();
        } else {
            return object.toString();
        }
    }

    public static DataTable fiColumnName(DataTable table, String name) {
		table.setFiColumnName(name);
		return table;
	}

	public static ModelTable populateFidelities(ModelTable table, FiEntry... entries) {
		DataTable impl = (DataTable)table;
		List fiColumn = impl.getColumn(impl.getFiColumnName());
		if (fiColumn == null) {
			fiColumn = new ArrayList(impl.getRowCount());
			while(fiColumn.size() < impl.getRowCount())
				fiColumn.add(null);
		}
		for (FiEntry fiEnt : entries) {
			fiColumn.set(fiEnt.getIndex(), fiEnt.getFidelities());
		}

        for (int i = 0; i < fiColumn.size()-1; i++) {
            if (fiColumn.get(i) != null && fiColumn.get(i + 1) == null) {
                fiColumn.set(i + 1, fiColumn.get(i));
            }
        }

        impl.addColumn(impl.getFiColumnName(), fiColumn);
        return impl;
	}

	public static ModelTable appendFidelities(ModelTable table, FiEntry... entries) {
		DataTable impl = (DataTable)table;
		List fiColumn = impl.getColumn(impl.getFiColumnName());
		if (fiColumn == null) {
			fiColumn = new ArrayList(impl.getRowCount());
			while(fiColumn.size() < impl.getRowCount())
				fiColumn.add(null);
		}
		for (FiEntry fiEnt : entries) {
			fiColumn.set(fiEnt.getIndex(), fiEnt.getFidelities());
		}
		impl.addColumn(impl.getFiColumnName(), fiColumn);
		return impl;
	}

	public static void rowNames(DataTable table, List rowIdentifiers) {
		table.setRowIdentifiers(rowIdentifiers);
	}

	public static List<String> rowNames(DataTable table) {
		return table.getRowNames();
	}


	public static void columnNames(DataTable table, List columnIdentifiers) {
		table.setColumnIdentifiers(columnIdentifiers);
	}

	public static List<String> columnNames(DataTable table) {
		return table.getColumnNames();
	}

	public static int rowSize(DataTable table) {
		return table.getRowCount();
	}

	public static int columnSize(DataTable table) {
		return table.getColumnCount();
	}

	public static Map<String, Object> rowMap(DataTable table, String rowName) {
		return table.getRowMap(rowName);
	}

	public static Object get(DataTable table, String rowName, String columnName) {
		return table.getValue(rowName, columnName);
	}

    public static ent setValue(ent entry, Object value) throws ContextException {
        return setValue((Entry)entry, value);
    }

	public static Request setValue(Request request, String path, Object value) throws ContextException {
		if (request instanceof Contextion) {
			if (request instanceof Context) {
				((Context)request).putValue(path, value);
			} else {
				try {
					((Contextion) request).getOutput().putValue(path, value);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		return request;
	}

    public static Entry setValue(Entry entry, Object value) throws ContextException {
		entry.setValue(value);
        if (entry instanceof Prc) {
            Prc callEntry = (Prc)entry;
            if (callEntry.getScope() != null) {
                callEntry.getScope().putValue(callEntry.getName(), value);
            }
        }
        entry.setValid(true);
        entry.setChanged(true);
        return entry;
    }

    public static Setup setValue(Setup entry, String contextPath, Object value) throws ContextException {
        entry.setEntry(contextPath, value);
        return entry;
    }

    public static Setup setValue(Setup entry, Value... entries) throws ContextException {
        for (Value e :  entries) {
            entry.setEntry(e.getName(), e.getValue());
        }
        return entry;
    }

    public static Object get(DataTable table, int row, int column) {
		return table.getValueAt(row, column);
	}

    public static Object get(Response response, String path, Arg... args) throws ContextException {
		try {
			return response.getValue(path, args);
		} catch (RemoteException e) {
			throw new ContextException(e);
		}
	}

    public static Object get(Row response, int index)  {
        return response.get(index);
    }

	public static <T> T get(Valuation<T> valuation) throws ContextException {
		return valuation.valuate();
	}

	public static <T> T value(Valuation<T> valuation) throws ContextException {
		return valuation.valuate();
	}

	public static Object value(Row row, String item) throws ContextException {
		try {
			return row.getValue(item);
		} catch (RemoteException e) {
			throw new ContextException(e);
		}
	}

	public static Object evalValue(Mogram mogram, String path) throws MogramException {
		return ((ServiceMogram)mogram).getEvaluatedValue(path);
	}

	public static <T> T v(Valuation<T> valuation) throws ContextException {
		return valuation.valuate();
	}

	public static Object get(Context context) throws ContextException,
		RemoteException {
		return context.getReturnValue();
	}

	public static Object get(Context context, int index)
		throws ContextException {
		if (context instanceof PositionalContext)
			return ((PositionalContext) context).getValueAt(index);
		else
			throw new ContextException("Not PositionalContext, index: " + index);
	}

	public static Object get(Service service, String path)
		throws ContextException, RemoteException {
		Object obj = null;
		if (service instanceof Context) {
			obj = ((ServiceContext) service).get(path);
			if (obj != null && obj instanceof Contextion) {
				while (obj instanceof Contextion ||
					(obj instanceof Reactive && ((Reactive) obj).isReactive())) {
					try {
						obj = ((Evaluation) obj).asis();
					} catch (RemoteException e) {
						throw new ContextException(e);
					}
				}
			}
		} else if (service instanceof Mogram) {
			obj = (((Mogram) service).getContext()).asis(path);
		}
		return obj;
	}

	public static Object get(Routine exertion, String component, String path)
		throws RoutineException {
		Routine c = (Routine) exertion.getMogram(component);
		try {
			return get(c, path);
		} catch (Exception e) {
			throw new RoutineException(e);
		}
	}
	public static Object get(Arg[] args, String path) throws EvaluationException {
		for (Arg arg : args) {
			if (arg instanceof sorcer.service.Callable && arg.getName().equals(path)) {
				try {
					return ((sorcer.service.Callable)arg).call(args);
				} catch (RemoteException e) {
					throw new EvaluationException(e);
				}
			}
		}
		return null;
	}

	public static <T extends Object> ListContext<T> listContext(T... items) throws ContextException {
		ListContext<T> lc = new ListContext<>();
		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				lc.add(items[i]);
			}
		}
		return lc;
	}

    public static Map<Object, Object> dictionary(Tuple2... entries) {
        Map<Object, Object> map = new HashMap<>();
		if (entries != null) {
			for (Tuple2 entry : entries) {
				map.put(entry.getName(), entry.value());
			}
		}
        return map;
    }

	public static Map<Object, Object> dictionary(Entry... entries) {
		Map<Object, Object> map = new HashMap<>();
		if (entries != null) {
			for (Entry entry : entries) {
				map.put(entry.getName(), entry.getImpl());
			}
		}
		return map;
	}

	public static <T extends Identifiable> Pool<String, T> pool(Fi.Type type, T... entries) {
		Pool<String, T> map = new Pool<>();
		if (entries != null) {
			map.setFiType(type);
			for (T entry : entries) {
				map.put(entry.getName(), entry);
			}
		}
		return map;
	}

	public static <T extends Identifiable> Pool<String, T> pool(T... entries) {
		Pool<String, T> map = new Pool<>();
		if (entries != null) {
			for (T entry : entries) {
				map.put(entry.getName(), entry);
			}
		}
		return map;
	}

	public static <K, V> Pool<K, V> entPool(Fi.Type type, Tuple2<K, V>... entries) {
		Pool<K, V> map = new Pool<>();
		if (entries != null) {
			map.setFiType(type);
			for (Tuple2<K, V> entry : entries) {
				map.put(entry._1, entry._2);
			}
		}
		return map;
	}

	public static <K, V> Pool<K, V> entPool(Fi.Type type, Slot<K, V>... entries) {
		Pool<K, V> map = new Pool<>();
		if (entries != null) {
			map.setFiType(type);
			for (Slot<K, V> entry : entries) {
				map.put(entry.getKey(), entry.getOut());
			}
		}
		return map;
	}

	public static <K, V> Pool<K, V> entPool(Tuple2<K, V>... entries) {
		Pool<K, V> map = new Pool<>();
		if (entries != null) {
			for (Tuple2<K, V> entry : entries) {
				map.put(entry._1, entry._2);
			}
		}
		return map;
	}

	public static <K, V> Pool<K, V> entPool(Slot<K, V>... entries) {
		Pool<K, V> map = new Pool<>();
		if (entries != null) {
			for (Slot<K, V> entry : entries) {
				map.put(entry.getKey(), entry.getOut());
			}
		}
		return map;
	}

	public static <K, V> Map<K, V> map(Tuple2<K, V>... entries) {
		Map<K, V> map = new HashMap<K, V>();
		if (entries != null) {
			for (Tuple2<K, V> entry : entries) {
				map.put(entry.key(), entry.value());
			}
		}
		return map;
	}

	public static <K, V> Map<K, V> map(Slot<K, V>... entries) throws ContextException {
		Map<K, V> map = new HashMap<K, V>();
		if (entries != null) {
			for (Slot<K, V> entry : entries) {
				map.put(entry.getKey(), entry.getData());
			}
		}
		return map;
	}

	public static Object rasis(Entry entry)
			throws ContextException {
		String path = entry.getName();
		Object o = asis(entry);
		while (o instanceof Function && ((Entry)o).getKey().equals(path)) {
			o = asis((Function)o);
		}
		return o;
	}

	public static Object get(Function entry) throws ContextException {
		return rasis(entry);
	}

	public static Object impl(Entry entry) {
		return entry.getImpl();
	}

	public static Entry setImpl(Entry entry, Object impl) {
		 entry.setImpl(impl);
		 entry.setOut(null);
		 return entry;
	}

//	public static Object impl(Model context, String contextReturn)
//			throws ContextException {
//		return impl((ServiceContext) context,  contextReturn);
//	}

	public static Object rimpl(Context context, String path) {
		Object obj = ((ServiceContext)context).get(path);
		if (obj instanceof Entry) {
			return ((Entry)((ServiceContext)context).get(path)).getImpl();
		} else {
			return null;
		}
	}

	public static Object impl(Mogram context, String path) {
		Object obj = ((ServiceContext)context).get(path);
		if (obj instanceof MultiFiSlot) {
			return ((Entry)obj).getImpl();
		} else {
			return obj;
		}
	}

	public static Object output(Slot entry) {
		return entry.getOut();
	}

	public static Object asis(Entry entry) {
		return entry.asis();
	}

	public static Object asis(Function entry) {
		return entry.asis();
	}

	public static Object rasis(Context context, String path) throws ContextException {
		Object o = context.asis(path);
		if (o instanceof Function)
			return rasis((Function)o);
		else
			return o;
	}

	public static Object asis(Context context, String path) {
		return ((ServiceContext)context).get(path);
	}

	public static Object asis(Model model, String path) throws ContextException {
		try {
			return  model.asis(path);
		} catch (RemoteException e) {
			throw new ContextException(e);
		}
	}

    public static Copier copier(ContextDomain fromContext, Arg[] fromEntries,
                                ContextDomain toContext, Arg[] toEntries) throws EvaluationException {
        return new Copier(fromContext, fromEntries, toContext, toEntries);
    }

    public static Paths eachSrv(Object... paths) {
        Paths list = createPaths(paths);
        list.type = Type.SRV;
        return list;
    }

	public static Paths dualPaths(Object... paths) {
		Paths list = createPaths(paths);
		list.type = Type.DUAL;
		return list;
	}

    public static Paths paths(Object... paths) {
        Paths list = createPaths(paths);
        list.type = Type.PATH;
        return list;
    }

	public static Paths createPaths(Object... paths) {
        Paths list = new Paths();
		for (Object o : paths) {
			if (o instanceof String) {
				list.add(new Path((String) o));
			} else if (o instanceof Path) {
				list.add((Path) o);
			} else if (o instanceof Identifiable) {
				list.add(new Path(((Identifiable)o).getName()));
			}
		}
		return list;
	}

	public static List<String> paths(Context context) throws ContextException {
		return context.getPaths();
	}

	public static void remove(ServiceContext entModel, String... paths) {
		for (String path : paths)
			entModel.getData().remove(path);
	}

    public static List<Evaluation>  mdlDeps(ContextDomain model) {
        return ((ServiceContext)model).getDomainStrategy().getModelDependers();
    }

    public static Map<String, List<ExecDependency>> domDeps(ContextDomain model) {
        return ((ServiceContext)model).getDomainStrategy().getDependentDomains();
    }

    public static Map<String, List<ExecDependency>> deps(ContextDomain model) {
         return ((ServiceContext)model).getDomainStrategy().getDependentPaths();
    }

    public static Dependency dependsOn(Dependency dependee,  Evaluation... dependers) throws ContextException, RemoteException {
		List<ExecDependency> functional = new ArrayList<>();
		List<ExecDependency> domain = new ArrayList<>();
        List<ExecDependency> vals = new ArrayList<>();
		List<ExecDependency> modelDeps = new ArrayList<>();

		for (Evaluation ed : dependers) {
			if (ed instanceof ExecDependency && ((ExecDependency) ed).getType() == Type.FUNCTION) {
				functional.add((ExecDependency)ed);
			} else if (ed instanceof ExecDependency && ((ExecDependency) ed).getType() == Type.DOMAIN) {
				domain.add((ExecDependency)ed);
			} else if (ed instanceof ExecDependency && ((ExecDependency) ed).getType() == Type.VAL) {
                vals.add((ExecDependency)ed);
            } else if (ed instanceof ExecDependency && ((ExecDependency) ed).getType() == Type.MODEL) {
				modelDeps.add((ExecDependency)ed);
			}
		}

		ExecDependency[] edArray;
		if (vals.size() > 0) {
			edArray = new ExecDependency[vals.size()];
			return domainDependency(dependee, vals.toArray(edArray));
		} else if (domain.size() > 0) {
			edArray = new ExecDependency[domain.size()];
			return domainDependency(dependee, domain.toArray(edArray));
		} else if (functional.size() > 0) {
			edArray = new ExecDependency[functional.size()];
			return funcDependency(dependee, functional.toArray(edArray));
		} else if (modelDeps.size() > 0) {
			edArray = new ExecDependency[modelDeps.size()];
			return funcDependency(dependee, modelDeps.toArray(edArray));
		} else {
			return funcDependency(dependee, dependers);
		}
	}

    public static Dependency domainDependency(Dependency dependee,  Evaluation... dependers) throws ContextException, RemoteException {
        String path ;
        List<Dependency> dl = new ArrayList<>();
        // find dependency lists
        for (int i = 0; i < dependers.length; i++) {
            if (dependers[i] instanceof ExecDependency && ((ExecDependency) dependers[i]).getDependees() != null) {
                ExecDependency mde = (ExecDependency) dependers[i];
                ExecDependency de ;
                for (Path p : mde.getDependees()) {
                    if (mde.getType() == Type.FIDELITY) {
                        de = dep(p.getName(), (Fidelity) mde.annotation(), (List<Path>)mde.getImpl());
                    } else if (mde.getType() == Type.CONDITION) {
                        de = dep(p.getName(), (Conditional) mde.annotation(), (List<Path>)mde.getImpl());
                    } else {
                        de = dep(p.getName(), (List<Path>)mde.getImpl());
                    }
                    dl.add(de);
                }
                dependers[i] = null;
            }
        }

        for (Evaluation d : dependers) {
            if (d != null) {
                path = ((Identifiable)d).getName();
                if (path != null && path.equals("self")) {
                    d.setName(((ContextDomain) dependee).getName());
                }

                if (d instanceof ExecDependency && ((ExecDependency) d).getType().equals(Type.CONDITION)) {
                    ((ExecDependency) d).getCondition().setConditionalContext((Context) dependee);
                }
                if (!dependee.getDependers().contains(d)) {
                    dependee.getDependers().add(d);
                }
            }
        }

        if (dependers.length > 0 && dependers[0] instanceof ExecDependency) {
            Map<String, List<ExecDependency>> dm = ((ModelStrategy)((Contextion) dependee).getDomainStrategy()).getDependentDomains();
            for (Evaluation e : dependers) {
                if (e != null) {
                    path = ((Identifiable) e).getName();
                    if (dm.get(path) != null) {
                        if (!dm.get(path).contains(e)) {
                            ((List) dm.get(path)).add(e);
                        }
                    } else {
                        List<ExecDependency> del = new ArrayList();
                        del.add((ExecDependency) e);
                        dm.put(path, del);
                    }
                }
            }
        }
        // second pass for dependency lists
        if (dl.size() > 0) {
            Evaluation[] deps = new Evaluation[dl.size()];
            dependsOn(dependee, dl.toArray(deps));
        }

        return dependee;
    }

	public static Dependency funcDependency(Dependency dependee,  Evaluation... dependers) throws ContextException, RemoteException {
		String path;
		List<Dependency> dl = new ArrayList<>();
		// find dependency lists
		for (int i = 0; i < dependers.length; i++) {
			if (dependers[i] instanceof ExecDependency && ((ExecDependency) dependers[i]).getDependees() != null) {
				ExecDependency mde = (ExecDependency) dependers[i];
				ExecDependency de;
				for (Path p : mde.getDependees()) {
					if (mde.getType() == Type.FIDELITY) {
						de = dep(p.getName(), (Fidelity) mde.annotation(), (List<Path>)mde.getImpl());
					} else if (mde.getType() == Type.CONDITION) {
						de = dep(p.getName(), (Conditional) mde.annotation(), (List<Path>)mde.getImpl());
					} else {
						de = dep(p.getName(), (List<Path>)mde.getImpl());
					}
					dl.add(de);
				}
				dependers[i] = null;
			}
		}

		for (Evaluation d : dependers) {
			if (d != null) {
				path = ((Identifiable)d).getName();
				if (path != null && path.equals("self")) {
					d.setName(((ContextDomain) dependee).getName());
				}

				if (d instanceof ExecDependency && ((ExecDependency) d).getType().equals(Type.CONDITION)) {
					((ExecDependency) d).getCondition().setConditionalContext((Context) dependee);
				}

				if (!dependee.getDependers().contains(d)) {
				    if (((Functionality)d).getType()== Type.MODEL) {
                        ((EntryModel)dependee).getModelDependers().add(d);
                    } else {
                        dependee.getDependers().add(d);
                    }
				}
			}
		}

		if (dependee instanceof ContextDomain && dependers.length > 0 && dependers[0] instanceof ExecDependency) {
			Map<String, List<ExecDependency>> dm = ((ModelStrategy)((ContextDomain) dependee).getDomainStrategy()).getDependentPaths();
			for (Evaluation e : dependers) {
				if (e != null && ((Functionality)e).getType() != Type.MODEL) {
					path = ((Identifiable)e).getName();
					if (dm.get(path) != null) {
						if (!dm.get(path).contains(e)) {
							((List) dm.get(path)).add(e);
						}
					} else {
						List<ExecDependency> del = new ArrayList();
						del.add((ExecDependency) e);
						dm.put(path, del);
					}
				}
			}
		}
		// second pass for dependency lists
		if (dl.size() > 0) {
			Evaluation[] deps = new Evaluation[dl.size()];
            dependsOn(dependee, dl.toArray(deps));
		}

		return dependee;
	}

	public static Dependency dependsOn(Dependency dependee, Context scope, Evaluation... dependers)
		throws ContextException, RemoteException {
		if (dependee instanceof Scopable) {
			Context context;
			context = ((Mogram) dependee).getScope();
			if (context == null)
				((Mogram) dependee).setScope(scope);
			else
				context.append(scope);
		}
		return dependsOn(dependee, dependers);
	}

	public static Loop loop(int to) {
		Loop loop = new Loop(to);
		return loop;
	}

	public static Loop loop(int from, int to) {
		Loop loop = new Loop(from, to);
		return loop;
	}

	public static Loop loop(String template, int to) {
		Loop loop = new Loop(template, 1, to);
		return loop;
	}

	public static Loop loop(List<String> templates, int to) {
		Loop loop = new Loop(templates, to);
		return loop;
	}

	public static Loop loop(String template, int from, int to) {
		Loop loop = new Loop(template, from, to);
		return loop;
	}

	public static List<String> names(Loop loop, String prefix) {
		return loop.getNames(prefix);
	}

	public static String[] names(String name, int size, int from) {
		List<String> out = new ArrayList<String>();
		for (int i = from - 1; i < from + size - 1; i++) {
			out.add(name + (i + 1));
		}
		String[] names = new String[size];
		out.toArray(names);
		return names;
	}

	public static String getUnknown() {
		return "unknown" + count++;
	}

	private static String getUnknown(String name) {
		return name + count++;
	}

	public static class Header<T> extends ArrayList<T> {
		private static final long serialVersionUID = 1L;

		public Header() {
			super();
		}

		public Header(int initialCapacity) {
			super(initialCapacity);
		}
	}

	public static class Tokens extends ArrayList<Object> implements Arg {
		private static final long serialVersionUID = 1L;

		private String type;

		public Tokens() {
			super();
		}

		public Tokens(int initialCapacity) {
			super(initialCapacity);
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@Override
		public String getName() {
			return type;
		}

		public Object execute(Arg... args) {
			return this;
		}
	}

	/**
	 * Returns an instance by class method initialization with a service
	 * context.
	 *
	 * @param signature
	 * @return object created
	 * @throws SignatureException
	 */
	public static Object instance(LocalSignature signature, Context context)
			throws SignatureException {
		return signature.build(context);
	}

	public static Object instance(LocalSignature signature)
		throws SignatureException {
		return signature.build();
	}

	public static Contextion instance(String domainName, Signature signature)
		throws SignatureException {
		if (signature instanceof LocalSignature) {
			Object obj = ((LocalSignature) signature).build();
			if (obj instanceof Contextion) {
				((Contextion) obj).setName(domainName);
				return (Contextion) obj;
			}
		}
		throw new SignatureException("not Contextion build signature");
	}

	public static <T> T build(ServiceMogram mogram) throws SignatureException {
		return mogram.getInstance();
	}

	/**
	 * Returns an instance by constructor method initialization or by
	 * instance/class method initialization.
	 *
	 * @param signature
	 * @return object created
	 * @throws SignatureException
	 */
	public static Object instance(Signature signature)
			throws SignatureException {
		if (signature instanceof NetletSignature) {
			String source = ((NetletSignature) signature).getServiceSource();
			if (source != null) {
				try {
					ServiceScripter se = new ServiceScripter(System.out, null, Sorcer.getWebsterUrl(), true);
					se.readFile(new File(source));
					return se.interpret();
				} catch (Throwable e) {
					throw new SignatureException(e);
				}
			} else {
				throw new SignatureException("missing netlet filename");
			}
		} else if ((signature.getSelector() == null
				&& ((LocalSignature) signature).getInitSelector() == null)
				|| (signature.getSelector() != null && signature.getSelector().equals("new"))
				|| (((LocalSignature) signature).getInitSelector() != null
				&& ((LocalSignature) signature).getInitSelector().equals("new")))
			return ((LocalSignature) signature).newInstance();
		else
			return ((LocalSignature) signature).initInstance();
	}

	public static Object instance(Signature signature, Fidelity fidefity) throws SignatureException {
		Object out = instance(signature);
		if (out instanceof Contextion) {
			try {
				((Contextion)out).selectFidelity(fidefity);
			} catch (ConfigurationException | RemoteException e) {
				throw new SignatureException(e);
			}
		}
		return out;
	}

	public static Object created(Signature signature) throws SignatureException {
		return instance(signature);
	}

	public static ContextDomain model(Signature signature) throws SignatureException {
		Object model = instance(signature);
		if (!(model instanceof ContextDomain)) {
			throw new SignatureException("Signature does not specify te ContextDomain: " + signature);
		}
		try {
			if (model instanceof Model) {
				((Model)model).setBuilder(signature);
			}
		} catch (ServiceException | RemoteException e) {
			throw new SignatureException(e);
		}
		return (ContextDomain) model;
	}

	public static Mogram instance(Mogram mogram, Arg... args) throws SignatureException, ServiceException {
		Signature builder = null;
		builder = ((ServiceMogram)mogram).getBuilder(args);
		if (builder == null) {
			throw new SignatureException("No signature builder for: " + mogram.getName());
		}
		ServiceMogram mog = (ServiceMogram) sorcer.co.operator.instance(builder);
		mog.setBuilder(builder);
		Tag name = (Arg.getName(args));
		if (name != null)
			mog.setName(name.getName());
		return mog;
	}

	public static Tag tag(Object object) {
		return new Tag(object.toString());
	}

	public static Index ind(int index) {
		return new Index(index);
	}

    public static Index rowInd(int index) {
        return new Index(index, Index.Direction.row);
    }

    public static Index columnInd(int index) {
        return new Index(index, Index.Direction.column);
    }

    public static Index ind(int index, Index.Direction type) {
        return new Index(index, type);
    }

	public static String dmnName(Object identifiable) throws RemoteException {
		String dname = null;
		if (identifiable instanceof Domain) {
			dname = ((Domain) identifiable).getDomainName();
		}
		if (dname == null) {
			dname = ((Identifiable) identifiable).getName();
		}
		return dname;
	}

	public static String name(Object identifiable) {
		if (identifiable instanceof Identifiable) {
			return ((Identifiable) identifiable).getName();
		} else if (identifiable instanceof Arg) {
			return ((Arg) identifiable).getName();
		} else {
			return identifiable.toString();
		}
	}

	public static Slot setName(Slot slot, String name) {
		slot.setKey(name);
		return slot;
	}

	public static Context setName(Context context, String name) {
		context.setName(name);
		return context;
	}
	public static Signature setName(Signature signature, String name) {
		((ServiceSignature)signature).setName(name);
		return signature;
	}

	public static Evaluator setName(Evaluator evaluator, String name) {
		evaluator.setName(name);
		return evaluator;
	}

	public static URL url(String urlName) throws MalformedURLException {
		return new URL(urlName);
	}

}