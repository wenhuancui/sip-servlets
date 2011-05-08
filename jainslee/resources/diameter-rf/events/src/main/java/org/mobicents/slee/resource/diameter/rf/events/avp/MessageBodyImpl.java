package org.mobicents.slee.resource.diameter.rf.events.avp;

import net.java.slee.resource.diameter.rf.events.avp.MessageBody;
import net.java.slee.resource.diameter.rf.events.avp.Originator;

import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * MessageBodyImpl.java
 *
 * <br>Project:  mobicents
 * <br>8:08:04 PM Apr 11, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class MessageBodyImpl extends GroupedAvpImpl implements MessageBody {

  public MessageBodyImpl() {
    super();
  }

  /**
   * @param code
   * @param vendorId
   * @param mnd
   * @param prt
   * @param value
   */
  public MessageBodyImpl( int code, long vendorId, int mnd, int prt, byte[] value ) {
    super( code, vendorId, mnd, prt, value );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#getContentDisposition()
   */
  public String getContentDisposition() {
    return getAvpAsUTF8String(DiameterRfAvpCodes.CONTENT_DISPOSITION, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#getContentLength()
   */
  public long getContentLength() {
    return getAvpAsUnsigned32(DiameterRfAvpCodes.CONTENT_LENGTH, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#getContentType()
   */
  public String getContentType() {
    return getAvpAsUTF8String(DiameterRfAvpCodes.CONTENT_TYPE, DiameterRfAvpCodes.TGPP_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#getOriginator()
   */
  public Originator getOriginator() {
    return (Originator) getAvpAsEnumerated(DiameterRfAvpCodes.ORIGINATOR, DiameterRfAvpCodes.TGPP_VENDOR_ID, Originator.class);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#hasContentDisposition()
   */
  public boolean hasContentDisposition() {
    return hasAvp( DiameterRfAvpCodes.CONTENT_DISPOSITION, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#hasContentLength()
   */
  public boolean hasContentLength() {
    return hasAvp( DiameterRfAvpCodes.CONTENT_LENGTH, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#hasContentType()
   */
  public boolean hasContentType() {
    return hasAvp( DiameterRfAvpCodes.CONTENT_TYPE, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#hasOriginator()
   */
  public boolean hasOriginator() {
    return hasAvp( DiameterRfAvpCodes.ORIGINATOR, DiameterRfAvpCodes.TGPP_VENDOR_ID );
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#setContentDisposition(String)
   */
  public void setContentDisposition( String contentDisposition ) {
    addAvp(DiameterRfAvpCodes.CONTENT_DISPOSITION, DiameterRfAvpCodes.TGPP_VENDOR_ID, contentDisposition);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#setContentLength(long)
   */
  public void setContentLength( long contentLength ) {
    addAvp(DiameterRfAvpCodes.CONTENT_LENGTH, DiameterRfAvpCodes.TGPP_VENDOR_ID, contentLength);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#setContentType(String)
   */
  public void setContentType( String contentType ) {
    addAvp(DiameterRfAvpCodes.CONTENT_TYPE, DiameterRfAvpCodes.TGPP_VENDOR_ID, contentType);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rf.events.avp.MessageBody#setOriginator(net.java.slee.resource.diameter.rf.events.avp.Originator)
   */
  public void setOriginator( Originator originator ) {
    addAvp(DiameterRfAvpCodes.ORIGINATOR, DiameterRfAvpCodes.TGPP_VENDOR_ID, originator.getValue());
  }

}
