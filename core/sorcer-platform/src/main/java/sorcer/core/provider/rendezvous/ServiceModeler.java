/*
 * Copyright 2015 the original author or authors.
 * Copyright 2015 SorcerSoft.org.
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

package sorcer.core.provider.rendezvous;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.*;
import sorcer.core.context.model.req.RequestModel;
import sorcer.core.provider.Modeler;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;
import sorcer.service.modeling.Model;

import java.rmi.RemoteException;

/**
 * ServiceModler - The SORCER rendezvous service provider that manages
 * coordination for executing service models using its service signatures
 * to create a dynamic federation of collaborating providers.
 *
 * @author Mike Sobolewski
 */
public class ServiceModeler extends SorcerExerterBean implements Modeler {
    private Logger logger = LoggerFactory.getLogger(ServiceModeler.class);

    public ServiceModeler() throws RemoteException {
        // do nothing
    }

    public Mogram localExert(Mogram mogram, Transaction txn, Arg... args)
            throws TransactionException, ContextException, RemoteException {
        setServiceID(mogram);
        Mogram result = null;
        Model model = null;
        Signature builder = null;
        ContextSelection contextSelector = null;
        Context dataContext = null;
        ModelTask taskModel = null;
        try {
            if (mogram instanceof RequestModel) {
                  return ((RequestModel)mogram).exert(args);
            } else if (mogram instanceof ModelerTask) {
                taskModel = ((ModelerTask) mogram).getModelTask();
                if (taskModel != null) {
                    model = taskModel.getModel();
                    builder = taskModel.getBuilder();
                    contextSelector = taskModel.getContextFilter();
                }
            } else if (mogram instanceof ModelerNetTask) {
                taskModel = ((ModelerNetTask) mogram).getModelTask();
                if (taskModel != null) {
                    model = taskModel.getModel();
                    builder = taskModel.getBuilder();
                    contextSelector = taskModel.getContextFilter();
                }
            }

            dataContext =  ((ServiceMogram)mogram).getDataContext();

            if (dataContext instanceof Model) {
                result = ((ServiceContext)dataContext).getResponse(args);
                if (builder != null) {
                    model = (Model) ((LocalSignature)builder).newInstance();
                }
                if (model != null) {
                    ((ServiceMogram)model).substitute((Arg) result);
                    result = model.exert(args);
                }
                if (contextSelector != null) {
                    result = (Mogram) contextSelector.doSelect(result);
                }
            }
            ((Subroutine)mogram).setContext((Context)result);
            logger.trace("<==== Result: " + result);

        } catch (Exception e) {
            throw new ContextException(e);
        }
        return mogram;
    }

}
