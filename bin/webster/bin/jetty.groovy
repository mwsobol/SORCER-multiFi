#!/usr/bin/env groovy
/*
 * Copyright to the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Starts Jetty, serving up Sorcer distribution content
 */

/*
 * Get the local Maven repository
 */
def getLocalRepository() {
    File repoDir
    String localRepository = null
    File defaultM2Home = new File(System.getProperty("user.home")+File.separator+".m2")
    File settingsFile = new File(defaultM2Home, "settings.xml")
    if (settingsFile.exists()) {
        def settings = new XmlSlurper().parse(settingsFile)
        localRepository = settings.localRepository
    }
    if (localRepository==null || localRepository.length()==0) {
        repoDir = new File(defaultM2Home, "repository")
    } else {
        repoDir = new File(localRepository)
    }
    repoDir.path
}

String scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent
String sorcerHome = new File(scriptDir).parentFile.parentFile.parentFile.path
println "SORCER_HOME: ${sorcerHome}"

/* Load versions.properties */
def versions = new Properties()
new File(sorcerHome, "configs/versions.properties").withReader { reader ->
    versions.load(reader)
}

/* Load sorcer.env */
def sorcerEnv = new Properties()
new File(sorcerHome, "configs/sorcer.env").withReader { reader ->
    sorcerEnv.load(reader)
}

def configSlurper = new ConfigSlurper()
configSlurper.setBinding("sorcerHome" : sorcerHome,
        "rioVersion" : versions['rio.version'],
        "m2Repo" : getLocalRepository())

File jettyConfig = new File(scriptDir, "../configs/jettyConfig.groovy")
def config = configSlurper.parse(jettyConfig.toURI().toURL())
StringBuilder java = new StringBuilder()
java.append(System.getProperty('java.home')).append("/bin/java")
boolean spawn = config.jetty.spawn
def args = []
if (System.getProperty("os.name").startsWith("Windows")) {
    args << "cmd.exe"
    args << "/C"
}

args << java.toString()

["java.protocol.handler.pkgs": "net.jini.url|sorcer.util.url|org.rioproject.url",
 "java.security.policy" : "${sorcerHome}/policy/policy.all",
 "java.rmi.server.useCodebaseOnly" : "false",
 "logback.configurationFile" : "${sorcerHome}/configs/sorcer-logging.groovy",
 "sorcerHome" : sorcerHome,
 "rioVersion" : versions['rio.version'],
 "m2Repo" : getLocalRepository(),
 "jetty.port" : "${sorcerEnv['provider.webster.port']}"
].each { key, value ->
    args << "-D${key}=${value}"
}
args << "-Xmx450M"
args << "-cp"

StringBuilder cp = new StringBuilder()
String rioHome = "${sorcerHome}/rio-${versions['rio.version']}"
def jars = ["${rioHome}/lib/logging/slf4j-api-${versions['slf4j.version']}.jar",
            "${rioHome}/lib/logging/logback-core-${versions['logback.version']}.jar",
            "${rioHome}/lib/logging/logback-classic-${versions['logback.version']}.jar",
            "${rioHome}/lib/rio-platform-${versions['rio.version']}.jar",
            "${rioHome}/lib/groovy-all-${versions['groovy.version']}.jar",
            "${rioHome}/lib/webster-${versions['rio.version']}.jar"]
for (String jar : jars) {
    if (cp.length() > 0) {
        cp.append(File.pathSeparator)
    }
    cp.append(jar)
}

args << cp.toString()
args << "org.rioproject.tools.jetty.Jetty"
args << jettyConfig.path

//println args
ProcessBuilder pb = new ProcessBuilder(args as String[])
Map<String, String> env = pb.environment()
env.put("SORCER_HOME", "${sorcerHome}")
env.put("RIO_HOME", "${sorcerHome}/rio-${versions['rio.version']}")

Process process = pb.start()
if (!spawn) {
    process.consumeProcessOutput(System.out, System.err)
    process.waitFor()
} else {
    println "putDirectory: ${config.jetty.putDirectory}"
    int rootNum = 0
    config.jetty.roots.each { root ->
        println "Root ${rootNum++} = $root"
    }
    process.in.close()
    process.out.close()
    process.err.close()
}
