package org.mobicents.javax.media.mscontrol.mediagroup;

import java.util.HashSet;
import java.util.Set;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.Trigger;

import org.mobicents.javax.media.mscontrol.MediaSessionImpl;
import org.mobicents.javax.media.mscontrol.ParametersImpl;
import org.mobicents.javax.media.mscontrol.mediagroup.signals.SignalDetectorConfig;
import org.mobicents.javax.media.mscontrol.resource.ExtendedParameter;

/**
 * 
 * @author amit.bhayani
 * 
 */
public class MediaGroupConfig {

	private final ParametersImpl parametersImpl;

	private final Set<Parameter> parameters;
	private final Set<Action> actions;
	private final Set<EventType> eventTypes;
	private final Set<Qualifier> qualifiers;
	private final Set<Trigger> triggers;
	private final Set<Value> values;

	private PlayerConfig playerConfig;
	private RecorderConfig recorderConfig;
	private SignalDetectorConfig sigDetConfig;

	public MediaGroupConfig(boolean clone) {
		this.parametersImpl = new ParametersImpl();
		this.parameters = new HashSet<Parameter>();
		this.actions = new HashSet<Action>();
		this.eventTypes = new HashSet<EventType>();
		this.qualifiers = new HashSet<Qualifier>();
		this.triggers = new HashSet<Trigger>();
		this.values = new HashSet<Value>();
	}

	public MediaGroupConfig(PlayerConfig playerConfig, RecorderConfig recorderConfig, SignalDetectorConfig sigDetConfig) {

		this.parametersImpl = new ParametersImpl();
		this.parametersImpl.put(ExtendedParameter.ENDPOINT_LOCAL_NAME, "/mobicents/media/IVR/$");
		this.parametersImpl.put(MediaObject.MEDIAOBJECT_ID, Parameters.NO_PARAMETER);
		this.parametersImpl.put(MediaSession.TIMEOUT, MediaSessionImpl.SESSION_TIMEOUT);

		this.parameters = new HashSet<Parameter>();
		this.actions = new HashSet<Action>();
		this.eventTypes = new HashSet<EventType>();
		this.qualifiers = new HashSet<Qualifier>();
		this.triggers = new HashSet<Trigger>();
		this.values = new HashSet<Value>();

		this.parameters.addAll(this.parametersImpl.keySet());

		init(playerConfig, recorderConfig, sigDetConfig);

	}

	private void init(PlayerConfig playerConfig, RecorderConfig recorderConfig, SignalDetectorConfig sigDetConfig) {
		this.playerConfig = playerConfig;

		if (this.playerConfig != null) {
			this.parameters.addAll(playerConfig.getParameters());
			this.actions.addAll(playerConfig.getActions());
			this.eventTypes.addAll(playerConfig.getEventTypes());
			this.qualifiers.addAll(playerConfig.getQualifiers());
			this.triggers.addAll(playerConfig.getTriggers());
			this.values.addAll(playerConfig.getValues());
		}

		this.recorderConfig = recorderConfig;

		if (this.recorderConfig != null) {
			this.parameters.addAll(recorderConfig.getParameters());
			this.actions.addAll(recorderConfig.getActions());
			this.eventTypes.addAll(recorderConfig.getEventTypes());
			this.qualifiers.addAll(recorderConfig.getQualifiers());
			this.triggers.addAll(recorderConfig.getTriggers());
			this.values.addAll(recorderConfig.getValues());
		}

		this.sigDetConfig = sigDetConfig;

		if (this.sigDetConfig != null) {
			this.parameters.addAll(this.sigDetConfig.getParameters());
			this.actions.addAll(this.sigDetConfig.getActions());
			this.eventTypes.addAll(this.sigDetConfig.getEventTypes());
			this.qualifiers.addAll(this.sigDetConfig.getQualifiers());
			this.triggers.addAll(this.sigDetConfig.getTriggers());
			this.values.addAll(this.sigDetConfig.getValues());
		}
	}

	public ParametersImpl getParametersImpl() {
		ParametersImpl newparams = new ParametersImpl();
		newparams.putAll(this.parametersImpl);
		if (this.playerConfig != null) {
			newparams.putAll(this.playerConfig.getParametersImpl());
		}
		if (this.recorderConfig != null) {
			newparams.putAll(this.recorderConfig.getParametersImpl());
		}
		if (this.sigDetConfig != null) {
			newparams.putAll(this.sigDetConfig.getParametersImpl());
		}
		return newparams;
	}

	public void setParametersImpl(Parameters params) {
		for (Parameter p : params.keySet()) {
			for (Parameter actual : this.parametersImpl.keySet()) {
				if (p == actual) {
					this.parametersImpl.put(actual, params.get(actual));
				}
			}
		}

		if (this.playerConfig != null) {
			this.playerConfig.setParametersImpl(params);
		}

		if (this.recorderConfig != null) {
			this.recorderConfig.setParametersImpl(params);
		}

		if (this.sigDetConfig != null) {
			this.sigDetConfig.setParametersImpl(params);
		}
	}

	public Set<Parameter> getParameters() {
		return parameters;
	}

	public Set<Action> getActions() {
		return actions;
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

	public PlayerConfig getPlayerConfig() {
		return playerConfig;
	}

	public RecorderConfig getRecorderConfig() {
		return recorderConfig;
	}

	public SignalDetectorConfig getSigDetConfig() {
		return sigDetConfig;
	}

	public MediaGroupConfig createCustomizedClone(Parameters params) {

		MediaGroupConfig mgCongClone = new MediaGroupConfig(true);
		mgCongClone.parametersImpl.putAll(this.parametersImpl);

		PlayerConfig plCfClone = null;
		if (this.playerConfig != null) {
			plCfClone = this.playerConfig.createCustomizedClone(params);
		}

		RecorderConfig reCfClone = null;
		if (this.recorderConfig != null) {
			reCfClone = this.recorderConfig.createCustomizedClone(params);
		}

		SignalDetectorConfig sedDetCfClone = null;
		if (this.sigDetConfig != null) {
			sedDetCfClone = this.sigDetConfig.createCustomizedClone(params);
		}

		mgCongClone.init(plCfClone, reCfClone, sedDetCfClone);

		if (params != null && params != Parameters.NO_PARAMETER) {
			for (Parameter p : mgCongClone.parametersImpl.keySet()) {
				for (Parameter pArg : params.keySet()) {
					if (p == pArg) {
						mgCongClone.parametersImpl.put(pArg, params.get(pArg));
					}
				}
			}
		}

		return mgCongClone;
	}

}
