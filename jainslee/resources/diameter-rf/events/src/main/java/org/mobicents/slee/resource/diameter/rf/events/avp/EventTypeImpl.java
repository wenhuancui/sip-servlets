package org.mobicents.slee.resource.diameter.rf.events.avp;

import net.java.slee.resource.diameter.rf.events.avp.EventType;

import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * EventTypeImpl.java
 *
 * <br>Project:  mobicents
 * <br>11:20:17 AM Apr 11, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class EventTypeImpl extends GroupedAvpImpl implements EventType {

  public EventTypeImpl() {
    super();
  }

  /**
   * @param code
   * @param vendorId
   * @param mnd
   * @param prt
   * @param value
   */
  public EventTypeImpl( int code, long vendorId, int mnd, int prt, byte[] value ) {
    super( code, vendorId, mnd, prt, value );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.EventType#getEvent()
   */
  public String getEvent() {
    return getAvpAsUTF8String(DiameterRfAvpCodes.EVENT, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.EventType#getExpires()
   */
  public long getExpires() {
    return getAvpAsUnsigned32(DiameterRfAvpCodes.EXPIRES, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.EventType#getSipMethod()
   */
  public String getSipMethod() {
    return getAvpAsUTF8String(DiameterRfAvpCodes.SIP_METHOD, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.EventType#hasEvent()
   */
  public boolean hasEvent() {
    return hasAvp(DiameterRfAvpCodes.EVENT, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.EventType#hasExpires()
   */
  public boolean hasExpires() {
    return hasAvp(DiameterRfAvpCodes.EXPIRES, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.EventType#hasSipMethod()
   */
  public boolean hasSipMethod() {
    return hasAvp(DiameterRfAvpCodes.SIP_METHOD, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.EventType#setEvent(String)
   */
  public void setEvent( String event ) {
    addAvp(DiameterRfAvpCodes.EVENT, DiameterRfAvpCodes.TGPP_VENDOR_ID, event);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.EventType#setExpires(long)
   */
  public void setExpires( long expires ) {
    addAvp(DiameterRfAvpCodes.EXPIRES, DiameterRfAvpCodes.TGPP_VENDOR_ID, expires);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.EventType#setSipMethod(String)
   */
  public void setSipMethod( String sipMethod )
  {
    addAvp(DiameterRfAvpCodes.SIP_METHOD, DiameterRfAvpCodes.TGPP_VENDOR_ID, sipMethod);
  }

}
