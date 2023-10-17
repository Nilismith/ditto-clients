/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.client.live.commands.modify;

import java.util.Optional;

import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.live.commands.base.AbstractLiveCommand;
import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.commands.modify.ThingModifyCommand;

/**
 * Abstract base implementation for LiveCommands wrapping {@link ThingModifyCommand}s.
 *
 * @param <T> the type of the LiveCommand; currently needed as return type for
 * {@link #setDittoHeaders(org.eclipse.ditto.base.model.headers.DittoHeaders)}.
 * @param <B> the type of the LiveCommandAnswerBuilder to be returned for {@link #answer()}.
 *
 * @since 2.0.0
 */
abstract class AbstractModifyLiveCommand<T extends LiveCommand<T, B> & ThingModifyCommand<T>, B extends LiveCommandAnswerBuilder>
        extends AbstractLiveCommand<T, B> implements ThingModifyCommand<T> {

    private final ThingModifyCommand<?> thingModifyCommand;

    /**
     * Constructs a new {@code AbstractModifyLiveCommand} object.
     *
     * @param thingModifyCommand the command to be wrapped by the returned object.
     * @throws NullPointerException if {@code command} is {@code null}.
     */
    protected AbstractModifyLiveCommand(final ThingModifyCommand<?> thingModifyCommand) {
        super(thingModifyCommand);
        this.thingModifyCommand = thingModifyCommand;
    }

    @Override
    public ThingId getEntityId() {
        return thingModifyCommand.getEntityId();
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        return thingModifyCommand.getEntity(schemaVersion);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setEntity(final JsonValue entity) {
        return (T) this;
    }
}
