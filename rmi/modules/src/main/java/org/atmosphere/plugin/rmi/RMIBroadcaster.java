/*
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


package org.atmosphere.plugin.rmi;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.util.AbstractBroadcasterProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * <p>
 * This broadcaster is able, each time a message is broadcasted, to update the a cluster through RMI.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 1.1.1
 * @see RMIPeerManager
 */
public class RMIBroadcaster extends AbstractBroadcasterProxy {

    /**
     * Block until the server is ready.
     */
    private final CountDownLatch ready = new CountDownLatch(1);

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(RMIBroadcaster.class);

    /**
     * <p>
     * Builds a new instance identified by the given ID.
     * </p>
     *
     * @param id the ID
     * @param config the configuration
     */
    public RMIBroadcaster(final String id, final AtmosphereConfig config) {
        super(id, null, config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incomingBroadcast() {
        try {
            logger.info("Starting Atmosphere RMI Clustering support");
            RMIPeerManager.getInstance().server(getID(), new RMIBroadcastServiceImpl(this));
        } catch (Throwable t) {
            logger.warn("Failed to initialize RMI server", t);
        } finally {
            ready.countDown();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void outgoingBroadcast(Object o) {
        logger.info("Outgoing broadcast for {}", o);

        super.broadcastReceivedMessage(o);
        RMIPeerManager.getInstance().sendAll(getID(), o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void broadcastReceivedMessage(final Object message) {
        super.broadcastReceivedMessage(message);
    }
}
