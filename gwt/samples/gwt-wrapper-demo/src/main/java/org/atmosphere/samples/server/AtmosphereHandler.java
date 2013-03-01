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
package org.atmosphere.samples.server;

import java.io.IOException;
import java.util.logging.Logger;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.extensions.gwtwrapper.client.Atmosphere;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;

/**
 * @author p.havelaar
 */
public class AtmosphereHandler extends AbstractReflectorAtmosphereHandler {

  static final Logger logger = Logger.getLogger("AtmosphereHandler");
    @Override
    public void onRequest(AtmosphereResource ar) throws IOException {
      if (ar.getRequest().getMethod().equals("GET") ) {
        ar.suspend();
      } else if (ar.getRequest().getMethod().equals("POST") ) {
        Object msg = ar.getRequest().getAttribute(Atmosphere.MESSAGE_OBJECT);
        if (msg != null) {
          logger.info("received post: " + msg.toString());
          ar.getBroadcaster().broadcast(msg);
        }
      }
    }
    

    @Override
    public void destroy() {
        
    }

}
