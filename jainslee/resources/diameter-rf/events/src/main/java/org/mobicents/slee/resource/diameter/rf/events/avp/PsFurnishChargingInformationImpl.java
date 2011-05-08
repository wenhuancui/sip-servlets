package org.mobicents.slee.resource.diameter.rf.events.avp;

import net.java.slee.resource.diameter.rf.events.avp.PsAppendFreeFormatData;
import net.java.slee.resource.diameter.rf.events.avp.PsFurnishChargingInformation;

import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * PsFurnishChargingInformationImpl.java
 *
 * <br>Project:  mobicents
 * <br>12:46:28 PM Apr 13, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class PsFurnishChargingInformationImpl extends GroupedAvpImpl implements PsFurnishChargingInformation {

  public PsFurnishChargingInformationImpl() {
    super();
  }

  /**
   * @param code
   * @param vendorId
   * @param mnd
   * @param prt
   * @param value
   */
  public PsFurnishChargingInformationImpl( int code, long vendorId, int mnd, int prt, byte[] value ) {
    super( code, vendorId, mnd, prt, value );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.PsFurnishChargingInformation#getPsAppendFreeFormatData()
   */
  public PsAppendFreeFormatData getPsAppendFreeFormatData() {
    return (PsAppendFreeFormatData) getAvpAsEnumerated(DiameterRfAvpCodes.PS_APPEND_FREE_FORMAT_DATA, DiameterRfAvpCodes.TGPP_VENDOR_ID, PsAppendFreeFormatData.class);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.PsFurnishChargingInformation#getPsFreeFormatData()
   */
  public String getPsFreeFormatData() {
    return getAvpAsOctetString(DiameterRfAvpCodes.PS_FREE_FORMAT_DATA, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.PsFurnishChargingInformation#getTgppChargingId()
   */
  public String getTgppChargingId() {
    return getAvpAsOctetString(DiameterRfAvpCodes.TGPP_CHARGING_ID, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.PsFurnishChargingInformation#hasPsAppendFreeFormatData()
   */
  public boolean hasPsAppendFreeFormatData() {
    return hasAvp( DiameterRfAvpCodes.PS_APPEND_FREE_FORMAT_DATA, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.PsFurnishChargingInformation#hasPsFreeFormatData()
   */
  public boolean hasPsFreeFormatData() {
    return hasAvp( DiameterRfAvpCodes.PS_FREE_FORMAT_DATA, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.PsFurnishChargingInformation#hasTgppChargingId()
   */
  public boolean hasTgppChargingId() {
    return hasAvp( DiameterRfAvpCodes.TGPP_CHARGING_ID, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.PsFurnishChargingInformation#setPsAppendFreeFormatData(net.java.slee.resource.diameter.rf.events.avp.PsAppendFreeFormatData)
   */
  public void setPsAppendFreeFormatData( PsAppendFreeFormatData psAppendFreeFormatData ) {
    addAvp(DiameterRfAvpCodes.PS_APPEND_FREE_FORMAT_DATA, DiameterRfAvpCodes.TGPP_VENDOR_ID, psAppendFreeFormatData.getValue());
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.PsFurnishChargingInformation#setPsFreeFormatData(byte[])
   */
  public void setPsFreeFormatData( String psFreeFormatData ) {
    addAvp(DiameterRfAvpCodes.PS_FREE_FORMAT_DATA, DiameterRfAvpCodes.TGPP_VENDOR_ID, psFreeFormatData);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.PsFurnishChargingInformation#setTgppChargingId(byte[])
   */
  public void setTgppChargingId( String tgppChargingId ) {
    addAvp(DiameterRfAvpCodes.TGPP_CHARGING_ID, DiameterRfAvpCodes.TGPP_VENDOR_ID, tgppChargingId);
  }

}
