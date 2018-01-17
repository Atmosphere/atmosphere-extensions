/*
 * Copyright 2018 Async-IO.org
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
package org.atmosphere.sockjs;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AsyncIOInterceptorAdapter;
import org.atmosphere.cpr.AsyncIOWriter;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereInterceptorWriter;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.HeaderConfig;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.util.IOUtils;
import org.atmosphere.util.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.HTMLFILE;
import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.JSONP;
import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.LONG_POLLING;
import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.POLLING;
import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.SSE;
import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.STREAMING;
import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.UNDEFINED;
import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.WEBSOCKET;

public class SockJsAtmosphereInterceptor extends AtmosphereInterceptorAdapter {

    public final static String SOCKS_JS_ORIGIN = SockJsAtmosphereInterceptor.class.getName() + ".origin";

    private static final Logger logger = LoggerFactory.getLogger(SockJsAtmosphereInterceptor.class);
    private boolean supportWebSocket = true;
    private final AtomicReference<String> baseURL = new AtomicReference<String>("");
    private AtmosphereFramework framework;
    private final Map<String, SockjsSession> sessions = Collections.synchronizedMap(new WeakHashMap<String, SockjsSession>());

    public final static AtmosphereHandler ECHO_ATMOSPHEREHANDLER = new AbstractReflectorAtmosphereHandler() {
        @Override
        public void onRequest(AtmosphereResource resource) throws IOException {
            String body = IOUtils.readEntirely(resource).toString();
            if (!body.isEmpty()) {
                resource.getBroadcaster().broadcast(body);
            }
        }
    };

    @Override
    public void configure(final AtmosphereConfig config) {
        framework = config.framework();
        supportWebSocket = config.framework().getAsyncSupport().supportWebSocket();
        config.properties().put(HeaderConfig.JSONP_CALLBACK_NAME, "c");
        for (AtmosphereInterceptor i : framework.interceptors()) {
            if (HeartbeatInterceptor.class.isAssignableFrom(i.getClass())) {
                HeartbeatInterceptor.class.cast(i).paddingText("h".getBytes()).heartbeatFrequencyInSeconds(25);
            }
        }

        if (config.handlers().size() == 0) {
            framework.addAtmosphereHandler("/*", ECHO_ATMOSPHEREHANDLER);
        }
    }

    @Override
    public Action inspect(final AtmosphereResource r) {
        final AtmosphereRequest request = r.getRequest();

        if (request.getAttribute("sockjs.skipInterceptor") != null) {
            return Action.CONTINUE;
        }

        boolean info = request.getRequestURI().endsWith("/info");
        if (info) {
            return info(r);
        }

        boolean iframe = request.getRequestURI().endsWith("/iframe.html");
        if (iframe) {
            return iframe(r);
        }

        if (!baseURL.get().isEmpty() && request.getRequestURI().startsWith(baseURL.get())) {
            super.inspect(r);

            // See https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-36
            // The URL received from the client should be similar to the following:
            //   <base_url>/<server>/<session>/<protocol>
            // The auxiliar params method will handle this URL processing
            String[] params = params(request.getRequestURI().substring(baseURL.get().length()), 2);
            final String sessionId = params[0];
            String transport = params[1];

            SockjsSession s = sessions.get(sessionId);
            configureTransport(AtmosphereResourceImpl.class.cast(r), transport, s != null);
            boolean longPolling = org.atmosphere.util.Utils.resumableTransport(r.transport());

            if (s == null) {
                sessions.put(sessionId, new SockjsSession());
                if (!longPolling) {
                    installWriter(r, sessionId);
                }
                return Action.CONTINUE;
            } else if (longPolling) {
                installWriter(r, sessionId);
                return Action.CONTINUE;
            }

            return injectMessage(r);
        }
        return Action.CONTINUE;
    }

    private Action iframe(AtmosphereResource r) {
        final AtmosphereResponse response = r.getResponse();
        response.setContentType("text/html");
        String origin = framework.getAtmosphereConfig().getInitParameter(SOCKS_JS_ORIGIN);
        if (origin == null) {
            origin = "http://localhost:8080/lib/sockjs.js";
        }
        try {
            response.write(IFrameUtils.generateIFrame(origin)).flushBuffer();
        } catch (IOException e) {
            logger.error("", e);
        }
        return Action.CANCELLED;
    }

    protected Action info(AtmosphereResource r) {
        final AtmosphereResponse response = r.getResponse();
        final AtmosphereRequest request = r.getRequest();

        response.headers().put("Content-Type", "application/json; charset=UTF-8");
        ObjectNode json = new ObjectNode(JsonNodeFactory.instance);
        json.put("websocket", supportWebSocket);
        json.putArray("origins").add("*:*");
        json.put("entropy", new Random().nextInt());
        r.write(JsonCodec.encode(json));

        if (baseURL.get().isEmpty()) {
            baseURL.set(request.getRequestURI().substring(0, request.getRequestURI().indexOf("/info")));
        }

        return Action.CANCELLED;
    }

    private static String[] params(String urlFragment, int pos) {
        if (pos <= 0) {
            throw new IllegalArgumentException("pos must be greater then zero");
        }
      
        String s = urlFragment;
        int currPos = 0;
        if (s.startsWith("/")) {
            currPos++;
        }
      
        List<String> params = new ArrayList<String>();
        int slashPos = -1;
        String token = null;
        while (params.size() < pos) {
            slashPos = s.indexOf('/', currPos);
            if (slashPos > 0) {
                token = s.substring(currPos, slashPos);
                currPos = slashPos + 1;
                params.add(token);
            } else {
                break;
            }
        }
        
        if (params.size() < pos) {
           throw new IllegalArgumentException(
               String.format("the number of tokens in the url fragment passed as argument '%s' is less than the number required %d", urlFragment, pos));
        }
      
        return params.toArray(new String[params.size()]);
    }    

    private void configureTransport(AtmosphereResourceImpl r, String s, boolean hasSession) {
        if ("websocket".equals(s)) {
            r.transport(WEBSOCKET).addEventListener(new WebSocketTransport());
        } else if ("xhr".equals(s) || "xdr".equals(s)) {
            r.transport(LONG_POLLING);

            if (!hasSession) {
                r.addEventListener(new LongPollingTransport());
            }
        } else if ("xhr_streaming".equals(s)) {
            r.transport(STREAMING).addEventListener(new StreamingTransport());
        } else if ("jsonp".equals(s)) {
            r.transport(JSONP);

            if (!hasSession) {
                r.addEventListener(new JSONPTransport());
            }
        } else if ("eventsource".equals(s)) {
            r.transport(SSE).addEventListener(new SSETransport());
        } else if (s.indexOf("_send") != -1) {
            r.transport(POLLING);
        } else if ("htmlfile".equals(s)) {
            r.transport(HTMLFILE).addEventListener(new HtmlFileTransport());
        } else if (s.indexOf("_send") != -1) {
        } else {
            r.transport(UNDEFINED).addEventListener(new StreamingTransport());
        }
    }

    private void installWriter(final AtmosphereResource r, final String sessionId) {
        final AtmosphereResource.TRANSPORT transport = r.transport();
        final AtmosphereResponse response = r.getResponse();

        AsyncIOWriter writer = response.getAsyncIOWriter();
        if (AtmosphereInterceptorWriter.class.isAssignableFrom(writer.getClass())) {
            AtmosphereInterceptorWriter.class.cast(writer).interceptor(new AsyncIOInterceptorAdapter() {
                @Override
                public byte[] transformPayload(AtmosphereResponse response, byte[] responseDraft, byte[] data) throws IOException {
                    String charEncoding = response.getCharacterEncoding() == null ? "UTF-8" : response.getCharacterEncoding();
                    String s = new String(responseDraft, charEncoding);

                    // Ugly.
                    if (s.equalsIgnoreCase("h") || s.equals("c") || (s.equals("o\n") && r.transport().equals(AtmosphereResource.TRANSPORT.WEBSOCKET))) {
                        return s.getBytes();
                    }

                    if (!s.isEmpty()) {
                        try {
                            if (transport.equals(JSONP)) {
                                return ("a" + s).getBytes(charEncoding);
                            } else if (transport.equals(HTMLFILE)) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("<script>\np(")
                                        .append("\"")
                                        .append(StringEscapeUtils.escapeJavaScript("a[\"" + StringEscapeUtils.escapeJavaScript(s) + "\"]\n"))
                                        .append("\")</script>\n");
                                return (sb.toString()).getBytes(charEncoding);
                            } else {
                                return ("a[\"" + StringEscapeUtils.escapeJavaScript(s) + "\"]\n").getBytes(charEncoding);

                            }
                        } catch (Exception e) {
                            logger.error("", e);
                            return "".getBytes();
                        }
                    }

                    return s.getBytes();
                }
            });
        } else {
            logger.warn("Unable to apply {}. Your AsyncIOWriter must implement {}", getClass().getName(), AtmosphereInterceptorWriter.class.getName());
        }

        r.addEventListener(new AtmosphereResourceEventListenerAdapter() {
            @Override
            public void onDisconnect(AtmosphereResourceEvent event) {
                sessions.remove(sessionId);
            }
        });
    }

    private Action injectMessage(AtmosphereResource r) {
        final AtmosphereResponse response = r.getResponse();
        final AtmosphereRequest request = r.getRequest();

        try {
            String body = IOUtils.readEntirely(r).toString();
            if (!body.isEmpty() && body.startsWith("d=")) {
                body = URLDecoder.decode(body, "UTF-8");
                body = body.substring(2);
                response.setStatus(200);
                response.write("ok", true).flushBuffer();
                reInject(request, response, body);
            } else {
                String[] messages = parseMessageString(body);
                for (String m : messages) {
                    if (m == null) continue;
                    reInject(request, response, m);
                }
                response.setStatus(204);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return Action.CANCELLED;
    }

    private void reInject(AtmosphereRequest request, AtmosphereResponse response, String body) throws IOException, ServletException {
        request.setAttribute("sockjs.skipInterceptor", Boolean.TRUE);
        framework.doCometSupport(request.body(body), response);
        request.setAttribute("sockjs.skipInterceptor", null);
    }

    private String[] parseMessageString(String msgs) {
        try {
            String[] parts;
            if (msgs.startsWith("[")) {
                //JSON array
                parts = (String[]) JsonCodec.decodeValue(msgs, String[].class);
            } else {
                //JSON string
                String str = (String) JsonCodec.decodeValue(msgs, String.class);
                parts = new String[]{str};
            }
            return parts;
        } catch (Exception e) {
            return null;
        }
    }
}
