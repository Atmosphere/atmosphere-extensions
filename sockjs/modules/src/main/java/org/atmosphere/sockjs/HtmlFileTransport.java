/*
 * Copyright 2008-2020 Async-IO.org
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

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HtmlFileTransport extends TransportBasedListener {

    private static final byte[] padding;

    static {
        StringBuilder whitespace = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            whitespace.append(" ");
        }
        whitespace.append("\r\n");
        String paddingText = whitespace.toString();
        padding = paddingText.getBytes();
    }

    private static final Logger logger = LoggerFactory.getLogger(SSETransport.class);
    @Override
    public void onPreSuspend(AtmosphereResourceEvent event) {
        AtmosphereResponse response = event.getResource().getResponse();
        AtmosphereRequest request = event.getResource().getRequest();

        String callback = request.getParameter("c");
        // TODO: Configurable
        response.setContentType("text/html; charset=UTF-8");
        response.write(IFrameUtils.generateHtmlFile(callback)).write(padding);
        try {
            response.write(("<script>\n" + "p(\"o\")" +  "\n</script>\n").getBytes()).flushBuffer();
        } catch (IOException e) {
            logger.trace("", e);
        }
    }


}
