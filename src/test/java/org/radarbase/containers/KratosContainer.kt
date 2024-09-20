import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile
import java.util.*

class KratosContainer {
    private val kratosImage = "oryd/kratos:v1.0.0"
    private val configPath =
        "/etc/config/kratos/kratos.yml" // Path inside the container where the config file will be mounted

    private val kratos: GenericContainer<*> =
        GenericContainer(kratosImage)
            .withCommand("serve -c $configPath --dev --watch-courier")
            .waitingFor(Wait.forHttp("/health/ready").forPort(4434).forStatusCode(200))
            .withCopyFileToContainer(MountableFile.forClasspathResource("kratos-config.yaml"), configPath)
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("identity.schema.user.json"),
                "/etc/config/kratos/identities/identity.schema.user.json",
            )

    fun start() {
        kratos.portBindings = Arrays.asList("4433:4433", "4434:4434")
        kratos.start()
    }
}
