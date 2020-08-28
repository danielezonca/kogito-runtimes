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

import org.kie.kogito.Application;
import org.kie.kogito.explainability.model.PredictInput;
import org.kie.kogito.explainability.model.PredictOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class ExplainabilityService {

    public static final ExplainabilityService INSTANCE = new ExplainabilityService(singletonList(new DecisionExplainabilityResourceExecutor()));
    private static final Logger logger = LoggerFactory.getLogger(ExplainabilityService.class);

    private final Collection<ExplainabilityResourceExecutor> executors;

    public ExplainabilityService(Collection<ExplainabilityResourceExecutor> executors) {
        this.executors = executors;
    }

    public List<PredictOutput> processRequest(Application application, List<PredictInput> predictInputs) {
        return predictInputs.stream()
                .map(predictInput -> tryExecute(application, predictInput))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    protected Optional<PredictOutput> tryExecute(Application application, PredictInput predictInput) {
        try {
            Optional<PredictOutput> optionalPredictOutput = executors.stream()
                    .filter(r -> r.acceptRequest(predictInput))
                    .map(r -> r.processRequest(application, predictInput))
                    .findFirst();
            if(!optionalPredictOutput.isPresent()) {
                logger.error("Unsupported resourceType {}", predictInput.getModelIdentifier().getResourceType());
            }
            return optionalPredictOutput;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }
}
