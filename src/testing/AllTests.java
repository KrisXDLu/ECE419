package testing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import org.apache.log4j.Level;

import app_kvServer.KVServer;
import junit.framework.Test;
import junit.framework.TestSuite;
import logger.LogSetup;


public class AllTests {

    static {
        try {
            new LogSetup("logs/testing/test.log", Level.INFO);
            KVServer server = new KVServer(50000, 10, "FIFO", "TestIterateDB");
            server.clearStorage();
            new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Test suite() {
        TestSuite clientSuite = new TestSuite("Basic Storage ServerTest-Suite");
        List<Class<? extends TestCase>> tests = Arrays.asList(
                ConnectionTest.class,
                InteractionTest.class,
                MessageTest.class,
                FIFOCacheTest.class,
                LFUCacheTest.class,
                PersistentStoreTest.class
        );
        for (Class<? extends TestCase> test :
                tests) {
            clientSuite.addTestSuite(test);
        }
        return clientSuite;
    }
}
