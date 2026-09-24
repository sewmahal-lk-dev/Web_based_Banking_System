import org.apache.catalina.startup.Tomcat;
import java.nio.file.Path;

/** Loopback-only public-page preview; no database fixtures or authenticated requests. */
class PremiumPreview {
    public static void main(String[] args) throws Exception {
        Tomcat server = new Tomcat();
        server.setBaseDir(Path.of("verification/premium-tomcat").toAbsolutePath().toString());
        server.setPort(8766);
        server.getConnector().setProperty("address", "127.0.0.1");
        server.addWebapp("/bank", Path.of("target/WebBasedBankingSystem").toAbsolutePath().toString());
        server.start();
        server.getServer().await();
    }
}
