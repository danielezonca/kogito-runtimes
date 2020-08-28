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

package org.kie.kogito.explainability;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.kogito.Application;
import org.kie.kogito.StaticApplication;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.dmn.DMNKogito;
import org.kie.kogito.dmn.DmnDecisionModel;
import org.kie.kogito.explainability.model.ModelIdentifier;
import org.kie.kogito.explainability.model.PredictInput;
import org.kie.kogito.explainability.model.PredictOutput;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.kie.kogito.explainability.model.ModelIdentifier.RESOURCE_ID_SEPARATOR;

public class ExplainabilityServiceTest {

    public static final String MODEL_RESOURCE = "/Traffic Violation.dmn";
    public static final String MODEL_NAMESPACE = "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF";
    public static final String MODEL_NAME = "Traffic Violation";

    private static final String TEST_EXECUTION_ID = "test";
    private static final DMNRuntime genericDMNRuntime = DMNKogito.createGenericDMNRuntime(new InputStreamReader(
            ExplainabilityServiceTest.class.getResourceAsStream(MODEL_RESOURCE)
    ));
    private static final DmnDecisionModel decisionModel = new DmnDecisionModel(genericDMNRuntime, MODEL_NAMESPACE, MODEL_NAME, () -> TEST_EXECUTION_ID);
    private static final ExplainabilityService service = ExplainabilityService.INSTANCE;
    private static final DecisionModels decisionModels = (namespace, name) -> {
        if (MODEL_NAMESPACE.equals(namespace) && MODEL_NAME.equals(name)) {
            return decisionModel;
        }
        throw new RuntimeException("Model not found.");
    };
    private static final Application application = new StaticApplication(null, null, null, decisionModels, null);

    @Test
    public void testPerturbedExecution() {
        Map<String, Object> perturbedRequest = createRequest();
        PredictInput predictInput = new PredictInput(
                "pi1",
                new ModelIdentifier("dmn", String.format("%s%s%s", MODEL_NAMESPACE, RESOURCE_ID_SEPARATOR, MODEL_NAME)),
                perturbedRequest);

        List<PredictOutput> predictOutputs = service.processRequest(application, singletonList(predictInput));

        Assertions.assertEquals(1, predictOutputs.size());
        checkResult(predictInput, predictOutputs.get(0));
    }

    @Test
    public void testPerturbedExecutionBrokenInputs() {
        PredictInput predictInput = new PredictInput(
                "pi1",
                new ModelIdentifier("dmn", String.format("%s%s%s", MODEL_NAMESPACE, RESOURCE_ID_SEPARATOR, MODEL_NAME)),
                createRequest());

        PredictInput unsupportedResourceType = new PredictInput(
                "pi2",
                new ModelIdentifier("unsupported", String.format("%s%s%s", MODEL_NAMESPACE, RESOURCE_ID_SEPARATOR, MODEL_NAME)),
                emptyMap());

        PredictInput unknownModel = new PredictInput(
                "pi3",
                new ModelIdentifier("dmn", "unknown:model"),
                emptyMap());

        List<PredictOutput> predictOutputs = service.processRequest(application, asList(predictInput, unsupportedResourceType, unknownModel));

        Assertions.assertEquals(1, predictOutputs.size());
        checkResult(predictInput, predictOutputs.get(0));
    }

    private Map<String, Object> createRequest() {
        Map<String, Object> driver = new HashMap<>();
        driver.put("Age", 25);
        driver.put("Points", 10);

        Map<String, Object> violation = new HashMap<>();
        violation.put("Type", "speed");
        violation.put("Actual Speed", 105);
        violation.put("Speed Limit", 100);

        Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("Driver", driver);
        contextVariables.put("Violation", violation);

        return contextVariables;
    }

    private void checkResult(PredictInput predictInput, PredictOutput predictOutput) {
        Assertions.assertNotNull(predictOutput);
        Assertions.assertNotNull(predictOutput.getResult());

        Assertions.assertEquals(predictInput.getId(), predictOutput.getId());

        Map<String, Object> perturbedResult = predictOutput.getResult();
        Assertions.assertTrue(perturbedResult.containsKey("Should the driver be suspended?"));
        Assertions.assertEquals("No", perturbedResult.get("Should the driver be suspended?"));
        Assertions.assertTrue(perturbedResult.containsKey("Fine"));
        Assertions.assertNull(perturbedResult.get("Fine"));
    }
}
