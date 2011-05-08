package org.mobicents.slee.resource.diameter.rf.events.avp;

import net.java.slee.resource.diameter.rf.events.avp.LocationType;
import net.java.slee.resource.diameter.rf.events.avp.WlanRadioContainer;

import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * WlanRadioContainerImpl.java
 *
 * <br>Project:  mobicents
 * <br>4:33:06 PM Apr 13, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class WlanRadioContainerImpl extends GroupedAvpImpl implements WlanRadioContainer {

  public WlanRadioContainerImpl() {
    super();
  }

  /**
   * @param code
   * @param vendorId
   * @param mnd
   * @param prt
   * @param value
   */
  public WlanRadioContainerImpl( int code, long vendorId, int mnd, int prt, byte[] value ) {
    super( code, vendorId, mnd, prt, value );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.WlanRadioContainer#getLocationType()
   */
  public LocationType getLocationType() {
    return (LocationType) getAvpAsCustom(DiameterRfAvpCodes.LOCATION_TYPE, DiameterRfAvpCodes.TGPP_VENDOR_ID, LocationTypeImpl.class);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.WlanRadioContainer#getWlanTechnology()
   */
  public long getWlanTechnology() {
    return getAvpAsUnsigned32(DiameterRfAvpCodes.WLAN_TECHNOLOGY, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.WlanRadioContainer#hasLocationType()
   */
  public boolean hasLocationType() {
    return hasAvp( DiameterRfAvpCodes.LOCATION_TYPE, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.WlanRadioContainer#hasWlanTechnology()
   */
  public boolean hasWlanTechnology() {
    return hasAvp( DiameterRfAvpCodes.WLAN_TECHNOLOGY, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.WlanRadioContainer#setLocationType(net.java.slee.resource.diameter.rf.events.avp.LocationType)
   */
  public void setLocationType( LocationType locationType ) {
    addAvp(DiameterRfAvpCodes.LOCATION_TYPE, DiameterRfAvpCodes.TGPP_VENDOR_ID, locationType.byteArrayValue());
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.WlanRadioContainer#setWlanTechnology(long)
   */
  public void setWlanTechnology( long wlanTechnology ) {
    addAvp(DiameterRfAvpCodes.WLAN_TECHNOLOGY, DiameterRfAvpCodes.TGPP_VENDOR_ID, wlanTechnology);
  }

}
