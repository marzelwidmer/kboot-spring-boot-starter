package hello

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(HelloService::class)
class HelloServiceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun helloService(): HelloService {
        return ConsoleHelloService()
    }
}
