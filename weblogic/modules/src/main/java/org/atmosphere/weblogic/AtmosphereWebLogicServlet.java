 /*
 * Copyright 2008-2021 Async-IO.org
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
 package org.atmosphere.weblogic;

 import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AsynchronousProcessor;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereRequestImpl;
import org.atmosphere.cpr.AtmosphereResponseImpl;
import org.atmosphere.cpr.AtmosphereServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weblogic.servlet.http.AbstractAsyncServlet;
import weblogic.servlet.http.RequestResponseKey;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResponse;

/**
 * WebLogic Comet implementation.
 */
public class AtmosphereWebLogicServlet extends AbstractAsyncServlet {

    protected static final Logger logger = LoggerFactory.getLogger(AtmosphereServlet.class);
    protected AtmosphereFramework framework;

    /**
     * Create an Atmosphere Servlet.
     */
    public AtmosphereWebLogicServlet() {
        this(false);
    }

    /**
     * Create an Atmosphere Servlet.
     *
     * @param isFilter true if this instance is used as an {@link org.atmosphere.cpr.AtmosphereFilter}
     */
    public AtmosphereWebLogicServlet(boolean isFilter) {
        this(isFilter, true);
    }

    /**
     * Create an Atmosphere Servlet.
     *
     * @param isFilter true if this instance is used as an {@link org.atmosphere.cpr.AtmosphereFilter}
     */
    public AtmosphereWebLogicServlet(boolean isFilter, boolean autoDetectHandlers) {
        framework = new AtmosphereFramework(isFilter, autoDetectHandlers);
    }

    @Override
    public void destroy() {
        framework.destroy();
    }

    public void init(final ServletConfig sc) throws ServletException {
        super.init(sc);
        framework.setAsyncSupport(new WebLogicCometSupport(framework.getAtmosphereConfig()));
        framework.init(sc);
    }

    public AtmosphereFramework framework() {
        return framework;
    }

    /**
     * Weblogic specific comet based implementation.
     *
     * @param rrk
     * @return true if suspended
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    protected boolean doRequest(RequestResponseKey rrk) throws IOException, ServletException {
        try {
            AtmosphereRequest req = AtmosphereRequestImpl.wrap(rrk.getRequest());
            AtmosphereResponse resp = AtmosphereResponseImpl.wrap(rrk.getResponse());
            Action action = framework.doCometSupport(req , resp);
            rrk.getRequest().getSession().setAttribute(WebLogicCometSupport.RRK + resp.uuid(), rrk);
            if (action.type() == Action.TYPE.SUSPEND) {
                if (action.timeout() == -1) {
                    rrk.setTimeout(Integer.MAX_VALUE);
                } else {
                    rrk.setTimeout((int) action.timeout());
                }
            }
            return action.type() == Action.TYPE.SUSPEND;
        } catch (IllegalStateException ex) {
            logger.error("AtmosphereServlet.doRequest exception", ex);
            throw ex;
        }
    }

    /**
     * Weblogic specific comet based implementation.
     *
     * @param rrk
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    protected void doResponse(RequestResponseKey rrk, Object context)
            throws IOException, ServletException {
        rrk.getResponse().flushBuffer();
    }

    /**
     * Weblogic specific comet based implementation.
     *
     * @param rrk
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    protected void doTimeout(RequestResponseKey rrk) throws IOException, ServletException {
        ((AsynchronousProcessor) framework.getAsyncSupport()).timedout(AtmosphereRequestImpl.wrap(rrk.getRequest()),
                AtmosphereResponseImpl.wrap(rrk.getResponse()));
    }


}
