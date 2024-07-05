
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile
import java.util.*

class KratosContainer {
    private val KRATOS_IMAGE = "oryd/kratos:v1.0.0"
    private val CONFIG_PATH = "/etc/config/kratos/kratos.yml" // Path inside the container where the config file will be mounted

    private val kratos: GenericContainer<*> = GenericContainer(KRATOS_IMAGE)
        .withCommand("serve -c $CONFIG_PATH --dev --watch-courier")
        .waitingFor(Wait.forHttp("/health/ready").forPort(4434).forStatusCode(200))
        .withCopyFileToContainer(MountableFile.forClasspathResource("kratos-config.yaml"), CONFIG_PATH)
        .withCopyFileToContainer(MountableFile.forClasspathResource("identity.schema.user.json"), "/etc/config/kratos/identities/identity.schema.user.json")

    fun start() {
        kratos.setPortBindings(Arrays.asList("4433:4433", "4434:4434"))
        kratos.start()
    }
}
