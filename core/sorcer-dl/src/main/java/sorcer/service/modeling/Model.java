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

package sorcer.service.modeling;

import sorcer.service.*;

import java.rmi.RemoteException;

/**
 * An top-level common interface for all service Models in SORCER.
 *
 * @author Mike Sobolewski
 */
public interface Model extends ContextDomain, mog, dmn, cxtn {

    // All-at-Once (AAO),
    // Simultaneous Analysis and Design (SAND),
    // Individual Discipline Feasible (IDF),
    // Multidiscipline Feasible (MDF)
    // Analysis, Mado, Explore, Supervise
    public enum Pattern {
        AAO, SAND, IDF, MDF, ANAL, OPTI, MDA, SNAP, MDA_EXPL, MADO, EXPL, SUPV, COLLAB, MADOCOLLAB
    }

    /**
     * Returns a model result.
     *
     * @return a model result
     * @throws ContextException
     * @throws RemoteException
     */
    public Object getResult() throws ContextException, RemoteException;

    /**
     * Returns a model  response context.
     *
     * @return a response context
     * @throws ContextException
     * @throws RemoteException
     */
    public Context getResponse(Context context, Arg... args) throws ContextException, RemoteException;

    /**
     * Sets a buider of this model to be used for replication it when needed.
     *
     * @param signature
     */
    public void setBuilder(Signature signature)  throws ServiceException, RemoteException;

}