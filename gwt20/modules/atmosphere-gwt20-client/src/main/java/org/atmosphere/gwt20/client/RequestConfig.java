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
/*
 * Copyright 2009 Richard Zschech.
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

import com.google.gwt.http.client.RequestBuilder.Method;

public interface RequestConfig {

    // Don't get crazy: For the 2.1 release we keep backward compatibility. FIX ME for 2.1
    void setFlags(AtmosphereRequestConfig.Flags... flags);

    // Don't get crazy: For the 2.1 release we keep backward compatibility. FIX ME for 2.1
    void clearFlags(AtmosphereRequestConfig.Flags... flags);

    void setOutboundSerializer(ClientSerializer serializer);

    void setHeader(String name, String value);

    void setMaxReconnectOnClose(int maxReconnectOnClose);

    void setContentType(String contentType);

    void setUrl(String url);

    void setConnectTimeout(int connectTimeout);

    void setReconnectInterval(int reconnectInterval);

    void setTimeout(int timeout);

    void setLogLevel(String logLevel);

    void setMaxRequest(int maxRequest);

    void setMaxStreamingLength(int maxStreamingLength);

    void setMethod(Method method);

    void setFallbackMethod(Method method);

    // Don't get crazy: For the 2.1 release we keep backward compatibility. FIX ME for 2.1
    void setTransport(AtmosphereRequestConfig.Transport transport);

    // Don't get crazy: For the 2.1 release we keep backward compatibility. FIX ME for 2.1
    void setFallbackTransport(AtmosphereRequestConfig.Transport transport);

    void setOpenHandler(AtmosphereOpenHandler handler);

    void setReopenHandler(AtmosphereReopenHandler handler);

    void setCloseHandler(AtmosphereCloseHandler handler);

    void setClientTimeoutHandler(AtmosphereClientTimeoutHandler handler);

    void setMessageHandler(AtmosphereMessageHandler handler);

    void setLocalMessageHandler(AtmosphereMessageHandler handler);

    void setErrorHandler(AtmosphereErrorHandler handler);

    void setReconnectHandler(AtmosphereReconnectHandler handler);

    void setFailureToReconnectHandler(AtmosphereFailureToReconnectHandler handler);

    void setMessagePublishedHandler(AtmosphereMessagePublishedHandler handler);

    void setTransportFailureHandler(AtmosphereTransportFailureHandler handler);

    ClientSerializer getOutboundSerializer();
}