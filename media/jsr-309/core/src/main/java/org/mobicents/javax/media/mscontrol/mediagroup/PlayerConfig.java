package org.mobicents.javax.media.mscontrol.mediagroup;

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
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.Resource;
import javax.media.mscontrol.resource.Trigger;
import javax.media.mscontrol.resource.enums.EventTypeEnum;

import org.mobicents.javax.media.mscontrol.DefaultEventGeneratorFactory;
import org.mobicents.javax.media.mscontrol.ParametersImpl;

/**
 * 
 * @author amit bhayani
 * 
 */
public class PlayerConfig {

	private final ParametersImpl parameters;
	private final Set<EventType> eventTypes;
	private final Set<Qualifier> qualifiers;
	private final Set<Trigger> triggers;
	private final Set<Value> values;
	private final Set<Action> actions;

	private final List<PlayerEventDetectorFactory> eventDetFctList;
	private final List<DefaultEventGeneratorFactory> eventGenFctList;
	
	private final List<PlayerEventDetectorFactory> eventDetFctListTTS;
	private final List<DefaultEventGeneratorFactory> eventGenFctListTTS;
	
	private final List<PlayerEventDetectorFactory> eventDetFctListALL;

	private PlayerConfig(boolean clone) {
		parameters = new ParametersImpl();
		eventTypes = new HashSet<EventType>();
		qualifiers = new HashSet<Qualifier>();
		triggers = new HashSet<Trigger>();
		values = new HashSet<Value>();
		actions = new HashSet<Action>();

		eventDetFctList = new ArrayList<PlayerEventDetectorFactory>();
		eventGenFctList = new ArrayList<DefaultEventGeneratorFactory>();
		
		eventDetFctListTTS = new ArrayList<PlayerEventDetectorFactory>();
		eventGenFctListTTS = new ArrayList<DefaultEventGeneratorFactory>();
		
		eventDetFctListALL = new ArrayList<PlayerEventDetectorFactory>();

	}

	public PlayerConfig() {

		eventDetFctList = constructEveDetFctList();
		eventGenFctList = constructEveGenFctList();
		
		eventDetFctListTTS = constructEveDetFctListTTS();
		eventGenFctListTTS = constructEveGenFctListTTS();
		
		eventDetFctListALL = constructEveDetFctListALL();

		parameters = new ParametersImpl();
		parameters.put(Player.BEHAVIOUR_IF_BUSY, Player.QUEUE_IF_BUSY);
		parameters.put(Player.MAX_DURATION, Resource.FOREVER);
		parameters.put(Player.INTERVAL, new Integer(0));
		parameters.put(Player.REPEAT_COUNT, new Integer(1));
		parameters.put(Player.START_OFFSET, new Integer(0));

		eventTypes = new HashSet<EventType>();

		for (PlayerEventDetectorFactory fac : eventDetFctList) {
			eventTypes.add(fac.getMediaEventType());
		}

		qualifiers = new HashSet<Qualifier>();
		qualifiers.add(PlayerEvent.DURATION_EXCEEDED);
		qualifiers.add(PlayerEvent.END_OF_PLAY_LIST);

		triggers = new HashSet<Trigger>();

		values = new HashSet<Value>();
		values.add(Player.FAIL_IF_BUSY);
		values.add(Player.QUEUE_IF_BUSY);
		values.add(Player.STOP_IF_BUSY);

		actions = new HashSet<Action>();
	}

	private List<PlayerEventDetectorFactory> constructEveDetFctList() {
		List<PlayerEventDetectorFactory> detectorList = new ArrayList<PlayerEventDetectorFactory>();

		PlayerEventDetectorFactory event = new PlayerEventDetectorFactory(PackageName.Announcement.toString(),
				MgcpEvent.oc.getName(), false, EventTypeEnum.PLAY_COMPLETED, true);
		detectorList.add(event);

		event = new PlayerEventDetectorFactory(PackageName.Announcement.toString(),
				MgcpEvent.of.getName(), false, EventTypeEnum.PLAY_COMPLETED, false);
		detectorList.add(event);

		return detectorList;
	}
	
	private List<PlayerEventDetectorFactory> constructEveDetFctListALL() {
		List<PlayerEventDetectorFactory> detectorList = new ArrayList<PlayerEventDetectorFactory>();

		detectorList.addAll(constructEveDetFctList());
		detectorList.addAll(constructEveDetFctListTTS());
		return detectorList;
	}
	
	private List<PlayerEventDetectorFactory> constructEveDetFctListTTS() {
		PackageName auPackageName=PackageName.factory("AU");
		List<PlayerEventDetectorFactory> detectorList = new ArrayList<PlayerEventDetectorFactory>();

		PlayerEventDetectorFactory event = new PlayerEventDetectorFactory(auPackageName.toString(),
				MgcpEvent.oc.getName(), false, EventTypeEnum.PLAY_COMPLETED, true);
		detectorList.add(event);

		event = new PlayerEventDetectorFactory(auPackageName.toString(),
				MgcpEvent.of.getName(), false, EventTypeEnum.PLAY_COMPLETED, false);
		detectorList.add(event);

		return detectorList;
	}

	private List<DefaultEventGeneratorFactory> constructEveGenFctList() {
		List<DefaultEventGeneratorFactory> generatorList = new ArrayList<DefaultEventGeneratorFactory>();

		DefaultEventGeneratorFactory ann = new DefaultEventGeneratorFactory(PackageName.Announcement.toString(),
				MgcpEvent.ann.getName(), false);
		generatorList.add(ann);

		return generatorList;
	}
	
	private List<DefaultEventGeneratorFactory> constructEveGenFctListTTS() {
		List<DefaultEventGeneratorFactory> generatorList = new ArrayList<DefaultEventGeneratorFactory>();
		PackageName auPackageName=PackageName.factory("AU");
		DefaultEventGeneratorFactory ann = new DefaultEventGeneratorFactory(auPackageName.toString(),
				MgcpEvent.ann.getName(), false);
		generatorList.add(ann);

		return generatorList;
	}

	public ParametersImpl getParametersImpl() {
		return parameters;
	}

	public void setParametersImpl(Parameters params) {
		for (Parameter p : params.keySet()) {
			for (Parameter actual : this.parameters.keySet()) {
				if (p == actual) {
					this.parameters.put(actual, params.get(actual));
				}
			}
		}
	}

	public Set<Parameter> getParameters() {
		return parameters.keySet();
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

	protected List<PlayerEventDetectorFactory> getEventDetFctList() {
		return eventDetFctList;
	}

	protected List<DefaultEventGeneratorFactory> getEventGenFctList() {
		return eventGenFctList;
	}
	
	protected List<PlayerEventDetectorFactory> getEventDetFctListTTS() {
		return eventDetFctListTTS;
	}

	protected List<DefaultEventGeneratorFactory> getEventGenFctListTTS() {
		return eventGenFctListTTS;
	}
	
	protected List<PlayerEventDetectorFactory> getEventDetFctListALL() {
		return eventDetFctListALL;
	}

	public PlayerConfig createCustomizedClone(Parameters params) {
		PlayerConfig clone = new PlayerConfig(true);
		clone.parameters.putAll(this.parameters);
		clone.eventTypes.addAll(this.eventTypes);
		clone.qualifiers.addAll(this.qualifiers);
		clone.triggers.addAll(this.triggers);
		clone.values.addAll(this.values);
		clone.actions.addAll(this.actions);

		clone.eventDetFctList.addAll(this.eventDetFctList);
		clone.eventGenFctList.addAll(this.eventGenFctList);

		if (params != null && params != Parameters.NO_PARAMETER) {
			for (Parameter p : clone.parameters.keySet()) {
				for (Parameter pArg : params.keySet()) {
					if (p.equals(pArg)) {
						clone.parameters.put(pArg, params.get(pArg));
					}
				}
			}
		}

		return clone;
	}

}
