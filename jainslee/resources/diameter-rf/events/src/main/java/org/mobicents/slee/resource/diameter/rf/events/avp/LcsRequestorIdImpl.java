package org.mobicents.slee.resource.diameter.rf.events.avp;

import net.java.slee.resource.diameter.rf.events.avp.LcsRequestorId;

import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * LcsRequestorIdImpl.java
 *
 * <br>Project:  mobicents
 * <br>3:41:44 AM Apr 12, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class LcsRequestorIdImpl extends GroupedAvpImpl implements LcsRequestorId {

  public LcsRequestorIdImpl() {
    super();
  }

  /**
   * @param code
   * @param vendorId
   * @param mnd
   * @param prt
   * @param value
   */
  public LcsRequestorIdImpl( int code, long vendorId, int mnd, int prt, byte[] value ) {
    super( code, vendorId, mnd, prt, value );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.LcsRequestorId#getLcsDataCodingScheme()
   */
  public String getLcsDataCodingScheme() {
    return getAvpAsUTF8String(DiameterRfAvpCodes.LCS_DATA_CODING_SCHEME, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.LcsRequestorId#getLcsRequestorIdString()
   */
  public String getLcsRequestorIdString() {
    return getAvpAsUTF8String(DiameterRfAvpCodes.LCS_REQUESTOR_ID_STRING, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.LcsRequestorId#hasLcsDataCodingScheme()
   */
  public boolean hasLcsDataCodingScheme()
  {
    return hasAvp( DiameterRfAvpCodes.LCS_DATA_CODING_SCHEME, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.LcsRequestorId#hasLcsRequestorIdString()
   */
  public boolean hasLcsRequestorIdString()
  {
    return hasAvp( DiameterRfAvpCodes.LCS_REQUESTOR_ID_STRING, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.LcsRequestorId#setLcsDataCodingScheme(String)
   */
  public void setLcsDataCodingScheme( String lcsDataCodingScheme ) {
    addAvp(DiameterRfAvpCodes.LCS_DATA_CODING_SCHEME, DiameterRfAvpCodes.TGPP_VENDOR_ID, lcsDataCodingScheme);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.LcsRequestorId#setLcsRequestorIdString(String)
   */
  public void setLcsRequestorIdString( String lcsRequestorIdString ) {
    addAvp(DiameterRfAvpCodes.LCS_REQUESTOR_ID_STRING, DiameterRfAvpCodes.TGPP_VENDOR_ID, lcsRequestorIdString);
  }

}
