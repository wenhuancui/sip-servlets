/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.impl.resource.video;

import java.io.RandomAccessFile;

/**
 *
 * @author kulikov
 */
public class TestRead {
    public static void main(String[] args) throws Exception {
        RandomAccessFile file = new RandomAccessFile("c:\\video\\001.mp4", "r");
        file.seek(23360 -4);
        
        byte[] buffer = new byte[1000];
        file.read(buffer);
        
        for (int i = 0; i < 20; i++) {
            System.out.println(Integer.toHexString(buffer[i]));
        }
    }
}
