/*
 * Copyright 2018 the original author or authors.
 * Copyright 2018 SorcerSoft.org.
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

import net.jini.core.transaction.Transaction;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.signature.ObjectSignature;
import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.Model;

import java.rmi.RemoteException;
import java.util.List;

/**
 * A free mogram is a mogram that has a name only to be bound at runtime.
 *
 * @see Mogram
 *
 * @author Mike Sobolewski
 */
public class FreeMogram extends ServiceMogram {

    private Mogram mogram;

    public FreeMogram(String name) {
        this.key = name;
    }

    public FreeMogram(String name, Functionality.Type type) {
        this.key = name;
        this.type = type;
    }

    public void bind(Signature signature) throws SignatureException, MogramException {
        if (signature != null) {
            mogram = (Mogram) ((ObjectSignature)signature).build();
            builder = signature;
        }
        mogram.setBuilder(builder);
        if (mogram instanceof Model) {
            type = Functionality.Type.MODEL;
        } else if (mogram instanceof Routine) {
            type = Functionality.Type.ROUTINE;
        }
    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public Mogram clearScope() throws MogramException {
        return null;
    }

    @Override
    public void reportException(Throwable t) {

    }

    @Override
    public List<ThrowableTrace> getExceptions() throws RemoteException {
        return null;
    }

    public Mogram getMogram() {
        return mogram;
    }

    public void setMogram(Mogram mogram) {
        this.mogram = mogram;
    }


    @Override
    public Context evaluate(Context context, Arg... args) throws EvaluationException, RemoteException {
        return null;
    }

    public boolean isModel() {
        return type == Functionality.Type.MODEL;
    }

    public boolean isRoutine() {
        return type == Functionality.Type.ROUTINE;
    }

    @Override
    public <T extends Mogram> T exert(T mogram, Transaction txn, Arg... entries) throws MogramException, RemoteException {
        return null;
    }
}
