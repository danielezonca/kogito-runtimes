/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.codegen.api;

import org.kie.kogito.codegen.api.io.CollectedResource;

import java.util.Objects;

/**
 * Class to represent a Kogito codegen exceptions
 */
public final class KogitoCodegenException extends RuntimeException {

    protected final CollectedResource source;
    protected final Exception exception;
    protected final String errorMessage;
    protected final Severity severity;
    protected final int line;
    protected final int column;

    private KogitoCodegenException(Builder builder, String errorMessage) {
        this.source = builder.source;
        this.exception = builder.exception;
        this.errorMessage = errorMessage;
        this.severity = builder.severity;
        this.line = builder.line;
        this.column = builder.column;
    }

    public static Builder builder(CollectedResource source) {
        return new Builder(source);
    }

    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }

    public static final class Builder {

        protected final CollectedResource source;
        protected Exception exception;
        protected String errorMessage;
        protected Severity severity = Severity.ERROR;
        protected int line = -1;
        protected int column = -1;

        private Builder(CollectedResource source) {
            Objects.requireNonNull(source, "source cannot be null");
            this.source = source;
        }

        public Builder withException(Exception exception) {
            this.exception = exception;
            return this;
        }

        public Builder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder withSeverity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder withLine(int line) {
            this.line = line;
            return this;
        }

        public Builder withColumn(int column) {
            this.column = column;
            return this;
        }

        public KogitoCodegenException build() {
            String message = errorMessage != null ? errorMessage : ( exception != null ? exception.getMessage() : null);
            return new KogitoCodegenException(this, message);
        }
    }
}
