package ca.test.kubedockudp;

import ca.test.kubedockudp.snmpsim.SnmpSimulatorContainer;
import com.github.dockerjava.api.model.ExposedPort;
import org.junit.jupiter.api.Test;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class SnmpCallTest {

    @Container
    SnmpSimulatorContainer snmpSimulatorContainer = new SnmpSimulatorContainer(DockerImageName.parse("tandrup/snmpsim"));

    public static ResponseEvent<UdpAddress> get(String host, int port, String targetName, String oid) throws IOException {
        try (Snmp snmp = getSnmp()) {
            return snmp.get(getPdu(oid), getTarget(host, port, targetName));
        }
    }

    public static Snmp getSnmp() throws IOException {
        var messageDispatcher = new MessageDispatcherImpl();
        messageDispatcher.addMessageProcessingModel(new MPv2c());
        var snmp = new Snmp(messageDispatcher);
        snmp.addTransportMapping(new DefaultUdpTransportMapping());
        snmp.listen();
        return snmp;
    }

    public static PDU getPdu(String oid) {
        PDU requestPdu = new PDU();
        requestPdu.setRequestID(new Integer32());
        requestPdu.setType(PDU.GET);
        requestPdu.add(new VariableBinding(new OID(oid)));
        return requestPdu;
    }

    public static Target<UdpAddress> getTarget(String host, int port, String targetName) {
        var target = new CommunityTarget<UdpAddress>();
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(new UdpAddress(host + "/" + port));
        target.setCommunity(new OctetString(targetName));
        return target;
    }

    @Test
    void testUdp() throws IOException {
        int port = 0;
        var binding = snmpSimulatorContainer.getContainerInfo().getNetworkSettings().getPorts().getBindings().get(ExposedPort.udp(161));
        if (binding != null && binding.length > 0 && binding[0] != null) {
            port = Integer.parseInt(binding[0].getHostPortSpec());
        }
        var response = get(snmpSimulatorContainer.getHost(), port, "demo", ".1.3.6.1.2.1.1.4.0");
        assertThat(response.getResponse().get(0).getVariable()).hasToString("SNMP Laboratories, info@snmplabs.com");
    }


}
