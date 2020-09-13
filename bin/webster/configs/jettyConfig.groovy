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
 * Defines the roots for Jetty as well as whether to spawn webster into it's own JVM.
 *
 * Note: The values ${sorcerHome}, ${rioVersion} and ${m2Repo} are set as bindings by
 * the jetty.groovy script
 */

/*
 * A configuration file for Jetty
 */
jetty {
    roots = ["${sorcerHome}" as String,
             "${sorcerHome}/lib/sorcer/lib-dl" as String,
             "${sorcerHome}/lib/sorcer/lib" as String,
             "${sorcerHome}/lib/river" as String,
             "${sorcerHome}/rio-${rioVersion}/lib" as String,
             "${sorcerHome}/rio-${rioVersion}/lib-dl" as String,
             "${sorcerHome}/lib/common" as String,
             "${sorcerHome}/lib/blitz" as String,
             "${sorcerHome}/lib" as String,
             "${m2Repo}" as String]
    putDirectory = "${sorcerHome}/data" as String
    secure = true
    spawn = true
}
