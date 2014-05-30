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


package org.atmosphere.plugin.rmi.test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import org.atmosphere.cpr.*;
import org.atmosphere.plugin.rmi.*;
import org.atmosphere.plugin.rmi.test.org.atmoshere.cpr.DefaultBroadcasterFactoryForTest;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

/**
 * Test for {@link RMIBroadcaster} and {@link RMIFilter}.
 */
public class RMIPluginTest {

    /**
     * Test for filter.
     *
     * @throws Exception if test fails
     */
    @Test
    public void usingRmiFilter() throws Exception {

        // Will contain the broadcasted message received from RMI
        final List<Object> receivedMessages = new ArrayList<Object>();
        final RMIBroadcastService service = new RMIBroadcastServiceImpl(null) {

            @Override
            public void send(Object message) {
                receivedMessages.add(message);
            }
        };

        // Create server notified by the filter
        final Registry registry = LocateRegistry.createRegistry(4001);
        registry.bind(RMIBroadcastService.class.getSimpleName() + "/RMITopic", service);

        // Create broadcaster
        final AtmosphereConfig config = new AtmosphereFramework().getAtmosphereConfig();
        final DefaultBroadcasterFactory factory = new DefaultBroadcasterFactoryForTest(DefaultBroadcaster.class, "NEVER", config);
        config.framework().setBroadcasterFactory(factory);
        final Broadcaster broadcaster = factory.get(DefaultBroadcaster.class, "RMITopic");
        broadcaster.getBroadcasterConfig().addFilter(new RMIFilter());

        // Expect to receive the message in localhost:4001
        broadcaster.broadcast("Use RMI");

        // Check that the message has been received
        assertEquals(1, receivedMessages.size());
        assertEquals("Use RMI", receivedMessages.get(0));
    }

    /**
     * Test for broadcaster.
     *
     * @throws Exception if test fails
     */
    @Test
    public void usingRmiBroadcaster() throws Exception {

        // Will contain the broadcasted message received from RMI
        final List<Object> receivedMessages = new ArrayList<Object>();

        // Mock broadcaster
        final RMIBroadcaster broadcaster = mock(RMIBroadcaster.class);
        when(broadcaster.getID()).thenReturn("/RMITopic");
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                receivedMessages.add(invocation.getArguments()[0]);
                return null;
            }
        }).when(broadcaster).broadcastReceivedMessage(anyString());

        // Bind on mocked service on port 4000
        final RMIBroadcastService service = new RMIBroadcastServiceImpl(broadcaster);
        RMIPeerManager.getInstance().server("RMITopic", service, null);

        // Send message
        final String url = String.format("rmi://localhost:4000/%s/RMITopic", RMIBroadcastService.class.getSimpleName());
        ((RMIBroadcastService) Naming.lookup(url)).send("Use RMI");

        // Check that the message has been received
        assertEquals(1, receivedMessages.size());
        assertEquals("Use RMI", receivedMessages.get(0));
    }
}
