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
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.ClusterBroadcastFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This {@link ClusterBroadcastFilter} supports broadcasting through RMI in the set of instances composing the cluster.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 1.1.1
 * @see RMIPeerManager
 */
public class RMIFilter implements ClusterBroadcastFilter {

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(RMIFilter.class);

    /**
     * Filtered broadcaster.
     */
    private Broadcaster bc;

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        logger.info("Destroying {}", getClass().getName());
        this.bc = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final AtmosphereConfig config) {
        logger.info("Initializing {}", getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BroadcastAction filter(String broadcasterId, final Object originalMessage, final Object message) {
        logger.info("Filtering message '{}' with {}", new Object[] { message, getClass().getName() });
        RMIPeerManager.getInstance().sendAll(bc.getID(), message);
        return new BroadcastAction(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Broadcaster getBroadcaster() {
        return bc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBroadcaster(final Broadcaster bc) {
        logger.info("Setting broadcaster for {}", getClass().getName());
        this.bc = bc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUri(final String clusterUri) {
        // NO OPS
    }
}
