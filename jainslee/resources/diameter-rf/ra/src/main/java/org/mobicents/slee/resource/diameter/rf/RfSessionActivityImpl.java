package org.mobicents.slee.resource.diameter.rf;

import net.java.slee.resource.diameter.base.DiameterAvpFactory;
import net.java.slee.resource.diameter.base.DiameterMessageFactory;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentity;
import net.java.slee.resource.diameter.rf.RfSessionActivity;

import org.jdiameter.api.Answer;
import org.jdiameter.api.EventListener;
import org.jdiameter.api.Request;
import org.jdiameter.api.Session;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.mobicents.slee.resource.diameter.base.DiameterActivityImpl;

/**
 *
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public abstract class RfSessionActivityImpl extends DiameterActivityImpl implements RfSessionActivity, StateChangeListener<AppSession>{

  private static final long serialVersionUID = 4602851199202393775L;

  /**
   * @param messageFactory
   * @param avpFactory
   * @param session
   * @param raEventListener
   * @param destinationHost
   * @param destinationRealm
   */
  public RfSessionActivityImpl(DiameterMessageFactory messageFactory, DiameterAvpFactory avpFactory, Session session, EventListener<Request, Answer> raEventListener,
      DiameterIdentity destinationHost, DiameterIdentity destinationRealm) {
    super(messageFactory, avpFactory, session, raEventListener, destinationHost, destinationRealm);
  }

}
