package net.java.slee.resource.diameter.rf.events;

import net.java.slee.resource.diameter.base.events.DiameterMessage;

/**
 * Defines an interface representing the Accounting-Answer command.
 * 
 * From the Diameter Ro/Rf Protocol (TS 32299-6c0) specification:
 * 
 * <pre>
 *       &lt;ACA&gt; ::= &lt; Diameter Header: 271, PXY &gt;
 *     
 *                 &lt; Session-Id &gt;
 *                 { Result-Code }
 *                 { Origin-Host }
 *                 { Origin-Realm }
 *                 { Accounting-Record-Type }
 *                 { Accounting-Record-Number }
 *                 [ Acct-Application-Id ]
 *                 [ User-Name ]
 *                 [ Acct-Interim-Interval ]
 *                 [ Origin-State-Id ]
 *                 [ Event-Timestamp ]
 *               * [ Proxy-Info ]
 *               * [ AVP ]
 * 
 * </pre>
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public interface RfAccountingAnswer extends DiameterMessage, RfAccountingMessage {

  /**
   * Returns true if the Result-Code AVP is present in the message.
   */
  boolean hasResultCode();

  /**
   * Returns the value of the Result-Code AVP, of type Unsigned32. Use
   * {@link #hasResultCode()} to check the existence of this AVP.
   * 
   * @return the value of the Result-Code AVP
   * @throws IllegalStateException
   *             if the Result-Code AVP has not been set on this message
   */
  long getResultCode();

  /**
   * Sets the value of the Result-Code AVP, of type Unsigned32.
   * 
   * @throws IllegalStateException
   *             if setResultCode has already been called
   */
  void setResultCode(long resultCode);

}
