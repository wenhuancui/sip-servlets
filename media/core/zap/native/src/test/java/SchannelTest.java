import junit.framework.*;

/**
 * 
 * @author amit bhayani
 *
 */
public class SchannelTest extends TestCase {

	public SchannelTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testNativeHelloWorld() throws Exception {
		org.mobicents.media.server.impl.resource.zap.Schannel app = new org.mobicents.media.server.impl.resource.zap.Schannel();

		this.assertEquals("Hello Native World!", app.sayHello());
	}

}
