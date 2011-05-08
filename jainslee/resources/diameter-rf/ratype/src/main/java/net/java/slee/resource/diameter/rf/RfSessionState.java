package net.java.slee.resource.diameter.rf;

/**
 * Enumeration of RfAccounting session states.
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public enum RfSessionState {
  Idle, PendingS, PendingE, PendingB, Open, PendingI, PendingL, PendingC;
}
