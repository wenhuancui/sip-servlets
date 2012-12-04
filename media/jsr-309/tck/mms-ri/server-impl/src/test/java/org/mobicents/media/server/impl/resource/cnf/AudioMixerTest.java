/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.impl.resource.cnf;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mobicents.media.Utils;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.resource.fft.SpectraAnalyzer;
import org.mobicents.media.server.impl.resource.fft.SpectrumEvent;
import org.mobicents.media.server.impl.resource.test.SineGenerator;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.Timer;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 *
 * @author kulikov
 */
public class AudioMixerTest {

    private final static int FREQ_ERROR = 5;
    private int MAX_ERRORS = 3;
    private final static int[] FREQ = new int[]{50, 150, 250};

    private SineGenerator g1,  g2,  g3;
    private SpectraAnalyzer a;
    
    private AudioMixer mixer;
    private Timer timer;
    
    private ArrayList<double[]> s;
    
    public AudioMixerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        timer = new TimerImpl();
        timer.start();
        
        s = new ArrayList();
        
        short A = (short)(Short.MAX_VALUE / 3);
        
        a = new SpectraAnalyzer("a1");
        a.addListener(new AnalyzerListener(s));

        g1 = new SineGenerator("g1", timer);
        g1.setFrequency(FREQ[0]);
        g1.setAmplitude(A);

        g2 = new SineGenerator("g2", timer);
        g2.setFrequency(FREQ[1]);
        g2.setAmplitude(A);
        
        g3 = new SineGenerator("g3", timer);
        g3.setFrequency(FREQ[2]);
        g3.setAmplitude(A);
        
        mixer = new AudioMixer("mixer", timer);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getOutput method, of class AudioMixer.
     */
    @Test
    public void testGetOutput() throws InterruptedException {
        mixer.connect(g1);
        mixer.connect(g2);
        mixer.connect(g3);
        mixer.connect(a);
        
        mixer.start();
        a.start();
        
        g1.start();
        g2.start();
        g3.start();
        
        Thread.currentThread().sleep(10000);
        
        a.stop();
        mixer.stop();
        
        g1.stop();
        g2.stop();
        g3.stop();
        
        Thread.currentThread().sleep(1000);
        
        boolean res = verify(s, new int[]{FREQ[0], FREQ[1], FREQ[2]});
        assertEquals(true, res);
        
        
    }

    private class AnalyzerListener implements NotificationListener {

        private ArrayList<double[]> s;

        public AnalyzerListener(ArrayList<double[]> s) {
            this.s = s;
        }

        public void update(NotifyEvent event) {
            switch (event.getEventID()) {
                case SpectrumEvent.SPECTRA :
                    SpectrumEvent evt = (SpectrumEvent) event;
                    s.add(evt.getSpectra());
                    break;
                case NotifyEvent.START_FAILED :
                    break;
            }
        }
    }

    private boolean verify(ArrayList<double[]> spectra, int[] F) {
        int errorCount = 0;
        if (spectra.size() == 0) {
            return false;
        }
        
        int i =0;
        for (double[] s : spectra) {            
            int[] ext = Utils.getFreq(s);
            boolean r = Utils.checkFreq(ext, F, FREQ_ERROR);
            if (!r) {
                errorCount++;
            }
        }
        return (errorCount <= MAX_ERRORS);
    }
    
}