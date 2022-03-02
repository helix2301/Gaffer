/*
 * Copyright 2016-2021 Crown Copyright
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

package uk.gov.gchq.gaffer.federatedstore.operation.handler.impl;

import uk.gov.gchq.gaffer.data.elementdefinition.exception.SchemaException;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.gaffer.store.operation.handler.OutputOperationHandler;
import uk.gov.gchq.gaffer.store.schema.Schema;

import static uk.gov.gchq.gaffer.federatedstore.util.FederatedStoreUtil.getDeprecatedGraphIds;

/**
 * A {@code FederatedGetSchemaHandler} handles the {@link uk.gov.gchq.gaffer.store.operation.GetSchema}
 * operation by merging federated schemas.
 */
public class FederatedGetSchemaHandler implements OperationHandler<GetSchema, Schema> {
    @Override
    public Schema doOperation(final GetSchema operation, final Context context, final Store store) throws OperationException {
        if (null == operation) {
            throw new OperationException("Operation cannot be null");
        }

        try {
            final Iterable<Schema> schemas = (Iterable<Schema>) store.execute(
                    new FederatedOperation.Builder()
                            .op(operation)
                            .graphIds(getDeprecatedGraphIds(operation)) // deprecate this line.
                            .build(), context);

            try {
                Schema.Builder builder = new Schema.Builder();
                schemas.forEach(builder::merge);
                return builder.build();
            } catch (final Exception e) {
                throw new SchemaException("Unable to merge the schemas for all of your federated graphs. You can limit which graphs to query for using the FederatedOperation.graphIds.", e);
            }
        } catch (final Exception e) {
            throw new OperationException("Error getting Schemas for FederatedStore - " + e.getMessage(), e);
        }

    }
}