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

package sorcer.core.service;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import sorcer.service.*;

import java.rmi.RemoteException;

/**
 * @author Mike Sobolewski
 */
abstract public class Realm extends MultiFiSlot implements Transdiscipline {

    protected Uuid id = UuidFactory.generate();

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Context superevaluate(Context context, Arg... args) throws ServiceException, RemoteException {
        return evaluate(context, args);
    }

}
