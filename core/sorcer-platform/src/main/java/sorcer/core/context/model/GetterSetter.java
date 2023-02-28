/*
 * Copyright 2020 the original author or authors.
 * Copyright 2020 SorcerSoft.org.
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

package sorcer.core.context.model;

import sorcer.service.*;
import sorcer.service.modeling.Getter;

import java.rmi.RemoteException;

public class GetterSetter implements Getter {

    public Getter getter;

    public boolean isDual;

    public GetterSetter(Getter getter) {
        this.getter = getter;
    }

    @Override
    public Object getValue(Arg... args) throws ContextException {
        return getter.getValue(args);
    }

    @Override
    public Fi getMultiFi() {
        return getter.getMultiFi();
    }

    @Override
    public Morpher getMorpher() {
        return getter.getMorpher();
    }

    @Override
    public Object execute(Arg... args) throws ServiceException, RemoteException {
        return getter.execute(args);
    }
}
