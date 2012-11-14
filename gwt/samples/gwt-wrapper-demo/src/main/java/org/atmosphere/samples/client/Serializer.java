package org.atmosphere.samples.client;

import org.atmosphere.samples.client.Event;
import org.atmosphere.extensions.gwtwrapper.client.GwtClientSerializer;
import org.atmosphere.extensions.gwtwrapper.client.GwtSerialTypes;

/**
 *
 * @author jotec
 */
@GwtSerialTypes(Event.class)
abstract public class Serializer extends GwtClientSerializer {

}
