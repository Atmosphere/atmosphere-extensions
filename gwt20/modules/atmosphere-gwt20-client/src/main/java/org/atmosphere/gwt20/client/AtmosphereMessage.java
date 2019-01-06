/*
 * Copyright 2008-2019 Async-IO.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.gwt20.client;

import java.io.Serializable;

/**
 * Base class for GWT deserialized messages that will be broadcasted as String through the Atmosphere Framework own component like {@link ManagedService}
 *
 * @author Jeanfrancois Arcand
 */
public interface AtmosphereMessage<T> extends Serializable {

    public enum TYPE {STRING, BYTES}

    /**
     * Return the {@link TYPE}
     *
     * @return
     */
    public TYPE type();

    /**
     * Return the message of type T
     *
     * @return
     */
    public T getMessage();

    /**
     * Set the message of type T
     *
     * @param message
     */
    public void setMessage(T message);

    /**
     * Serialize this object in the form of an {@link String}.
     *
     * @return
     */
    public String asString();

    /**
     * Serialize this object in the form of an byte[] array.
     *
     * @return
     */
    public byte[] asByte();

}
