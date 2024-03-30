package gwt.example.server;

import static org.eclipse.jetty.util.resource.ResourceFactory.combine;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;

public class Main {
  public static void main(String[] args) throws Exception {
    var server =
        new Server(
            InetSocketAddress.createUnresolved(
                System.getProperty("example.bindAddress", "0.0.0.0"),
                Integer.getInteger("example.port", 8000)));
    server.setStopAtShutdown(true);

    var contextHandler = new ServletContextHandler();
    server.setHandler(contextHandler);

    List<Resource> list = new ArrayList<>();
    for (String arg : args) {
      list.add(contextHandler.newResource(arg));
    }
    contextHandler.setBaseResource(combine(list));

    contextHandler.addServlet(GreetingServiceImpl.class, "/example/greet");

    contextHandler.addServlet(DefaultServlet.class, "/*");

    server.start();
    server.join();
  }
}
