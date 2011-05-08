package org.mobicents.slee.resource.diameter.rf.events.avp;

import net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation;
import net.java.slee.resource.diameter.rf.events.avp.MmContentType;

import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * MmContentTypeImpl.java
 *
 * <br>Project:  mobicents
 * <br>9:16:09 AM Apr 13, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class MmContentTypeImpl extends GroupedAvpImpl implements MmContentType {

  public MmContentTypeImpl() {
    super();
  }

  /**
   * @param code
   * @param vendorId
   * @param mnd
   * @param prt
   * @param value
   */
  public MmContentTypeImpl( int code, long vendorId, int mnd, int prt, byte[] value ) {
    super( code, vendorId, mnd, prt, value );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#getAdditionalContentInformations()
   */
  public AdditionalContentInformation[] getAdditionalContentInformations() {
    return (AdditionalContentInformation[]) getAvpsAsCustom(DiameterRfAvpCodes.ADDITIONAL_TYPE_INFORMATION, DiameterRfAvpCodes.TGPP_VENDOR_ID, AdditionalContentInformation.class);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#getAdditionalTypeInformation()
   */
  public String getAdditionalTypeInformation() {
    return getAvpAsUTF8String(DiameterRfAvpCodes.ADDITIONAL_TYPE_INFORMATION, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#getContentSize()
   */
  public long getContentSize() {
    return getAvpAsUnsigned32(DiameterRfAvpCodes.CONTENT_SIZE, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#getTypeNumber()
   */
  public int getTypeNumber() {
    return getAvpAsInteger32(DiameterRfAvpCodes.TYPE_NUMBER, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#hasAdditionalTypeInformation()
   */
  public boolean hasAdditionalTypeInformation() {
    return hasAvp( DiameterRfAvpCodes.ADDITIONAL_TYPE_INFORMATION, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#hasContentSize()
   */
  public boolean hasContentSize() {
    return hasAvp( DiameterRfAvpCodes.CONTENT_SIZE, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#hasTypeNumber()
   */
  public boolean hasTypeNumber() {
    return hasAvp( DiameterRfAvpCodes.TYPE_NUMBER, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#setAdditionalContentInformation(net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation)
   */
  public void setAdditionalContentInformation( AdditionalContentInformation additionalContentInformation ) {
    addAvp(DiameterRfAvpCodes.ADDITIONAL_CONTENT_INFORMATION, DiameterRfAvpCodes.TGPP_VENDOR_ID, additionalContentInformation.byteArrayValue());
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#setAdditionalContentInformations(net.java.slee.resource.diameter.rf.events.avp.AdditionalContentInformation[])
   */
  public void setAdditionalContentInformations( AdditionalContentInformation[] additionalContentInformations ) {
    for(AdditionalContentInformation additionalContentInformation : additionalContentInformations) {
      setAdditionalContentInformation(additionalContentInformation);
    }
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#setAdditionalTypeInformation(String)
   */
  public void setAdditionalTypeInformation( String additionalTypeInformation ) {
    addAvp(DiameterRfAvpCodes.ADDITIONAL_TYPE_INFORMATION, DiameterRfAvpCodes.TGPP_VENDOR_ID, additionalTypeInformation);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#setContentSize(long)
   */
  public void setContentSize( long contentSize ) {
    addAvp(DiameterRfAvpCodes.CONTENT_SIZE, DiameterRfAvpCodes.TGPP_VENDOR_ID, contentSize);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MmContentType#setTypeNumber(int)
   */
  public void setTypeNumber( int typeNumber ) {
    addAvp(DiameterRfAvpCodes.TYPE_NUMBER, DiameterRfAvpCodes.TGPP_VENDOR_ID, typeNumber);
  }

}
