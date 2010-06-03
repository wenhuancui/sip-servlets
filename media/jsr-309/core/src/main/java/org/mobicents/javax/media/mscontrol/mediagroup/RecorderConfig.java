package org.mobicents.javax.media.mscontrol.mediagroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.mediagroup.Recorder;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.Resource;
import javax.media.mscontrol.resource.Trigger;


import org.mobicents.javax.media.mscontrol.DefaultEventGeneratorFactory;
import org.mobicents.javax.media.mscontrol.ParametersImpl;
import org.mobicents.protocols.mgcp.jain.pkg.AUMgcpEvent;
import org.mobicents.protocols.mgcp.jain.pkg.AUPackage;

/**
 * 
 * @author amit bhayani
 * 
 */
public class RecorderConfig {

	private final List<DefaultEventGeneratorFactory> eventGenFctList;
	private final List<RecorderEventDetectorFactory> eventDetFctList;

	private final ParametersImpl parametersImpl;
	private final Set<EventType> eventTypes;
	private final Set<Qualifier> qualifiers;
	private final Set<Trigger> triggers;
	private final Set<Value> values;
	private final Set<Action> actions;

	private RecorderConfig(boolean clone) {
		parametersImpl = new ParametersImpl();
		eventTypes = new HashSet<EventType>();
		qualifiers = new HashSet<Qualifier>();
		triggers = new HashSet<Trigger>();
		values = new HashSet<Value>();
		actions = new HashSet<Action>();

		eventDetFctList = new ArrayList<RecorderEventDetectorFactory>();
		eventGenFctList = new ArrayList<DefaultEventGeneratorFactory>();

	}

	public RecorderConfig() {

		parametersImpl = new ParametersImpl();
		parametersImpl.put(Recorder.MAX_DURATION, Resource.FOR_EVER);
		parametersImpl.put(Recorder.START_BEEP, false);

		eventTypes = new HashSet<EventType>();
		qualifiers = new HashSet<Qualifier>();
		triggers = new HashSet<Trigger>();
		values = new HashSet<Value>();
		actions = new HashSet<Action>();

		eventGenFctList = constructEveGenFctList();
		eventDetFctList = new ArrayList<RecorderEventDetectorFactory>();

	}

	private List<DefaultEventGeneratorFactory> constructEveGenFctList() {
		List<DefaultEventGeneratorFactory> generatorList = new ArrayList<DefaultEventGeneratorFactory>();

		DefaultEventGeneratorFactory ann = new DefaultEventGeneratorFactory(AUPackage.AU.toString(), AUMgcpEvent.aupr
				.getName(), false);
		generatorList.add(ann);

		return generatorList;
	}

	protected List<DefaultEventGeneratorFactory> getEventGenFctList() {
		return eventGenFctList;
	}

	protected List<RecorderEventDetectorFactory> getEventDetFctList() {
		return eventDetFctList;
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

	public RecorderConfig createCustomizedClone(Parameters params) {
		RecorderConfig clone = new RecorderConfig(true);

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
