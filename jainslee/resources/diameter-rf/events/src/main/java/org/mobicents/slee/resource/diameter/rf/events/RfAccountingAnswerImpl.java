package org.mobicents.slee.resource.diameter.rf.events;

import net.java.slee.resource.diameter.rf.events.RfAccountingAnswer;

import org.jdiameter.api.Message;

/**
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class RfAccountingAnswerImpl extends RfAccountingMessageImpl implements RfAccountingAnswer {

  /**
   * @param message
   */
  public RfAccountingAnswerImpl(Message message) {
    super(message);
  }

  @Override
  public String getLongName() {
    // return "Rf-Accounting-Answer";
    return "Accounting-Answer";
  }

  @Override
  public String getShortName() {
    return "ACA";
  }

}
