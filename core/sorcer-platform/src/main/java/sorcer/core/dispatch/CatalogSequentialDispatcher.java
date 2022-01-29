/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.core.dispatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.model.ent.EntryModel;
import sorcer.core.exertion.Mograms;
import sorcer.service.Exerter;
import sorcer.service.*;
import sorcer.service.modeling.Model;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import static sorcer.service.Exec.*;

public class CatalogSequentialDispatcher extends CatalogExertDispatcher {
    private final Logger logger = LoggerFactory.getLogger(CatalogSequentialDispatcher.class);

	@SuppressWarnings("rawtypes")
    public CatalogSequentialDispatcher(Routine job,
                                       Set<Context> sharedContext,
                                       boolean isSpawned,
                                       Exerter provider,
                                       ProvisionManager provisionManager) {
        super(job, sharedContext, isSpawned, provider, provisionManager);
    }

    protected void doExec(Arg... args) throws MogramException, SignatureException {
        String pn;
        if (inputXrts == null) {
            xrt.setStatus(FAILED);
            state = FAILED;
            try {
                pn = provider.getProviderName();
                if (pn == null)
                    pn = provider.getClass().getName();
                RoutineException fe = new RoutineException(pn + " received invalid job: "
                        + xrt.getName(), xrt);

                xrt.reportException(fe);
                dispatchers.remove(xrt.getId());
                throw fe;
            } catch (RemoteException e) {
                logger.warn("Error during local prc", e);
            }
        }

        xrt.startExecTime();
        Context previous = null;
        for (Contextion mogram: inputXrts) {
            if (xrt.isBlock()) {
                try {
                    if (mogram.getScope() != null)
                        mogram.getScope().append(xrt.getContext());
                    else {
                        mogram.setScope(xrt.getContext());
                    }
                } catch (Exception ce) {
                    throw new MogramException(ce);
                }
            }

            try {
                if (mogram instanceof Routine) {
                    Subroutine se = (Subroutine) mogram;
                    // support for continuous pre and post execution of task
                    // signatures
                    if (previous != null && se.isTask() && ((Task) se).isContinous())
                        se.setContext(previous);
                    dispatchExertion(se, args);
                    previous = se.getContext();
                    if (mogram instanceof Block) {
                        xrt.getDataContext().append(previous);
                    }
                } else if (mogram instanceof EntryModel) {
                    ((EntryModel)mogram).updateEntries(xrt.getContext());
                    xrt.getDataContext().append((Context) ((Model) mogram).getResponse());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RoutineException(e);
            }

        }

        if (masterXrt != null) {
            masterXrt = (Subroutine) execExertion(masterXrt, args); // executeMasterExertion();
            if (masterXrt.getStatus() <= FAILED) {
                state = FAILED;
                xrt.setStatus(FAILED);
            } else {
                state = DONE;
                xrt.setStatus(DONE);
            }
        } else
            state = DONE;
        dispatchers.remove(xrt.getId());
        xrt.stopExecTime();
        xrt.setStatus(DONE);
    }

    protected void dispatchExertion(Subroutine se, Arg... args) throws SignatureException, RoutineException {
        se = (Subroutine) execExertion(se, args);
        if (se.getStatus() <= FAILED) {
            xrt.setStatus(FAILED);
            state = FAILED;
            try {
                String pn = provider.getProviderName();
                if (pn == null) {
                    pn = provider.getClass().getName();
                }
                RoutineException fe = new RoutineException(pn
                        + " received failed task: " + se.getName(), se);
                xrt.reportException(fe);
                dispatchers.remove(xrt.getId());
                throw fe;
            } catch (RemoteException e) {
                logger.warn("Exception during local prc");
            }
        } else if (se.getStatus() == SUSPENDED
                || xrt.getControlContext().isReview(se)) {
            xrt.setStatus(SUSPENDED);
            RoutineException ex = new RoutineException(
                    "exertion suspended", se);
            se.reportException(ex);
            dispatchers.remove(xrt.getId());
            throw ex;
        }
    }

    protected List<Contextion> getInputExertions() throws ContextException {
        return Mograms.getInputExertions(((Job) xrt));
    }

}
