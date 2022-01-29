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

package sorcer.service;


import java.util.List;

public interface Fi<T> extends Identifiable, Service, Arg {

    int e = 0;
    int s = 1;
    int r = 2;
    int c = 3;
    int m = 4;
    int v =  5;
    int vFi = 6;
    int ev =  7;
    int gt =  8;
    int st =  9;
    int gr = 10;
    int dVar = 11;

    enum Type implements Arg {
        IN, OUT, VAL, ENTRY, SIG, REF, MORPH, VAR, VAR_FI, PROC, SRV, ANE, EVALUATOR, GETTER, SETTER, GRADIENT, DERIVATIVE,
        MULTI, REQUEST, RESPONSE, UPDATE, ADD, REPLACE, DELETE, SELECT, META, NAME, SOA, IF, IF_SOA, SYS, CONTEXT,
        MODEL, PATH, IN_PATH, OUT_PATH, MDA, INTENT, DESIGN, DEV, SUP, ANALYZER, EXPLORER, CONFIG, DISCIPLINE, DISPATCHER,
        CONTEXTION, PROJECTION, FROM_TO, CXT_PRJ, MTF, MMTF;

		static public String name(int fiType) {
			for (Type s : Type.values()) {
				if (fiType == s.ordinal())
					return "" + s;
			}
			return null;
		}

        static public Type type(int fiType) {
            for (Type s : Type.values()) {
                if (fiType == s.ordinal())
                    return s;
            }
            return null;
        }
        public String getName() {
			return toString();
		}

        public Object execute(Arg... args) {
            return this;
        }
	}

    T getSelect();

    T get(int index);

	int size();

    T selectSelect(String fiName) throws ConfigurationException;

    List<T> getSelects();

    void addSelect(T select);

    String getPath();

    void setPath(String path);

    void setSelect(T select);

	void removeSelect(T select);

    boolean isValid();

    Type getFiType();

    boolean isChanged();

    void setChanged(boolean state);

	void clearFi();

	// inner fidelity otherwise self
    Fidelity getFidelity();

}

