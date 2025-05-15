package kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack

import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class SimpleMultipartFile(
    private val fileName: String,
    private val contentType: String?,
    private val content: ByteArray
) : MultipartFile {
    override fun getName(): String = fileName
    override fun getOriginalFilename(): String? = fileName
    override fun getContentType(): String? = contentType
    override fun isEmpty(): Boolean = content.isEmpty()
    override fun getSize(): Long = content.size.toLong()
    override fun getBytes(): ByteArray = content
    override fun getInputStream(): InputStream = ByteArrayInputStream(content)
    override fun transferTo(dest: File) = dest.writeBytes(content)
} 