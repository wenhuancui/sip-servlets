package org.mobicents.slee.resource.diameter.rf.handlers;

import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.SessionFactory;
import org.jdiameter.api.rf.ClientRfSession;
import org.jdiameter.api.rf.ServerRfSession;
import org.jdiameter.api.rf.events.RfAccountingAnswer;
import org.jdiameter.api.rf.events.RfAccountingRequest;
import org.jdiameter.common.impl.app.rf.RfSessionFactoryImpl;
import org.mobicents.slee.resource.diameter.base.handlers.DiameterRAInterface;

/**
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class RfSessionFactory extends RfSessionFactoryImpl {

  public DiameterRAInterface ra;

  /**
   * 
   */
  public RfSessionFactory() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param sessionFactory
   */
  public RfSessionFactory(DiameterRAInterface ra, SessionFactory sessionFactory) {
    super(sessionFactory);
    this.ra = ra;
  }

  @Override
  public void doRfAccountingAnswerEvent(ClientRfSession appSession, RfAccountingRequest acr, RfAccountingAnswer aca) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    ra.fireEvent(appSession.getSessions().get(0).getSessionId(), aca.getMessage());
  }

  @Override
  public void doRfAccountingRequestEvent(ServerRfSession appSession, RfAccountingRequest acr) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    ra.fireEvent(appSession.getSessions().get(0).getSessionId(), acr.getMessage());
  }

}
