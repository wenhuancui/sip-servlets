package org.mobicents.javax.media.mscontrol;

import java.util.HashSet;
import java.util.Set;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.SupportedFeatures;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.Trigger;

import org.mobicents.javax.media.mscontrol.mediagroup.MediaGroupConfig;
import org.mobicents.javax.media.mscontrol.mixer.MediaMixerConfig;
import org.mobicents.javax.media.mscontrol.mixer.MixerAdapterConfig;
import org.mobicents.javax.media.mscontrol.networkconnection.NetworkConnectionConfig;

/**
 * 
 * @author amit bhayani
 * 
 */
public class SupportedFeaturesImpl implements SupportedFeatures {

	private XMLParser parser = null;

	private final Set<Parameter> parameters;
	private final Set<Action> actions;
	private final Set<EventType> eventTypes;
	private final Set<Qualifier> qualifiers;
	private final Set<Trigger> triggers;
	private final Set<Value> values;

	private final NetworkConnectionConfig netConConfig;
	private final MediaGroupConfig medGrpConfig;
	private final MediaMixerConfig medMixConfig;
	private final MixerAdapterConfig mixAdaConfig;

	protected SupportedFeaturesImpl(MixerAdapterConfig mixAdaConfig) {
		this(null, null, null, mixAdaConfig);
	}

	protected SupportedFeaturesImpl(NetworkConnectionConfig netConnConfig) {
		this(netConnConfig, null, null, null);
	}

	protected SupportedFeaturesImpl(MediaMixerConfig mmConfig) {
		this(null, null, mmConfig, null);
	}

	protected SupportedFeaturesImpl(MediaGroupConfig medGrpConfig) {
		this(null, medGrpConfig, null, null);
	}

	protected SupportedFeaturesImpl(NetworkConnectionConfig netConnConfig, MediaGroupConfig medGrpConfig,
			MediaMixerConfig mmConfig, MixerAdapterConfig mixAdaConfig) {

		this.parameters = new HashSet<Parameter>();
		this.actions = new HashSet<Action>();
		this.eventTypes = new HashSet<EventType>();
		this.qualifiers = new HashSet<Qualifier>();
		this.triggers = new HashSet<Trigger>();
		this.values = new HashSet<Value>();

		this.netConConfig = netConnConfig;

		if (this.netConConfig != null) {
			this.parameters.addAll(this.netConConfig.getParameters());
			this.actions.addAll(this.netConConfig.getActions());
			this.eventTypes.addAll(this.netConConfig.getEventTypes());
			this.qualifiers.addAll(this.netConConfig.getQualifiers());
			this.triggers.addAll(this.netConConfig.getTriggers());
			this.values.addAll(this.netConConfig.getValues());
		}

		this.medGrpConfig = medGrpConfig;

		if (this.medGrpConfig != null) {
			this.parameters.addAll(medGrpConfig.getParameters());
			this.actions.addAll(medGrpConfig.getActions());
			this.eventTypes.addAll(medGrpConfig.getEventTypes());
			this.qualifiers.addAll(medGrpConfig.getQualifiers());
			this.triggers.addAll(medGrpConfig.getTriggers());
			this.values.addAll(medGrpConfig.getValues());
		}

		this.medMixConfig = mmConfig;

		if (this.medMixConfig != null) {
			this.parameters.addAll(this.medMixConfig.getParameters());
			this.actions.addAll(this.medMixConfig.getActions());
			this.eventTypes.addAll(this.medMixConfig.getEventTypes());
			this.qualifiers.addAll(this.medMixConfig.getQualifiers());
			this.triggers.addAll(this.medMixConfig.getTriggers());
			this.values.addAll(this.medMixConfig.getValues());
		}

		this.mixAdaConfig = mixAdaConfig;
		if (this.mixAdaConfig != null) {
			this.parameters.addAll(this.mixAdaConfig.getParameters());
			this.actions.addAll(this.mixAdaConfig.getActions());
			this.eventTypes.addAll(this.mixAdaConfig.getEventTypes());
			this.qualifiers.addAll(this.mixAdaConfig.getQualifiers());
			this.triggers.addAll(this.mixAdaConfig.getTriggers());
			this.values.addAll(this.mixAdaConfig.getValues());
		}
	}

	public NetworkConnectionConfig getNetConConfig() {
		return netConConfig;
	}

	public MediaGroupConfig getMedGrpConfig() {
		return medGrpConfig;
	}

	public MediaMixerConfig getMedMixConfig() {
		return medMixConfig;
	}
	
	public MixerAdapterConfig getMixAdaConfig(){
		return this.mixAdaConfig;
	}

	/**
	 * SupportedFeatures methods
	 */

	public Set<Action> getSupportedActions() {
		return this.actions;
	}

	public Set<EventType> getSupportedEventTypes() {
		return this.eventTypes;
	}

	public Set<Parameter> getSupportedParameters() {
		return this.parameters;
	}

	public Set<Qualifier> getSupportedQualifiers() {
		return this.qualifiers;
	}

	public Set<Trigger> getSupportedTriggers() {
		return this.triggers;
	}

	public Set<Value> getSupportedValues() {
		return this.values;
	}

	protected SupportedFeaturesImpl createCustomizedClone(Parameters params) {

		NetworkConnectionConfig netCfClone = null;
		if (this.netConConfig != null) {
			netCfClone = this.netConConfig.createCustomizedClone(params);
		}

		MediaGroupConfig mgCfClone = null;
		if (this.medGrpConfig != null) {
			mgCfClone = this.medGrpConfig.createCustomizedClone(params);
		}

		MediaMixerConfig mmCfClone = null;
		if (this.medMixConfig != null) {
			mmCfClone = this.medMixConfig.createCustomizedClone(params);
		}
		
		MixerAdapterConfig maCfClone = null;
		if(this.mixAdaConfig!=null){
			maCfClone = this.mixAdaConfig.createCustomizedClone(params);
		}

		return new SupportedFeaturesImpl(netCfClone, mgCfClone, mmCfClone, maCfClone);
	}

}
