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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <p>
 * Represents a {@code Remote} object which expose the capability to send a message to be broadcasted to the server
 * which is hosting it.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 1.1.1
 */
public interface RMIBroadcastService extends Remote {

    /**
     * <p>
     * Sends a message to be broadcasted.
     * </p>
     *
     * @param message the message
     * @throws RemoteException if an error occurs remotely
     */
    void send(Object message) throws RemoteException;
}
