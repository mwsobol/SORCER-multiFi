/*
 * Copyright to the original author or authors.
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

/*
 * This configuration is used to start a ProvisionMonitor, including an embedded Webster and
 * a Lookup Service
 */

import com.sun.jini.start.ServiceDescriptor
import org.rioproject.config.Component
import org.rioproject.resolver.maven2.Repository
import org.rioproject.security.SecureEnv
import org.rioproject.start.util.ServiceDescriptorUtil
import org.rioproject.util.RioHome


@Component('org.rioproject.start')
class StartMonitorConfig {
    final boolean useHttps

    StartMonitorConfig() {
        useHttps = SecureEnv.setup()
    }

    static String[] getMonitorConfigArgs(String rioHome) {
        def configArgs = [rioHome+'/config/common.groovy', rioHome+'/config/monitor.groovy']
        return configArgs as String[]
    }

    static String[] getLookupConfigArgs(String rioHome) {
        def configArgs = [rioHome+'/config/common.groovy', rioHome+'/config/reggie.groovy']
        return configArgs as String[]
    }

    static void sorcerData() {
        String user = System.properties['user.name']
        String tmpDir = System.getenv("TMPDIR")==null?System.properties['java.io.tmpdir']:System.getenv("TMPDIR")
        File sorcerDataDir = new File("${tmpDir}/sorcer-${user}/data")
        sorcerDataDir.mkdirs()
    }

    ServiceDescriptor[] getServiceDescriptors() {
        ServiceDescriptorUtil.checkForLoopback()
        String m2Repo = Repository.getLocalRepository().absolutePath
        String rioHome = RioHome.get()

        def websterRoots = [rioHome+'/deploy', ';', m2Repo]
        String sorcerHome = System.getProperty("sorcer.home", System.getenv("SORCER_HOME"))
        if(sorcerHome!=null) {
            int ndx = rioHome.lastIndexOf("-")
            String version = rioHome.substring(ndx+1)
            ["${sorcerHome}/lib/sorcer/lib-dl",
             "${sorcerHome}/lib/sorcer/lib",
             "${sorcerHome}/lib/river",
             "${sorcerHome}/lib/common",
             "${sorcerHome}/lib/blitz",
             "${sorcerHome}/lib",
             "${sorcerHome}/rio-${version}/lib-dl",
             "${sorcerHome}/rio-${version}/lib",
             "${sorcerHome}"].each { root ->
                websterRoots << ';'
                websterRoots << root
            }
            sorcerData()
        }
        String policyFile = rioHome+'/policy/policy.all'

        def serviceDescriptors = [
            //ServiceDescriptorUtil.getWebster(policyFile, '0', websterRoots as String[]),
            ServiceDescriptorUtil.getJetty('0', websterRoots as String[], useHttps),
            ServiceDescriptorUtil.getLookup(policyFile, getLookupConfigArgs(rioHome)),
            ServiceDescriptorUtil.getMonitor(policyFile, getMonitorConfigArgs(rioHome))
        ]

        return (ServiceDescriptor[])serviceDescriptors
    }

}
