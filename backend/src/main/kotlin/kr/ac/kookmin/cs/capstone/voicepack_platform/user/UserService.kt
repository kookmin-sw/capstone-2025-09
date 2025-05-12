package kr.ac.kookmin.cs.capstone.voicepack_platform.user

import jakarta.servlet.http.HttpSession
import kr.ac.kookmin.cs.capstone.voicepack_platform.common.util.S3PresignedUrlGenerator
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserLoginRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserSignupRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.dto.UserProfileDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.service.CreditService
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.VoicepackRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.SaleRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.core.sync.RequestBody
import java.util.UUID
import org.springframework.web.multipart.MultipartFile
import org.springframework.beans.factory.annotation.Value

@Service
class UserService(
    private val userRepository: UserRepository,
    private val creditService: CreditService,
    private val voicepackRepository: VoicepackRepository,
    private val voicepackUsageRightRepository: VoicepackUsageRightRepository,
    private val saleRepository: SaleRepository,
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket-name}") private val bucketName: String,
    private val s3PresignedUrlGenerator: S3PresignedUrlGenerator
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun signup(request: UserSignupRequest): Long {
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다")
        }

        val user = User(
            email = request.email,
            password = request.password, // 실제 구현시 암호화 필요
            name = request.name
        )

        // 프로필 이미지가 있는 경우 S3에 업로드
        request.profileImage?.let { file ->
            val objectKey = "profile-images/${UUID.randomUUID()}_${file.originalFilename}"
            val s3Url = uploadFileToS3(objectKey, file)
            user.profileImageUrl = s3Url // S3 URL 저장
        }

        return userRepository.save(user).id
    }

    fun login(request: UserLoginRequest, session: HttpSession): Long {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("가입된 이메일이 존재하지 않습니다.")

        if (user.password != request.password) {
            throw IllegalArgumentException("비밀번호가 틀렸습니다.")
        }

        session.removeAttribute("userId")
        session.setAttribute("userId", user.id)

        return user.id;
    }

    @Transactional // Credit 초기화 과정이 실행되므로 readOnly 사용 불가 (추후 변경 해야될 듯)
    fun getUserProfile(userId: Long): UserProfileDto {
        logger.info("사용자 프로필 조회 요청: userId={}", userId)
        
        val user = userRepository.findById(userId)
            .orElseThrow { 
                logger.warn("사용자 프로필 조회 실패: 사용자를 찾을 수 없음 - userId={}", userId)
                NoSuchElementException("사용자를 찾을 수 없습니다.") 
            }
        
        // 크레딧 정보 조회 (CreditService 필요)
        val creditBalance = creditService.getUserBalance(userId).balance
        // TODO: CreditService에 총 수입 계산 로직 구현 필요 -> SaleRepository 직접 사용으로 변경
        val totalEarnings = saleRepository.sumAmountBySellerId(userId) ?: 0 // SaleRepository 사용
        
        // 보이스팩 통계 조회
        val createdPacks = voicepackRepository.countByAuthorId(userId)
        val boughtPacks = voicepackUsageRightRepository.countPurchasedVoicepacksByUserId(userId)
        
        logger.info("사용자 프로필 조회 성공: userId={}", userId)
        return UserProfileDto(
            id = user.id,
            name = user.name,
            email = user.email,
            profileImageUrl = user.profileImageUrl?.let { s3PresignedUrlGenerator.generatePresignedUrl(it) },
            credit = creditBalance,
            totalEarnings = totalEarnings,
            createdPacks = createdPacks,
            boughtPacks = boughtPacks
        )
    }

    private fun uploadFileToS3(objectKey: String, file: MultipartFile): String {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName) // S3 버킷 이름 사용
                .key(objectKey)
                .build(),
            RequestBody.fromInputStream(file.inputStream, file.size)
        )
        return "https://$bucketName.s3.amazonaws.com/$objectKey"
    }
} 