package org.mobicents.slee.resource.diameter.rf.events.avp;

import net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation;

import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * 
 * AdditionalContentInformationImpl.java
 *
 * <br>Project:  mobicents
 * <br>8:17:22 PM Apr 10, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class AdditionalContentInformationImpl extends GroupedAvpImpl implements AdditionalContentInformation {

  public AdditionalContentInformationImpl() {
    super();
  }

  public AdditionalContentInformationImpl( int code, long vendorId, int mnd, int prt, byte[] value ) {
    super( code, vendorId, mnd, prt, value );
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation#getAdditionalTypeInformation()
   */
  public String getAdditionalTypeInformation() {
    return getAvpAsUTF8String(DiameterRfAvpCodes.ADDITIONAL_TYPE_INFORMATION, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation#getContentSize()
   */
  public long getContentSize() {
    return getAvpAsUnsigned32(DiameterRfAvpCodes.CONTENT_SIZE, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation#getTypeNumber()
   */
  public int getTypeNumber() {
    return getAvpAsInteger32(DiameterRfAvpCodes.TYPE_NUMBER, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation#hasAdditionalTypeInformation()
   */
  public boolean hasAdditionalTypeInformation() {
    return hasAvp(DiameterRfAvpCodes.ADDITIONAL_TYPE_INFORMATION, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation#hasContentSize()
   */
  public boolean hasContentSize() {
    return hasAvp(DiameterRfAvpCodes.CONTENT_SIZE, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation#hasTypeNumber()
   */
  public boolean hasTypeNumber() {
    return hasAvp(DiameterRfAvpCodes.TYPE_NUMBER, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation#setAdditionalTypeInformation(String)
   */
  public void setAdditionalTypeInformation( String additionalTypeInformation ) {
    addAvp(DiameterRfAvpCodes.ADDITIONAL_TYPE_INFORMATION, DiameterRfAvpCodes.TGPP_VENDOR_ID, additionalTypeInformation);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation#setContentSize(long)
   */
  public void setContentSize( long contentSize ) {
    addAvp(DiameterRfAvpCodes.CONTENT_SIZE, DiameterRfAvpCodes.TGPP_VENDOR_ID, contentSize);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation#setTypeNumber(int)
   */
  public void setTypeNumber( int typeNumber ) {
    addAvp(DiameterRfAvpCodes.TYPE_NUMBER, DiameterRfAvpCodes.TGPP_VENDOR_ID, typeNumber);
  }

}
