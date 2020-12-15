package bar

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(BarService::class)
class BarServiceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun barService(): BarService {
        return BarServiceImpl()
    }
}
