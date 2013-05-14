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

package org.atmosphere.gwt.server.impl;


import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import org.atmosphere.gwt.shared.server.SerializationException;

/**
 * @author p.havelaar
 */
public class IFrameResponseWriter extends ManagedStreamResponseWriter {

    public IFrameResponseWriter(GwtAtmosphereResourceImpl resource) {
        super(resource);
    }

    // IE requires padding to start processing the page.
    private static final int PADDING_REQUIRED = 256;

    private static final String HEAD = "<html><body onload='parent.window.d()'><script>";
    private static final String MID = "parent.window.c(";
    private static final String TAIL = ");</script>";

    private static final String PADDING_STRING;

    static {
        // the required padding minus the length of the heading
        int capacity = PADDING_REQUIRED - HEAD.length() - MID.length() - TAIL.length();
        char[] padding = new char[capacity];
        for (int i = 0; i < capacity; i++) {
            padding[i] = ' ';
        }
        PADDING_STRING = new String(padding);
    }

    @Override
    public void initiate() throws IOException {
        getResponse().setContentType("text/html");

        String origin = getRequest().getHeader("Origin");
        if (origin != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Origin: " + origin);
            }
            getResponse().setHeader("Access-Control-Allow-Origin", origin);
        }

        super.initiate();

        writer.append(HEAD);
        String domain = getRequest().getParameter("d");
        if (domain != null) {
            writer.append("document.domain='");
            writer.append(domain);
            writer.append("';");
        }
        writer.append(MID);
        writer.append(Integer.toString(resource.getHeartBeatInterval()));
        writer.append(',').append("'").append(String.valueOf(resource.getConnectionUUID())).append("'");
        writer.append(TAIL);
    }

    @Override
    protected int getPaddingRequired() {
        return PADDING_REQUIRED;
    }

    @Override
    protected CharSequence getPadding(int padding) {
        if (padding > PADDING_STRING.length()) {
            StringBuilder result = new StringBuilder(padding);
            for (int i = 0; i < padding; i++) {
                result.append(' ');
            }
            return result;
        } else {
            return PADDING_STRING.substring(0, padding);
        }
    }

    @Override
    protected void doSendError(int statusCode, String message) throws IOException {
        getResponse().setContentType("text/html");
        writer.append("<html><script>parent.window.e(").append(Integer.toString(statusCode));
        if (message != null) {
            writer.append(",'").append(escapeString(message)).append('\'');
        }
        writer.append(")</script></html>");
    }

    @Override
    protected void doWrite(List<? extends Serializable> messages) throws IOException, SerializationException {
        writer.append("<script>parent.window.m(");
        boolean first = true;
        for (Serializable message : messages) {
            CharSequence string;
            if (message instanceof CharSequence) {
                string = "]" + escapeString((CharSequence) message);
            } else {
                string = escapeObject(serialize(message));
            }
            if (first) {
                first = false;
            } else {
                writer.append(',');
            }
            writer.append('\'').append(string).append('\'');
        }
        writer.append(")</script>");
    }

    @Override
    protected void doHeartbeat() throws IOException {
        writer.append("<script>parent.window.h();</script>");
    }

    @Override
    protected void doTerminate() throws IOException {
        writer.append("<script>parent.window.t();</script>");
    }

    @Override
    protected void doRefresh() throws IOException {
        writer.append("<script>parent.window.r();</script>");
    }

    @Override
    protected boolean isOverRefreshLength(int written) {
        if (length != null) {
            return written > length;
        } else {
            return written > 4 * 1024 * 1024;
        }
    }

    private CharSequence escapeString(CharSequence string) {
        if (!this.shouldEscapeText())
            return string;

        int len = (string != null) ? string.length() : 0;
        int i = 0;
        loop:
        while (i < len) {
            char ch = string.charAt(i);
            switch (ch) {
                case '\'':
                case '\\':
                case '/':
                case '\b':
                case '\f':
                case '\n':
                case '\r':
                    break loop;
            }
            i++;
        }

        if (i == len)
            return string;

        StringBuilder str = new StringBuilder(string.length() * 2);
        str.append(string, 0, i);
        while (i < len) {
            char ch = string.charAt(i);
            switch (ch) {
                case '\'':
                    str.append("\\\'");
                    break;
                case '\\':
                    str.append("\\\\");
                    break;
                case '/':
                    str.append("\\/");
                    break;
                case '\b':
                    str.append("\\b");
                    break;
                case '\f':
                    str.append("\\f");
                    break;
                case '\n':
                    str.append("\\n");
                    break;
                case '\r':
                    str.append("\\r");
                    break;
                case '\t':
                    str.append("\\t");
                    break;
                default:
                    str.append(ch);
            }
            i++;
        }
        return str;
    }

    private CharSequence escapeObject(CharSequence string) {
        if (!this.shouldEscapeText())
            return string;

        int len = (string != null) ? string.length() : 0;
        int i = 0;
        loop:
        while (i < len) {
            char ch = string.charAt(i);
            switch (ch) {
                case '\'':
                case '\\':
                case '/':
                    break loop;
            }
            i++;
        }

        if (i == len)
            return string;

        StringBuilder str = new StringBuilder(string.length() * 2);
        str.append(string, 0, i);
        while (i < len) {
            char ch = string.charAt(i);
            switch (ch) {
                case '\'':
                    str.append("\\\'");
                    break;
                case '\\':
                    str.append("\\\\");
                    break;
                case '/':
                    str.append("\\/");
                    break;
                default:
                    str.append(ch);
            }
            i++;
        }
        return str;
    }
}
