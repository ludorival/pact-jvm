package au.com.dius.pact.consumer.model

import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.support.Utils.randomPort
import java.io.File
import java.security.KeyStore

/**
 * Mock Provider configuration for HTTPS
 */
class MockHttpsProviderConfig @JvmOverloads constructor(
  override val hostname: String = LOCALHOST,
  override val port: Int = 0,
  override val pactVersion: PactSpecVersion = PactSpecVersion.V3,
  val keyStore: KeyStore? = null,
  val keyStoreAlias: String = "alias",
  val keystorePassword: String = "changeme",
  val privateKeyPassword: String = "changeme",
  override val mockServerImplementation: MockServerImplementation = MockServerImplementation.KTorServer
) : MockProviderConfig(hostname, port, pactVersion, "https", mockServerImplementation) {

  @Suppress("ComplexMethod")
  override fun mergeWith(config: MockProviderConfig): MockProviderConfig {
    return if (config is MockHttpsProviderConfig) {
      MockHttpsProviderConfig(
        if (hostname.isEmpty() || hostname == LOCALHOST) config.hostname else hostname,
        if (port == 0) config.port else port,
        if (pactVersion == PactSpecVersion.UNSPECIFIED) config.pactVersion else pactVersion,
        keyStore ?: config.keyStore,
        if (keyStoreAlias.isEmpty() || keyStoreAlias == "alias") config.keyStoreAlias else keyStoreAlias,
        if (keystorePassword.isEmpty() || keystorePassword == "changeme")
          config.keystorePassword
        else keystorePassword,
        if (privateKeyPassword.isEmpty() || privateKeyPassword == "changeme")
          config.privateKeyPassword
        else privateKeyPassword,
        mockServerImplementation
      )
    } else {
      MockHttpsProviderConfig(
        if (hostname.isEmpty() || hostname == LOCALHOST) config.hostname else hostname,
        if (port == 0) config.port else port,
        if (pactVersion == PactSpecVersion.UNSPECIFIED) config.pactVersion else pactVersion,
        keyStore,
        keyStoreAlias,
        keystorePassword,
        privateKeyPassword,
        mockServerImplementation
      )
    }
  }

  companion object {
    @JvmStatic
    @JvmOverloads
    fun httpsConfig(
      hostname: String = LOCALHOST,
      port: Int = 0,
      pactVersion: PactSpecVersion = PactSpecVersion.V3,
      implementation: MockServerImplementation = MockServerImplementation.KTorServer
    ): MockHttpsProviderConfig {
      val jksFile = File.createTempFile("PactTest", ".jks")
      val p = if (port == 0) {
        randomPort()
      } else {
        port
      }
      val keystore = io.ktor.network.tls.certificates.generateCertificate(jksFile, "SHA1withRSA",
              "PactTest", "changeit", "changeit", 1024)
      return MockHttpsProviderConfig(hostname, p, pactVersion, keystore, "PactTest", "changeit", "changeit",
        implementation.merge(MockServerImplementation.KTorServer))
    }
  }
}
