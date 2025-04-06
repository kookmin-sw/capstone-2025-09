package kr.ac.kookmin.cs.capstone.voicepack_platform.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class RequestLoggingInterceptor : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val startTime = System.currentTimeMillis()
        request.setAttribute("startTime", startTime)

        val timestamp = LocalDateTime.now().format(dateTimeFormatter)
        val method = request.method
        val uri = request.requestURI
        val queryString = request.queryString
        val fullUri = if (queryString != null) "$uri?$queryString" else uri
        val clientIp = getClientIp(request)
        val userAgent = request.getHeader("User-Agent")
        
        logger.info("""
            [Request] $timestamp
            Method: $method
            URI: $fullUri
            Client IP: $clientIp
            User-Agent: $userAgent
        """.trimIndent())

        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val startTime = request.getAttribute("startTime") as Long
        val endTime = System.currentTimeMillis()
        val processingTime = endTime - startTime
        
        val status = response.status

        val timestamp = LocalDateTime.now().format(dateTimeFormatter)
        val method = request.method
        val uri = request.requestURI
        
        val responseBody = getResponseBody(response)

        logger.info("""
            [Response] $timestamp
            Method: $method
            URI: $uri
            Status: $status
            Processing Time: ${processingTime}ms
            Body: $responseBody
        """.trimIndent())
    }
    
    private fun getResponseBody(response: HttpServletResponse): String {
        if (response is ContentCachingResponseWrapper) {
            val buf = response.contentAsByteArray
            if (buf.isNotEmpty()) {
                return try {
                    String(buf, 0, buf.size, response.characterEncoding?.let { charset(it) } ?: StandardCharsets.UTF_8)
                } catch (e: Exception) {
                    logger.warn("응답 본문을 문자열로 변환 중 오류 발생: ${e.message}")
                    "[Error converting response body to string]"
                }
            }
        }
        return "[Response body not available]"
    }

    private fun getClientIp(request: HttpServletRequest): String {
        var ip = request.getHeader("X-Forwarded-For")
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }
        return ip
    }
} 