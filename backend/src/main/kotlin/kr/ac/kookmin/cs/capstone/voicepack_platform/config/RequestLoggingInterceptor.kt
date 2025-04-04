package kr.ac.kookmin.cs.capstone.voicepack_platform.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.BufferedReader
import java.io.InputStreamReader
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
        
        // 요청 본문 로깅
        val requestBody = getRequestBody(request)

        logger.info("""
            [Request] $timestamp
            Method: $method
            URI: $fullUri
            Client IP: $clientIp
            User-Agent: $userAgent
            Body: $requestBody
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
        
        // 응답 본문 로깅 (ContentCachingResponseWrapper 사용)
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
    
    private fun getRequestBody(request: HttpServletRequest): String {
        return try {
            if (request.contentType?.contains("multipart/form-data") == true) {
                return "[Multipart Form Data]"
            }
            
            val reader = BufferedReader(InputStreamReader(request.inputStream))
            val body = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                body.append(line)
            }
            request.setAttribute("requestBody", body.toString())
            body.toString()
        } catch (e: Exception) {
            logger.warn("요청 본문을 읽는 중 오류 발생: ${e.message}")
            "[Error reading request body]"
        }
    }
    
    private fun getResponseBody(response: HttpServletResponse): String {
        return try {
            // ContentCachingResponseWrapper를 사용하여 응답 본문 캐싱
            val wrapper = ContentCachingResponseWrapper(response)
            val contentAsString = String(wrapper.contentAsByteArray)
            wrapper.copyBodyToResponse()
            contentAsString
        } catch (e: Exception) {
            logger.warn("응답 본문을 읽는 중 오류 발생: ${e.message}")
            "[Error reading response body]"
        }
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