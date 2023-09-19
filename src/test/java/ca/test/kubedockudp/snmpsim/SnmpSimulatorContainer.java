package ca.test.kubedockudp.snmpsim;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;


public class SnmpSimulatorContainer extends GenericContainer<SnmpSimulatorContainer> {
    public SnmpSimulatorContainer(DockerImageName image) {
        super(image);
        this.withCreateContainerCmdModifier(cmd -> {
            HostConfig hostConfig = Objects.requireNonNullElseGet(cmd.getHostConfig(), () -> cmd.withHostConfig(new HostConfig()).getHostConfig());
            Ports ports = Objects.requireNonNullElseGet(hostConfig.getPortBindings(), Ports::new);
            ports.bind(ExposedPort.udp(161), Ports.Binding.empty());
            hostConfig.withPortBindings(ports);
            cmd.withExposedPorts(new ArrayList<>(ports.getBindings().keySet()));
        });
        this.waitStrategy = Wait.forLogMessage(".*Listening at UDP/IPv4 endpoint 0.0.0.0:161.*", 1).withStartupTimeout(Duration.ofSeconds(60L));
    }
}

