package org.mobicents.javax.media.mscontrol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaConfigException;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.mixer.MixerAdapter;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.resource.video.VideoLayout;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.mediagroup.MediaGroupConfig;
import org.mobicents.javax.media.mscontrol.mediagroup.PlayerConfig;
import org.mobicents.javax.media.mscontrol.mediagroup.RecorderConfig;
import org.mobicents.javax.media.mscontrol.mediagroup.signals.SignalDetectorConfig;
import org.mobicents.javax.media.mscontrol.mixer.MediaMixerConfig;
import org.mobicents.javax.media.mscontrol.mixer.MixerAdapterConfig;
import org.mobicents.javax.media.mscontrol.networkconnection.NetworkConnectionConfig;
import org.mobicents.jsr309.mgcp.MgcpStackFactory;
import org.mobicents.jsr309.mgcp.MgcpWrapper;
import org.xml.sax.SAXException;

/**
 * 
 * @author amit bhayani
 * 
 */
public class MsControlFactoryImpl implements MsControlFactory {

	private static final Logger logger = Logger
			.getLogger(MsControlFactoryImpl.class);

	private Properties properties = null;
	private MgcpWrapper mgcpWrapper = null;

	private List<Integer> list = new ArrayList<Integer>();

	private List<MediaSession> mediaSessionList = new ArrayList<MediaSession>();

	private XMLParser parser = new XMLParser();

	// protected static Map<Configuration, MediaConfigImpl>
	// configVsMediaConfigMap = new HashMap<Configuration, MediaConfigImpl>();

	public MsControlFactoryImpl(Properties properties) {
		this.properties = properties;

		MgcpStackFactory mgcpStackFactory = MgcpStackFactory.getInstance();
		this.mgcpWrapper = mgcpStackFactory.getMgcpStackProvider(properties);

		if (mgcpWrapper == null) {
			throw new RuntimeException(
					"Could not create instance of MediaSessionFactory. Check the exception in logs");
		}

		// // NC.BASIC
		// configVsMediaConfigMap.put(NetworkConnection.BASIC,
		// this.createNetConnBasic());
		//
		// // MG.PPLAYER
		// configVsMediaConfigMap.put(MediaGroup.PLAYER,
		// this.createMedGrpPlayer());
		//
		// // MG.PLAYER_SIGNALDETECTOR
		// configVsMediaConfigMap.put(MediaGroup.PLAYER_SIGNALDETECTOR,
		// this.createMedGrpPlayerSignDete());
		//
		// // MG.SIGNALDETECTOR
		// configVsMediaConfigMap.put(MediaGroup.SIGNALDETECTOR,
		// this.createMedGrpSignDete());
		//
		// // MG.PLAYER_RECORDER_SIGNALDETECTOR
		// configVsMediaConfigMap.put(MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR,
		// this.createMedGrpPlaRecSigDet());
		//
		// // MMX.AUDIO
		// configVsMediaConfigMap.put(MediaMixer.AUDIO, this.createMedMixAud());
		//
		// // MXAD.DTMF_CLAMP
		// configVsMediaConfigMap.put(MixerAdapter.DTMF_CLAMP,
		// this.createMixAdaDtmfClamp());
		//
		// // MXAD.DTMFCLAMP_VOLUME
		// configVsMediaConfigMap.put(MixerAdapter.DTMFCLAMP_VOLUME,
		// this.createMixAdaDtmfClampVolume());
		//
		// // MXAD.EMPTY
		// configVsMediaConfigMap.put(MixerAdapter.EMPTY,
		// this.createMixAdaEmpty());
	}

	private MediaConfigImpl createMedMixAud() {

		MediaMixerConfig mmConfig = new MediaMixerConfig();
		SupportedFeaturesImpl suppFeatures = new SupportedFeaturesImpl(mmConfig);
		MediaConfigImpl mmMediaConf = new MediaConfigImpl(suppFeatures);
		return mmMediaConf;
	}

	private MediaConfigImpl createNetConnBasic() {

		NetworkConnectionConfig ncConfig = new NetworkConnectionConfig();
		SupportedFeaturesImpl suppFeatures = new SupportedFeaturesImpl(ncConfig);
		MediaConfigImpl ncMediaConf = new MediaConfigImpl(suppFeatures);

		return ncMediaConf;
	}

	private MediaConfigImpl createMedGrpPlayer() {

		PlayerConfig playerConf = new PlayerConfig();
		MediaGroupConfig medGrpConfig = new MediaGroupConfig(playerConf, null,
				null);
		SupportedFeaturesImpl suppFeatures = new SupportedFeaturesImpl(null,
				medGrpConfig, null, null);
		MediaConfigImpl ncMediaConf = new MediaConfigImpl(suppFeatures);
		return ncMediaConf;
	}

	private MediaConfigImpl createMedGrpSignDete() {

		SignalDetectorConfig sigDetConfig = new SignalDetectorConfig();
		MediaGroupConfig medGrpConfig = new MediaGroupConfig(null, null,
				sigDetConfig);
		SupportedFeaturesImpl suppFeatures = new SupportedFeaturesImpl(null,
				medGrpConfig, null, null);
		MediaConfigImpl mgMediaConf = new MediaConfigImpl(suppFeatures);

		return mgMediaConf;

	}

	private MediaConfigImpl createMedGrpPlayerSignDete() {

		PlayerConfig playerConf = new PlayerConfig();
		SignalDetectorConfig sigDetConfig = new SignalDetectorConfig();
		MediaGroupConfig medGrpConfig = new MediaGroupConfig(playerConf, null,
				sigDetConfig);
		SupportedFeaturesImpl suppFeatures = new SupportedFeaturesImpl(null,
				medGrpConfig, null, null);
		MediaConfigImpl mgMediaConf = new MediaConfigImpl(suppFeatures);

		return mgMediaConf;
	}

	private MediaConfigImpl createMedGrpPlaRecSigDet() {
		PlayerConfig playerConf = new PlayerConfig();
		RecorderConfig recConfig = new RecorderConfig();
		SignalDetectorConfig sigDetConfig = new SignalDetectorConfig();

		MediaGroupConfig medGrpConfig = new MediaGroupConfig(playerConf,
				recConfig, sigDetConfig);
		SupportedFeaturesImpl suppFeatures = new SupportedFeaturesImpl(null,
				medGrpConfig, null, null);
		MediaConfigImpl mgMediaConf = new MediaConfigImpl(suppFeatures);
		return mgMediaConf;
	}

	private MediaConfigImpl createMixAdaDtmfClamp() {
		MixerAdapterConfig mixAdtConf = new MixerAdapterConfig("DTMF_CLAMP");
		SupportedFeaturesImpl suppFeatures = new SupportedFeaturesImpl(
				mixAdtConf);
		MediaConfigImpl mgMediaConf = new MediaConfigImpl(suppFeatures);
		return mgMediaConf;
	}

	private MediaConfigImpl createMixAdaDtmfClampVolume() {
		MixerAdapterConfig mixAdtConf = new MixerAdapterConfig(
				"DTMFCLAMP_VOLUME");
		SupportedFeaturesImpl suppFeatures = new SupportedFeaturesImpl(
				mixAdtConf);
		MediaConfigImpl mgMediaConf = new MediaConfigImpl(suppFeatures);
		return mgMediaConf;
	}

	private MediaConfigImpl createMixAdaEmpty() {
		MixerAdapterConfig mixAdtConf = new MixerAdapterConfig("EMPTY");
		SupportedFeaturesImpl suppFeatures = new SupportedFeaturesImpl(
				mixAdtConf);
		MediaConfigImpl mgMediaConf = new MediaConfigImpl(suppFeatures);
		return mgMediaConf;
	}

	public MediaSession createMediaSession() {
		MediaSession medSession = new MediaSessionImpl(this.mgcpWrapper, this);
		mediaSessionList.add(medSession);
		return medSession;
	}

	public Parameters createParameters() {
		return new ParametersImpl();
	}

	public VideoLayout createVideoLayout(String mimeType, Reader xmlDef)
			throws MediaConfigException {
		// TODO Auto-generated method stub
		return null;
	}

	public VideoLayout getPresetLayout(String type) throws MediaConfigException {
		return null;
	}

	public VideoLayout[] getPresetLayouts(int numberOfLiveRegions)
			throws MediaConfigException {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public MediaConfig getMediaConfig(Configuration<?> paramConfiguration)
			throws MediaConfigException {
		if (paramConfiguration.equals(NetworkConnection.BASIC)) {
			return this.createNetConnBasic();
		} else if (paramConfiguration.equals(MediaGroup.PLAYER)) {
			return this.createMedGrpPlayer();
		} else if (paramConfiguration.equals(MediaGroup.PLAYER_SIGNALDETECTOR)) {
			return this.createMedGrpPlayerSignDete();
		} else if (paramConfiguration.equals(MediaGroup.SIGNALDETECTOR)) {
			return this.createMedGrpSignDete();
		} else if (paramConfiguration
				.equals(MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR)) {
			return this.createMedGrpPlaRecSigDet();
		} else if (paramConfiguration.equals(MediaMixer.AUDIO)) {
			return this.createMedMixAud();
		} else if (paramConfiguration.equals(MixerAdapter.DTMF_CLAMP)) {
			return this.createMixAdaDtmfClamp();
		} else if (paramConfiguration.equals(MixerAdapter.DTMFCLAMP_VOLUME)) {
			return this.createMixAdaDtmfClampVolume();
		} else if (paramConfiguration.equals(MixerAdapter.EMPTY)) {
			return this.createMixAdaEmpty();
		}

		throw new MediaConfigException("Unsupported Configuration "
				+ paramConfiguration);
	}

	public MediaConfig getMediaConfig(Reader paramReader)
			throws MediaConfigException {
		int c;
		MediaConfigImpl config = null;

		try {
			while ((c = paramReader.read()) != -1) {
				list.add(c);
			}

			byte[] b = new byte[list.size()];
			int count = 0;
			for (int i : list) {
				b[count] = (byte) i;
				count++;
			}

			list.clear();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(b);
			config = parser.parse(null, inputStream);
		} catch (IOException e) {
			logger.error(e);
			throw new MediaConfigException(e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			logger.error(e);
			throw new MediaConfigException(e.getMessage(), e);
		} catch (SAXException e) {
			logger.error(e);
			throw new MediaConfigException(e.getMessage(), e);
		}

		return config;
	}

	public MediaObject getMediaObject(URI paramURI) {
		for (MediaSession ms : this.mediaSessionList) {
			if (ms.getURI().equals(paramURI)) {
				return ms;
			}

			MediaSessionImpl msImpl = (MediaSessionImpl) ms;
			for (NetworkConnection nc : msImpl.getNetConnList()) {
				if (nc.getURI().equals(paramURI)) {
					return nc;
				}
			}

			for (MediaGroup mg : msImpl.getMedGrpList()) {
				if (mg.getURI().equals(paramURI)) {
					return mg;
				}
			}

			for (MediaMixer mx : msImpl.getMedMxrList()) {
				if (mx.getURI().equals(paramURI)) {
					return mx;
				}
			}
		}

		return null;
	}

	protected List<MediaSession> getMediaSessionList() {
		return this.mediaSessionList;
	}

}
