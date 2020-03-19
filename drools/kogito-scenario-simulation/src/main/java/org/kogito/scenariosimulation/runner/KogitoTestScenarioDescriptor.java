/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kogito.scenariosimulation.runner;

import org.drools.scenariosimulation.backend.runner.model.ScenarioRunnerDTO;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

public class KogitoTestScenarioDescriptor extends AbstractTestDescriptor {

    private final ScenarioRunnerDTO scenarioRunnerDTO;

    public KogitoTestScenarioDescriptor(UniqueId uniqueId, ScenarioRunnerDTO scenarioRunnerDTO) {
        super(uniqueId.append("testscenario", scenarioRunnerDTO.getFileName()),
                scenarioRunnerDTO.getFileName(),
                ClassSource.from(KogitoTestScenarioEngine.class));
        this.scenarioRunnerDTO = scenarioRunnerDTO;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    public ScenarioRunnerDTO getScenarioRunnerDTO() {
        return scenarioRunnerDTO;
    }
}
