/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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
package sorcer.core.monitor;

import net.jini.id.Uuid;
import sorcer.service.Exec;
import sorcer.service.MonitorException;
import sorcer.service.Monitorable;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Map;

public interface MonitoringManagement extends Monitorable, MonitorManagement,
		MonitorSessionManagement, MonitorUIManagement, Remote {
}
