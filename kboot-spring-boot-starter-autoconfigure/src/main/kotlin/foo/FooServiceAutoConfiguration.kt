package foo

import hello.HelloService
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(HelloService::class)
class FooServiceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun fooService(): FooService {
        return FooServiceImpl()
    }
}
