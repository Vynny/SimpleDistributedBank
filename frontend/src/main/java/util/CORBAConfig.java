package util;

import java.util.Properties;

public class CORBAConfig {
    public static Properties getCorbaProperties() {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "1050");
        props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
        return props;
    }
}
