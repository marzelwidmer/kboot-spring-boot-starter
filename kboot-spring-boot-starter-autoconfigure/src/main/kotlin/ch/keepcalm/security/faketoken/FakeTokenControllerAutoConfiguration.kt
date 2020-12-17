package ch.keepcalm.security.faketoken

import hello.HelloService
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(FakeTokenController::class)
class FakeTokenControllerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun generateToken(): FakeTokenController {
        return FakeTokenController()
    }
}
