package org.mobicents.javax.media.mscontrol.mediagroup.signals;

import jain.protocol.ip.mgcp.pkg.MgcpEvent;
import jain.protocol.ip.mgcp.pkg.PackageName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.Resource;
import javax.media.mscontrol.resource.Trigger;
import javax.media.mscontrol.resource.enums.EventTypeEnum;

import org.mobicents.javax.media.mscontrol.DefaultEventGeneratorFactory;
import org.mobicents.javax.media.mscontrol.ParametersImpl;

public final class SignalDetectorConfig {

	private final ParametersImpl parametersImpl;
	private final Set<EventType> eventTypes;
	private final Set<Qualifier> qualifiers;
	private final Set<Trigger> triggers;
	private final Set<Value> values;
	private final Set<Action> actions;

	private final List<SignalDetectorEventDetectorFactory> eventDetFctList;
	private final List<DefaultEventGeneratorFactory> eventGenFctList;

	 private SignalDetectorConfig(boolean clone) {
		parametersImpl = new ParametersImpl();
		eventTypes = new HashSet<EventType>();
		qualifiers = new HashSet<Qualifier>();
		triggers = new HashSet<Trigger>();
		values = new HashSet<Value>();
		actions = new HashSet<Action>();

		eventDetFctList = new ArrayList<SignalDetectorEventDetectorFactory>();
		eventGenFctList = new ArrayList<DefaultEventGeneratorFactory>();

	}

	public SignalDetectorConfig() {

		eventDetFctList = constructEveDetFctList();
		eventGenFctList = constructEveGenFctList();

		parametersImpl = getSignalDetectorParameters();
		actions = new HashSet<Action>();
		eventTypes = getSignalDetectorEventTypes();

		qualifiers = getSignalDetectorQualifiers();

		triggers = getSignalDetectorTriggers();

		values = getSignalDetectorValues();

	}

	private ParametersImpl getSignalDetectorParameters() {
		ParametersImpl paraImpl = new ParametersImpl();
		paraImpl.put(SignalDetector.INITIAL_TIMEOUT, Resource.FOR_EVER);
		paraImpl.put(SignalDetector.MAX_DURATION, Resource.FOR_EVER);
		paraImpl.put(SignalDetector.INTER_SIG_TIMEOUT, Resource.FOR_EVER);

		for (Parameter p : SignalDetector.PATTERN) {
			paraImpl.put(p, Parameters.NO_PARAMETER);
		}

		return paraImpl;
	}

	private Set<EventType> getSignalDetectorEventTypes() {

		// TODO : Shouldn't the eventType be build from the getSigDetEveDetFacList() ?

		Set<EventType> eventTypes = new HashSet<EventType>();
		eventTypes.add(SignalDetectorEvent.OVERFLOWED);
		eventTypes.add(SignalDetectorEvent.RECEIVE_SIGNALS_COMPLETED);
		eventTypes.add(SignalDetectorEvent.SIGNAL_DETECTED);
		for (EventType e : SignalDetectorEvent.PATTERN_MATCHED) {
			eventTypes.add(e);
		}
		return eventTypes;
	}

	private Set<Qualifier> getSignalDetectorQualifiers() {
		Set<Qualifier> qualifiers = new HashSet<Qualifier>();
		qualifiers.add(SignalDetectorEvent.DURATION_EXCEEDED);
		qualifiers.add(SignalDetectorEvent.INITIAL_TIMEOUT_EXCEEDED);
		qualifiers.add(SignalDetectorEvent.INTER_SIG_TIMEOUT_EXCEEDED);
		qualifiers.add(SignalDetectorEvent.NUM_SIGNALS_DETECTED);
		for (Qualifier q : SignalDetectorEvent.PATTERN_MATCHING) {
			qualifiers.add(q);
		}
		qualifiers.add(SignalDetectorEvent.PROMPT_FAILURE);
		return qualifiers;
	}

	private Set<Trigger> getSignalDetectorTriggers() {
		Set<Trigger> triggers = new HashSet<Trigger>();
		triggers.add(SignalDetector.DETECTION_OF_ONE_SIGNAL);
		triggers.add(SignalDetector.FLUSHING_OF_BUFFER);
		for (Trigger t : SignalDetector.PATTERN_MATCH) {
			triggers.add(t);
		}
		triggers.add(SignalDetector.RECEIVE_SIGNALS_COMPLETION);
		return triggers;
	}

	private Set<Value> getSignalDetectorValues() {
		Set<Value> values = new HashSet<Value>();
		// values.add(SignalDetector.DETECTING_MODE);
		// values.add(SignalDetector.IDLE_MODE);
		return values;
	}

	private List<SignalDetectorEventDetectorFactory> constructEveDetFctList() {
		List<SignalDetectorEventDetectorFactory> sigDeteEveDetList = new ArrayList<SignalDetectorEventDetectorFactory>();

		SignalDetectorEventDetectorFactory dtmf0Eve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmf0.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmf0Eve);

		SignalDetectorEventDetectorFactory dtmf1Eve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmf1.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmf1Eve);

		SignalDetectorEventDetectorFactory dtmf2Eve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmf2.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmf2Eve);

		SignalDetectorEventDetectorFactory dtmf3Eve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmf3.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmf3Eve);

		SignalDetectorEventDetectorFactory dtmf4Eve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmf4.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmf4Eve);

		SignalDetectorEventDetectorFactory dtmf5Eve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmf5.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmf5Eve);

		SignalDetectorEventDetectorFactory dtmf6Eve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmf6.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmf6Eve);

		SignalDetectorEventDetectorFactory dtmf7Eve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmf7.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmf7Eve);

		SignalDetectorEventDetectorFactory dtmf8Eve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmf8.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmf8Eve);

		SignalDetectorEventDetectorFactory dtmf9Eve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmf9.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmf9Eve);

		SignalDetectorEventDetectorFactory dtmfAEve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmfA.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmfAEve);

		SignalDetectorEventDetectorFactory dtmfBEve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmfB.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmfBEve);

		SignalDetectorEventDetectorFactory dtmfCEve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmfC.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmfCEve);

		SignalDetectorEventDetectorFactory dtmfDEve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmfD.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmfDEve);

		SignalDetectorEventDetectorFactory dtmfStarEve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmfStar.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmfStarEve);

		SignalDetectorEventDetectorFactory dtmfHashEve = new SignalDetectorEventDetectorFactory(PackageName.Dtmf
				.toString(), MgcpEvent.dtmfHash.getName(), false, EventTypeEnum.SIGNAL_DETECTED, true);
		sigDeteEveDetList.add(dtmfHashEve);

		return sigDeteEveDetList;
	}

	private List<DefaultEventGeneratorFactory> constructEveGenFctList() {
		List<DefaultEventGeneratorFactory> sigDeteEveGenList = new ArrayList<DefaultEventGeneratorFactory>();

		DefaultEventGeneratorFactory dtmf0 = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmf0.getName(), false);
		sigDeteEveGenList.add(dtmf0);

		DefaultEventGeneratorFactory dtmf1 = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmf1.getName(), false);
		sigDeteEveGenList.add(dtmf1);

		DefaultEventGeneratorFactory dtmf2 = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmf2.getName(), false);
		sigDeteEveGenList.add(dtmf2);

		DefaultEventGeneratorFactory dtmf3 = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmf3.getName(), false);
		sigDeteEveGenList.add(dtmf3);

		DefaultEventGeneratorFactory dtmf4 = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmf4.getName(), false);
		sigDeteEveGenList.add(dtmf4);

		DefaultEventGeneratorFactory dtmf5 = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmf5.getName(), false);
		sigDeteEveGenList.add(dtmf5);

		DefaultEventGeneratorFactory dtmf6 = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmf6.getName(), false);
		sigDeteEveGenList.add(dtmf6);

		DefaultEventGeneratorFactory dtmf7 = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmf7.getName(), false);
		sigDeteEveGenList.add(dtmf7);

		DefaultEventGeneratorFactory dtmf8 = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmf8.getName(), false);
		sigDeteEveGenList.add(dtmf8);

		DefaultEventGeneratorFactory dtmf9 = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmf9.getName(), false);
		sigDeteEveGenList.add(dtmf9);

		DefaultEventGeneratorFactory dtmfA = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmfA.getName(), false);
		sigDeteEveGenList.add(dtmfA);

		DefaultEventGeneratorFactory dtmfB = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmfB.getName(), false);
		sigDeteEveGenList.add(dtmfB);

		DefaultEventGeneratorFactory dtmfC = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmfC.getName(), false);
		sigDeteEveGenList.add(dtmfC);

		DefaultEventGeneratorFactory dtmfD = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmfD.getName(), false);
		sigDeteEveGenList.add(dtmfD);

		DefaultEventGeneratorFactory dtmfStar = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmfStar.getName(), false);
		sigDeteEveGenList.add(dtmfStar);

		DefaultEventGeneratorFactory dtmfHash = new DefaultEventGeneratorFactory(PackageName.Dtmf.toString(),
				MgcpEvent.dtmfHash.getName(), false);
		sigDeteEveGenList.add(dtmfHash);

		return sigDeteEveGenList;

	}

	public List<SignalDetectorEventDetectorFactory> getEventDetFctList() {
		return eventDetFctList;
	}

	protected List<DefaultEventGeneratorFactory> getEventGenFctList() {
		return eventGenFctList;
	}

	public ParametersImpl getParametersImpl() {
		return parametersImpl;
	}

	public void setParametersImpl(Parameters params) {
		for (Parameter p : params.keySet()) {
			for (Parameter actual : this.parametersImpl.keySet()) {
				if (p == actual) {
					this.parametersImpl.put(actual, params.get(actual));
				}
			}
		}
	}

	public Set<Parameter> getParameters() {
		return parametersImpl.keySet();
	}

	public Set<EventType> getEventTypes() {
		return eventTypes;
	}

	public Set<Qualifier> getQualifiers() {
		return qualifiers;
	}

	public Set<Trigger> getTriggers() {
		return triggers;
	}

	public Set<Value> getValues() {
		return values;
	}

	public Set<Action> getActions() {
		return actions;
	}

	public SignalDetectorConfig createCustomizedClone(Parameters params) {
		SignalDetectorConfig clone = new SignalDetectorConfig(true);
		 clone.parametersImpl.putAll(this.parametersImpl);
		clone.eventTypes.addAll(this.eventTypes);
		clone.qualifiers.addAll(this.qualifiers);
		clone.triggers.addAll(this.triggers);
		clone.values.addAll(this.values);
		clone.actions.addAll(this.actions);

		clone.eventDetFctList.addAll(this.eventDetFctList);
		clone.eventGenFctList.addAll(this.eventGenFctList);

		if (params != null && params != Parameters.NO_PARAMETER) {
			for (Parameter p : clone.parametersImpl.keySet()) {
				for (Parameter pArg : params.keySet()) {
					if (p == pArg) {
						clone.parametersImpl.put(pArg, params.get(pArg));
					}
				}
			}
		}

		return clone;
	}

}
