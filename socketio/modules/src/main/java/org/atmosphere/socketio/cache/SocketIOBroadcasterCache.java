package org.atmosphere.socketio.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.atmosphere.cache.AbstractBroadcasterCache;
import org.atmosphere.cache.SessionBroadcasterCache;
import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.cache.UUIDBroadcasterCache.CacheMessage;
import org.atmosphere.cache.UUIDBroadcasterCache.ClientQueue;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.BroadcasterCache.Message;
import org.atmosphere.socketio.cpr.SocketIOAtmosphereHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketIOBroadcasterCache extends UUIDBroadcasterCache {
	
	private static final Logger logger = LoggerFactory.getLogger(SocketIOBroadcasterCache.class);
	
	@Override
	public CacheMessage addCacheCandidate(String broadcasterID, AtmosphereResource resource, Object msg) {
		logger.trace("addCacheCandidate broadcasterID[" + broadcasterID + "]  AtmosphereResource UUID = " + "[" + uuid(resource) + "] message=" + msg);
		return super.addCacheCandidate(broadcasterID, resource, msg);
	}

	@Override
	public void addToCache(String broadcasterID, AtmosphereResource resource, Message msg) {
		logger.trace("addToCache broadcasterID[" + broadcasterID + "]  AtmosphereResource UUID = " + "[" + uuid(resource) + "] message=" + msg.message);
		super.addToCache(broadcasterID, resource, msg);
	}

	@Override
	public void clearCache(String broadcasterID, AtmosphereResourceImpl resource, CacheMessage message) {
		logger.trace("clearCache broadcasterID[" + broadcasterID + "]  AtmosphereResource UUID = " + "[" + uuid(resource) + "] message=" + message);
		super.clearCache(broadcasterID, resource, message);
	}

	@Override
	public List<Object> retrieveFromCache(String broadcasterID, AtmosphereResource resource) {
		logger.trace("retrieveFromCache broadcasterID[" + broadcasterID + "]  AtmosphereResource UUID = " + "[" + uuid(resource) + "]");
		return super.retrieveFromCache(broadcasterID, resource);
	}
	
	protected String uuid(AtmosphereResource r) {
		if(r==null)return null;
		
		String uuid = (String) r.getRequest().getAttribute(ApplicationConfig.SUSPENDED_ATMOSPHERE_RESOURCE_UUID);
		if(uuid==null){
			uuid = r.uuid();
		} 
		
        return uuid;
    }

}
