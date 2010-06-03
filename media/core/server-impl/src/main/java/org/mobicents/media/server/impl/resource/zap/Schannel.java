/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
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
	private static final String _LIB_NAME_ = "zap-native-linux.so";
	private static final String _MMS_HOME_ = "MMS_HOME";
	private static final String _SCHANNEL_HOME_ = "lib_schannel";

	public native int open(int[] zapId);

	public native int read(byte[] buffer);

	public native void write(byte[] buffer, int len);

	public native void close();

	public native String sayHello();

	static {
		try {
			Map<String, String> env = System.getenv();
			String path = null;
			if (env.get(_MMS_HOME_) == null) {
				// this mean we are runnign in different env? like jslee
				path = env.get(_SCHANNEL_HOME_) + File.separator + _LIB_NAME_;
				//System.loadLibrary("zap-native-linux");
			} else {
				path = env.get(_MMS_HOME_) + File.separator + "native"
						+ File.separator + _LIB_NAME_;
				System.load(path);
			}
			System.out.println("Loaded "+_LIB_NAME_);
		} catch (UnsatisfiedLinkError x) {
			x.printStackTrace();
		}

	}
}
