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
package org.atmosphere.gwt20.client.managed;

import org.atmosphere.gwt20.client.AtmosphereMessage;

import java.io.InputStream;

/**
 * This class is used during serialization by the {@link RPCSerializer}. This class is normally used with
 * the {@link org.atmosphere.gwt20.managed.AtmosphereMessageInterceptor} to allow GWT and non GWT
 * application to interact using a {@link org.atmosphere.cpr.Broadcaster}.
 * <p/>
 * The {@link #asString()}  or {@link #asByte()}  are used to serialize the object so Atmosphere's component
 * can react on the deserialize GWT object as a normal request's body, without the GWT protocol.
 * <p/>
 *
 * @author Jeanfrancois Arcand
 */
public class RPCEvent implements AtmosphereMessage<String> {

    private String message;

    public RPCEvent() {
    }

    public RPCEvent(String message) {
        this.message = message;
    }

    @Override
    public TYPE type() {
        return TYPE.STRING;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String asString() {
        return message;
    }

    @Override
    public byte[] asByte() {
        return message.getBytes();
    }

}
