/*
 * Copyright 2012-2013 the original author or authors.
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

import org.activiti.engine.ProcessEngine

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Andres Almiray
 */
class ProcessEngineHolder {
    private static final String DEFAULT = 'default'
    private static final Object[] LOCK = new Object[0]
    private final Map<String, ProcessEngine> engines = [:]

    private static final ProcessEngineHolder INSTANCE

    static {
        INSTANCE = new ProcessEngineHolder()
    }

    static ProcessEngineHolder getInstance() {
        INSTANCE
    }

    String[] getProcessEngineNames() {
        List<String> engineNames = new ArrayList().addAll(engines.keySet())
        engineNames.toArray(new String[engineNames.size()])
    }

    ProcessEngine getProcessEngine(String engineName = DEFAULT) {
        if (isBlank(engineName)) engineName = DEFAULT
        retrieveProcessEngine(engineName)
    }

    void setProcessEngine(String engineName = DEFAULT, ProcessEngine engine) {
        if (isBlank(engineName)) engineName = DEFAULT
        storeProcessEngine(engineName, engine)
    }

    boolean isProcessEngineConnected(String engineName) {
        if (isBlank(engineName)) engineName = DEFAULT
        retrieveProcessEngine(engineName) != null
    }

    void disconnectProcessEngine(String engineName) {
        if (isBlank(engineName)) engineName = DEFAULT
        storeProcessEngine(engineName, null)
    }

    ProcessEngine fetchProcessEngine(String engineName) {
        if (isBlank(engineName)) engineName = DEFAULT
        ProcessEngine engine = retrieveProcessEngine(engineName)
        if (engine == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = ActivitiConnector.instance.createConfig(app)
            engine = ActivitiConnector.instance.connect(app, config, engineName)
        }

        if (engine == null) {
            throw new IllegalArgumentException("No such activiti engine configuration for name $engineName")
        }
        engine
    }

    private ProcessEngine retrieveProcessEngine(String engineName) {
        synchronized(LOCK) {
            engines[engineName]
        }
    }

    private void storeProcessEngine(String engineName, ProcessEngine engine) {
        synchronized(LOCK) {
            engines[engineName] = engine
        }
    }
}
