package ch.keepcalm.security.faketoken

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@RestController
class FakeTokenController() {
    @GetMapping(value = ["/faketoken"])
    fun faketoken() = generateToken(Token())
}

fun main() {
    println("Welcome to JWT token generator....")
    println("subject : [john.doe@foo.bar.ch]")
    val subject = readLine()?.ifBlank { Token().subject }
    println("vorname : [John]")
    val firstName = readLine()?.ifBlank { Token().firstName }
    println("name : [Doe]")
    val name = readLine()?.ifBlank { Token().name }
    println("roles : [keepcalm.user, keepcalm.admin]")
    val roles = readLine()?.ifBlank { Token().roles }
    println("issuer : [Keepcalm Auth]")
    val issuer = readLine()?.ifBlank { Token().issuer }
    println("audience : [Keepcalm]")
    val audience = readLine()?.ifBlank { Token().audience }
    println("secret : [willbereplacedinalaterversiononceRSAcanbeused]")
    val secret = readLine()?.ifBlank { Token().secret }.toString()
    println("userEmail : [joh.doe@foo.bar.ch]")
    val userEmail = readLine()?.ifBlank { Token().userEmail }
    println("language : [de]")
    val language = readLine()?.ifBlank { Token().language }
    println("expiration : [3600000]")
    val expiration = readLine()?.toIntOrNull() ?: Token().expiration

    val token = Token(
        subject = subject,
        firstName = firstName,
        language = language,
        name = name,
        roles = roles,
        issuer = issuer,
        audience = audience,
        secret = secret,
        userEmail = userEmail,
        expiration = expiration
    )

    val generatedToken = generateToken(token)
    println("-----------------")
    println("export DEMO_TOKEN=\"${generatedToken}\" \n")
    println(" http :8080/api/document/1 \"Authorization:Bearer  \$DEMO_TOKEN\" -v \n")
    println("-----------------")
    println("###############################")
    println("export DEMO_TOKEN=\"${generatedToken}\" \n")
    println(
        "\ncurl http://localhost:8080/api/document/1 " +
                "-H \"Authorization:Bearer  \$DEMO_TOKEN\" -v  | python -m json.tool \n"
    )
    println("###############################")
}

fun generateToken(token: Token) =
    Jwts.builder()
        .setId(UUID.randomUUID().toString())
        .setSubject(token.subject)
        .setIssuedAt(Date())
        .setExpiration(
            Date.from(token.expiration.toLong().let {
                LocalDateTime.now().plusSeconds(it).atZone(ZoneId.systemDefault()).toInstant()
            })
        )
        .setIssuer(token.issuer)
        .setAudience(token.audience)
        .addClaims(
            mapOf(
                Pair("language", token.language),
                Pair("name", token.name),
                Pair("firstName", token.firstName),
                Pair("email", token.userEmail),
                Pair("roles", token.roles)
            )
        )
        .signWith(
            SignatureAlgorithm.HS256,
            Base64.getEncoder().encodeToString(token.secret.toByteArray(StandardCharsets.UTF_8))
        ).compact()


data class Token(
    var language: String? = "de",
    var firstName: String? = "John",
    var name: String? = "Doe",
    var subject: String? = "john.doe@foo.bar.ch",
    var roles: String? = "keepcalm.user",
    var issuer: String? = "Keepcalm Auth",
    var audience: String? = "Keepcalm",
    var secret: String = "willbereplacedinalaterversiononceRSAcanbeused",
    var userEmail: String? = "joh.doe@foo.bar.ch",
    var expiration: Int = 3600000
)




































