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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * <p>
 * Default implementation of the {@link RMIBroadcastService} which can be bound to the RMI registry. It wraps a
 * broadcaster to use when message is received.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 1.1.1
 */
public class RMIBroadcastServiceImpl extends UnicastRemoteObject implements RMIBroadcastService {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(RMIBroadcastServiceImpl.class);

    /**
     * The broadcaster.
     */
    private RMIBroadcaster broadcaster;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param b the broadcaster
     * @throws RemoteException if an error occurs remotely
     */
    public RMIBroadcastServiceImpl(final RMIBroadcaster b) throws RemoteException {
        broadcaster = b;
    }

    /**
     * {@inheritDoc}
     */
    public void send(final Object message) {
        logger.info("Receiving message to be broadcasted : {}", message);
        broadcaster.broadcastReceivedMessage(message);
    }
}
