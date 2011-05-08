/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
