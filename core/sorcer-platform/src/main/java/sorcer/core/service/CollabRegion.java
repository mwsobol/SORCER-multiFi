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

import sorcer.core.context.ModelStrategy;
import sorcer.service.*;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author Mike Sobolewski
 */
public class CollabRegion extends Collaboration implements Region {

    private static int count = 0;

    private Supervision supervisor;

    public CollabRegion(String name) {
        if (name == null) {
            this.key = getClass().getSimpleName() + "-" + count++;
        } else {
            this.key = name;
        }
        serviceStrategy = new ModelStrategy(this);
    }

    public CollabRegion(String name, Node[] nodes) {
        this(name);
        for (Node node : nodes) {
            this.children.put(node.getName(), node);
        }
    }

    public CollabRegion(String name, List<Node> nodes) {
        this(name);
        for (Node node : nodes) {
            this.children.put(node.getName(), node);
        }
    }

    public Supervision getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Supervision supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public Context evaluate(Context context, Arg... args) throws ServiceException {
        if (children.size() == 1) {
            try {
                output =  children.values().iterator().next().evaluate(context, args);
            } catch (MogramException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            output = super.evaluate(context, args);
        }
        return output;
    }

}
