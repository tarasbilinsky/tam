package base;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import com.google.inject.Provider;
public class EbeanServerProvider implements Provider<EbeanServer> {

    @Override
    public EbeanServer get() {
        // EbeanServer configured by ebean.properties
        return Ebean.getDefaultServer();
    }
}
