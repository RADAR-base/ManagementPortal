import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile

class KratosContainer {
    private val kratos = GenericContainer<Nothing>("oryd/kratos:v1.0.0")
        .withCommand("serve -c /etc/config/kratos/kratos.yml --dev --watch-courier")
        .waitingFor(Wait.forHttp("/health/ready").forPort(4434).forStatusCode(200))
        .withCopyFileToContainer(MountableFile.forClasspathResource("kratos-config.yaml"), "/etc/config/kratos/kratos.yml")
        .withCopyFileToContainer(MountableFile.forClasspathResource("identity.schema.user.json"), "/etc/config/kratos/identities/identity.schema.user.json")
        .withExposedPorts(4433, 4434) 

    fun start() {
        kratos.start()
    }

    fun getPublicUrl(): String = "http://localhost:${kratos.getMappedPort(4433)}"
    fun getAdminUrl(): String = "http://localhost:${kratos.getMappedPort(4434)}"
}
