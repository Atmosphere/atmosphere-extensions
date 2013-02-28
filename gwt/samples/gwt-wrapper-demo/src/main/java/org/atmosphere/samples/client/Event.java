package org.atmosphere.samples.client;

import java.io.Serializable;

/**
 * simple event
 * @author jotec
 */
public class Event implements Serializable {

    private String data;
    
    public Event() {
    }

    public Event(String data) {
        this.data = data;
    }
    
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

  @Override
  public String toString() {
    return "Event data=" + getData();
  }
    
    
        
}
