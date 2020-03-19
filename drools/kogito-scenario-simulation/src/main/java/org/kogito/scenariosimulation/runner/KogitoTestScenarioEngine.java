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

import org.drools.scenariosimulation.api.model.*;
import org.drools.scenariosimulation.backend.expression.ExpressionEvaluatorFactory;
import org.drools.scenariosimulation.backend.runner.IndexedScenarioException;
import org.drools.scenariosimulation.backend.runner.ScenarioException;
import org.drools.scenariosimulation.backend.runner.model.ScenarioRunnerDTO;
import org.drools.scenariosimulation.backend.runner.model.ScenarioRunnerData;
import org.drools.scenariosimulation.backend.util.ScenarioSimulationXMLPersistence;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.drools.scenariosimulation.api.utils.ScenarioSimulationSharedUtils.FILE_EXTENSION;
import static org.drools.scenariosimulation.backend.util.ResourceHelper.getResourcesByExtension;

public class KogitoTestScenarioEngine implements TestEngine {

    private static final ScenarioSimulationXMLPersistence xmlReader = ScenarioSimulationXMLPersistence.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(KogitoTestScenarioEngine.class);

    @Override
    public String getId() {
        return "kogito-test-scenario";
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        EngineDescriptor parentDescriptor = new EngineDescriptor(uniqueId, "Kogito Test Scenario");
        Stream<String> resourcesByExtension = getResourcesByExtension(FILE_EXTENSION);

        System.out.println("LALALALAL");

        resourcesByExtension.map(this::parseFile)
                .forEach(scesim -> parentDescriptor.addChild(new KogitoTestScenarioDescriptor(uniqueId, scesim)));

        return parentDescriptor;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        System.out.println("LALALALAL");
        KogitoDMNScenarioRunnerHelper scenarioRunnerHelper = new KogitoDMNScenarioRunnerHelper();
        for (TestDescriptor testDescriptor : executionRequest.getRootTestDescriptor().getChildren()) {

            executionRequest.getEngineExecutionListener().executionStarted(testDescriptor);

            ScenarioRunnerDTO scenarioRunnerDTO = ((KogitoTestScenarioDescriptor) testDescriptor).getScenarioRunnerDTO();
            ExpressionEvaluatorFactory expressionEvaluatorFactory = ExpressionEvaluatorFactory.create(
                    null,
                    scenarioRunnerDTO.getSettings().getType());
            ScesimModelDescriptor simulationModelDescriptor = scenarioRunnerDTO.getSimulationModelDescriptor();
            Settings settings = scenarioRunnerDTO.getSettings();
            Background background = scenarioRunnerDTO.getBackground();

            for (ScenarioWithIndex scenarioWithIndex : scenarioRunnerDTO.getScenarioWithIndices()) {
                ScenarioRunnerData scenarioRunnerData = new ScenarioRunnerData();
                int index = scenarioWithIndex.getIndex();



                try {
                    scenarioRunnerHelper.run(null,
                            simulationModelDescriptor,
                            scenarioWithIndex,
                            expressionEvaluatorFactory,
                            null,
                            scenarioRunnerData,
                            settings,
                            background);
                } catch (ScenarioException e) {
                    IndexedScenarioException indexedScenarioException = new IndexedScenarioException(index, e);
                    indexedScenarioException.setFileName(scenarioRunnerDTO.getFileName());
//                    runNotifier.fireTestFailure(new Failure(descriptionForScenario, indexedScenarioException));
                    logger.error(e.getMessage(), e);
                } catch (Throwable e) {
                    IndexedScenarioException indexedScenarioException = new IndexedScenarioException(index, "Unexpected test error in scenario '" +
                            scenarioWithIndex.getScesimData().getDescription() + "'", e);
                    indexedScenarioException.setFileName(scenarioRunnerDTO.getFileName());
                    logger.error(e.getMessage(), e);
//                    runNotifier.fireTestFailure(new Failure(descriptionForScenario, indexedScenarioException));
                }

            }
            executionRequest.getEngineExecutionListener().executionFinished(testDescriptor, TestExecutionResult.successful());
        }

    }

    protected ScenarioRunnerDTO parseFile(String path) {
        try (final Scanner scanner = new Scanner(new File(path))) {
            String rawFile = scanner.useDelimiter("\\Z").next();
            ScenarioSimulationModel scenarioSimulationModel = xmlReader.unmarshal(rawFile);
            return new ScenarioRunnerDTO(scenarioSimulationModel, path);
        } catch (FileNotFoundException e) {
            throw new ScenarioException("File not found, this should not happen: " + path, e);
        } catch (Exception e) {
            throw new ScenarioException("Issue on parsing file: " + path, e);
        }
    }

}