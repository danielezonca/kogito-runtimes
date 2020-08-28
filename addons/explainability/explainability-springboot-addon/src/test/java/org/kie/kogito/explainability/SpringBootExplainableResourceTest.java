/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.explainability;

import org.junit.jupiter.api.Test;
import org.kie.kogito.explainability.model.ModelIdentifier;
import org.kie.kogito.explainability.model.PredictInput;
import org.kie.kogito.explainability.model.PredictOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.explainability.Constants.MODEL_NAME;
import static org.kie.kogito.explainability.Constants.MODEL_NAMESPACE;

class SpringBootExplainableResourceTest {

    SpringBootExplainableResource resource = new SpringBootExplainableResource(new ApplicationMock());

    @Test
    @SuppressWarnings("unchecked")
    void explainServiceTest() {
        List<PredictInput> inputs = singletonList(createInput("pi1", 40));

        List<PredictOutput> outputs = (List<PredictOutput>) resource.predict(inputs).getBody();

        assertNotNull(outputs);
        assertEquals(1, outputs.size());

        PredictOutput output = outputs.get(0);

        assertNotNull(output.getResult());
        assertNotNull(output.getModelIdentifier());
        assertEquals(inputs.get(0).getId(), output.getId());
        Map<String, Object> result = output.getResult();

        assertTrue(result.containsKey("Should the driver be suspended?"));
        assertEquals("Yes", result.get("Should the driver be suspended?"));
        assertTrue(result.containsKey("Fine"));
        Map<String, Object> expectedFine = new HashMap<>();
        expectedFine.put("Points", BigDecimal.valueOf(7));
        expectedFine.put("Amount", BigDecimal.valueOf(1000));
        assertEquals(expectedFine.get("Points"), ((Map<String, Object>)result.get("Fine")).get("Points"));
        assertEquals(expectedFine.get("Amount"), ((Map<String, Object>)result.get("Fine")).get("Amount"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void explainServiceTestMultipleInputs() {
        List<PredictInput> inputs = asList(createInput("pi1", 40), createInput("pi2", 120));

        List<PredictOutput> outputs = (List<PredictOutput>) resource.predict(inputs).getBody();

        assertNotNull(outputs);
        assertEquals(2, outputs.size());

        PredictOutput output = outputs.get(1);

        assertNotNull(output);
        assertNotNull(output.getResult());
        assertNotNull(output.getModelIdentifier());

        IntStream.range(0, inputs.size()).forEach(i -> assertEquals(inputs.get(i).getId(), outputs.get(i).getId()));

        Map<String, Object> result = output.getResult();

        assertTrue(result.containsKey("Should the driver be suspended?"));
        assertEquals("No", result.get("Should the driver be suspended?"));
        assertNull(result.get("Fine"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void explainServiceTestNoInputs() {
        List<PredictOutput> outputs = (List<PredictOutput>) resource.predict(emptyList()).getBody();

        assertNotNull(outputs);
        assertEquals(0, outputs.size());
    }

    @Test
    void explainServiceFail() {
        PredictInput input = createInput("pi1", 10);
        input.getModelIdentifier().setResourceId("unknown:model");
        ResponseEntity<Object> responseEntity = resource.predict(singletonList(input));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof List);
        @SuppressWarnings("unchecked")
        List<PredictOutput> outputs = (List<PredictOutput>) responseEntity.getBody();
        assertTrue(outputs.isEmpty());
    }

    private PredictInput createInput(String id, int speedLimit) {
        String resourceId = String.format("%s:%s", MODEL_NAMESPACE, MODEL_NAME);

        Map<String, Object> driver = new HashMap<>();
        driver.put("Age", 25);
        driver.put("Points", 100);

        Map<String, Object> violation = new HashMap<>();
        violation.put("Type", "speed");
        violation.put("Actual Speed", 120);
        violation.put("Speed Limit", speedLimit);

        Map<String, Object> payload = new HashMap<>();
        payload.put("Driver", driver);
        payload.put("Violation", violation);

        ModelIdentifier modelIdentifier = new ModelIdentifier("dmn", resourceId);
        return new PredictInput(id, modelIdentifier, payload);
    }
}
