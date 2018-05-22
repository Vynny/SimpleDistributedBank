package fe;

import org.omg.CORBA.LocalObject;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

/**
 * ServantManager to create new FE implementation on requests.
 * 
 * Source: https://docs.oracle.com/javase/7/docs/technotes/guides/idl/servantlocators.html
 */
public class PoaServantLocator extends LocalObject implements ServantLocator {
    public Servant preinvoke(byte[] oid, POA adapter, String operation, CookieHolder the_cookie) throws ForwardRequest {
        try {
            FrontEndImpl feImpl = new FrontEndImpl();
            feImpl.setAttributes("1234");
            System.out.println("PoaServantLocator.preinvoke(): Created \"" + feImpl.getClass().getName() + "\" "
                    + "servant object for \"" + adapter.the_name() + "\"");
            return feImpl;
        } catch (Exception e) {
            System.err.println("preinvoke: Caught exception - " + e);
        }
        return null;
    }

    public void postinvoke(byte[] oid, POA adapter, String operation, java.lang.Object the_cookie,
            Servant the_servant) {
        try {
            System.out.println("PoaServantLocator.postinvoke(): For \"" + adapter.the_name() + "\" adapter of servant "
                    + "object type \"" + the_servant.getClass().getName() + "\"");
        } catch (Exception e) {
            System.err.println("postinvoke: Caught exception - " + e);
        }
    }

}
