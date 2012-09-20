/*
 * Copyright 2012 the original author or authors.
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

package griffon.plugins.activiti

import org.activiti.engine.*

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import griffon.util.CallableWithArgs
import static griffon.util.GriffonNameUtils.isBlank

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
class ProcessEngineHolder implements ActivitiProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessEngineHolder)
    private static final Object[] LOCK = new Object[0]
    private final Map<String, ProcessEngine> engines = [:]

    String[] getEngineNames() {
        List<String> engineNames = new ArrayList().addAll(engines.keySet())
        engineNames.toArray(new String[engineNames.size()])
    }

    ProcessEngine getEngine(String engineName = 'default') {
        if(isBlank(engineName)) engineName = 'default'
        retrieveEngine(engineName)
    }

    void setEngine(String engineName = 'default', ProcessEngine engine) {
        if(isBlank(engineName)) engineName = 'default'
        storeEngine(engineName, engine)
    }

    Object withActiviti(String engineName = 'default', Closure closure) {
        ProcessEngine engine = fetchEngine(engineName)
        if(LOG.debugEnabled) LOG.debug("Executing block with engine '$engineName'")
        return closure(engineName, engine)
    }

    public <T> T withActiviti(String engineName = 'default', CallableWithArgs<T> callable) {
        ProcessEngine engine = fetchEngine(engineName)
        if(LOG.debugEnabled) LOG.debug("Executing block with engine '$engineName'")
        callable.args = [engineName, engine] as Object[]
        return callable.call()
    }

    boolean isEngineConnected(String engineName) {
        if(isBlank(engineName)) engineName = 'default'
        retrieveEngine(engineName) != null
    }

    void disconnectEngine(String engineName) {
        if(isBlank(engineName)) engineName = 'default'
        storeEngine(engineName, null)
    }

    private ProcessEngine fetchEngine(String engineName) {
        if(isBlank(engineName)) engineName = 'default'
        ProcessEngine engine = retrieveEngine(engineName)
        if(engine == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = ActivitiConnector.instance.createConfig(app)
            engine = ActivitiConnector.instance.connect(app, config, engineName)
        }

        if(engine == null) {
            throw new IllegalArgumentException("No such activiti engine configuration for name $engineName")
        }
        engine
    }

    private ProcessEngine retrieveEngine(String engineName) {
        synchronized(LOCK) {
            engines[engineName]
        }
    }

    private void storeEngine(String engineName, ProcessEngine engine) {
        synchronized(LOCK) {
            engines[engineName] = engine
        }
    }
}
