/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import javax.sdp.MediaDescription;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

/**
 *
 * @author kulikov
 */
public class TestSdp {
    
    public static void main(String[] args) throws Exception {
        InetSocketAddress a;
        SdpFactory sdpFactory = SdpFactory.getInstance();
        SessionDescription sdp = sdpFactory.createSessionDescription();
        sdp.setVersion(sdpFactory.createVersion(0));
        MediaDescription md = sdpFactory.createMediaDescription("audio", 123, 1, "RTP/AVP", new int[0]);
        md.getMedia().getMediaFormats(true).add(0);
        sdp.getMediaDescriptions(true).add(md);
        System.out.println(sdp);
    }
}
