/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.impl.resource.zap;

import java.io.File;
import java.util.Map;

/**
 * 
 * @author kulikov
 * @author baranowb
 */
public class Schannel {
	private static final String _LIB_NAME_ = "schannel.so";
	private static final String _MMS_HOME_ = "MMS_HOME";
	private static final String _SCHANNEL_HOME_ = "lib_schannel";
	public native int open(int zapId);

	public native int read(byte[] buffer);

	public native void write(byte[] buffer, int len);

	public native void close();

	static {
		try {
			Map<String,String> env = System.getenv();
			String path = null;
			if(env.get(_MMS_HOME_) == null)
			{
				//this mean we are runnign in different env? like jslee
				path = env.get(_SCHANNEL_HOME_)+File.separator+_LIB_NAME_;
			}else
			{
				path = env.get(_MMS_HOME_)+File.separator+"native"+File.separator+_LIB_NAME_;
			}
			System.load(path);
		} catch (UnsatisfiedLinkError x) {
			x.printStackTrace();
		}

	}
}
