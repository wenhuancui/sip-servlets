package org.mobicents.slee.resource.diameter.rf.events.avp;

import net.java.slee.resource.diameter.rf.events.avp.AddressDomain;

import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * 
 * AddressDomainImpl.java
 *
 * <br>Project:  mobicents
 * <br>12:52:48 AM Apr 11, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class AddressDomainImpl extends GroupedAvpImpl implements AddressDomain {

  public AddressDomainImpl() {
    super();
  }

  /**
   * 
   * @param code
   * @param vendorId
   * @param mnd
   * @param prt
   * @param value
   */
  public AddressDomainImpl(int code, long vendorId, int mnd, int prt, byte[] value) {
    super( code, vendorId, mnd, prt, value );
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AddressDomain#getDomainName()
   */
  public String getDomainName() {
    return getAvpAsUTF8String(DiameterRfAvpCodes.DOMAIN_NAME, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AddressDomain#getTgppImsiMccMnc()
   */
  public byte[] getTgppImsiMccMnc() {
    return getAvpAsRaw(DiameterRfAvpCodes.TGPP_IMSI_MCC_MNC, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AddressDomain#hasDomainName()
   */
  public boolean hasDomainName() {
    return hasAvp(DiameterRfAvpCodes.DOMAIN_NAME, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AddressDomain#hasTgppImsiMccMnc()
   */
  public boolean hasTgppImsiMccMnc() {
    return hasAvp(DiameterRfAvpCodes.TGPP_IMSI_MCC_MNC, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AddressDomain#setDomainName(String)
   */
  public void setDomainName(String domainName) {
    addAvp(DiameterRfAvpCodes.DOMAIN_NAME, DiameterRfAvpCodes.TGPP_VENDOR_ID, domainName);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.AddressDomain#setTgppImsiMccMnc(byte[])
   */
  public void setTgppImsiMccMnc(byte[] tgppImsiMccMnc) {
    addAvp(DiameterRfAvpCodes.TGPP_IMSI_MCC_MNC, DiameterRfAvpCodes.TGPP_VENDOR_ID, tgppImsiMccMnc);
  }

}
