/*
* Copyright 2015 SORCERsoft.org.
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

import java.rmi.RemoteException;

/**
 * Created by Mike Sobolewski on 11/9/15.
 */
@FunctionalInterface
public interface Morpher extends Controller, Directive {

    public void morph(FidelityManagement manager, Fi<Service> mFi, Object value) throws RemoteException, ServiceException, ConfigurationException;

    enum Dir implements Arg {
        IN, OUT, INOUT;

        @Override
        public String getName() {
            return toString();
        }

        static public Dir fromString(String direction) {
            if (direction == null) {
                return null;
            } else if (direction.equals(""+IN)) {
                return IN;
            } else if (direction.equals(""+OUT)) {
                return OUT;
            } else if (direction.equals(""+INOUT)) {
                return INOUT;
            } else {
                return null;
            }
        }

        public Object execute(Arg... args) {
            return this;
        }
    }

}
