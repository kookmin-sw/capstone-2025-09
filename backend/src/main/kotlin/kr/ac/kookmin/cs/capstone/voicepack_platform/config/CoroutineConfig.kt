package kr.ac.kookmin.cs.capstone.voicepack_platform.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineConfig {

    @Bean
    fun applicationScope(): CoroutineScope {
        // SupervisorJob: 자식 코루틴 중 하나가 실패해도 다른 코루틴이나 부모 코루틴에 영향을 주지 않음
        // Dispatchers.Default: CPU 집약적인 작업에 최적화된 디스패처 사용 (SQS 메시지 처리는 I/O 작업이지만, CoroutineScope 자체는 여기서 생성)
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}