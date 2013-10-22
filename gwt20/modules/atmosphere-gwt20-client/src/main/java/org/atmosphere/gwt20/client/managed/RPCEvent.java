/*
 * Copyright 2013 Jeanfrancois Arcand
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

import java.io.Serializable;

/**
 * This class is used during serialization by the {@link RPCSerializer}. This class is normally used with
 * the {@link org.atmosphere.gwt20.server.managed.RPCEventDeserializerInterceptor} to allow GWT and non GWT
 * application to interact using a {@link org.atmosphere.cpr.Broadcaster}.
 *
 * This class can also be used with {@link org.atmosphere.cpr.AtmosphereHandler}
 *
 * @author Jeanfrancois Arcand
 */
public class RPCEvent implements Serializable {

    private String data;

    public RPCEvent() {
    }

    public RPCEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
