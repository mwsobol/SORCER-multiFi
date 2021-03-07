/*
 * Copyright 2021 the original author or authors.
 * Copyright 2021 SorcerSoft.org.
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

import sorcer.core.Dispatcher;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author Mike Sobolewski
 */
public class DscFidelity implements Identifiable, Serializable, Arg {

    private String name;

    protected String path;

    private Context context;

    private Contextion contextion;

    private Dispatcher dispatcher;

    public DscFidelity(String name) {
        this.name = name;
    }

    @Override
    public Object getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Contextion getContextion() {
        return contextion;
    }

    public void setContextion(Contextion contextion) {
        this.contextion = contextion;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Object execute(Arg... args) throws ServiceException, RemoteException {
        return null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) throws ContextException {

    }
}
