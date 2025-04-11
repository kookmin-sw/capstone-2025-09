package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto

import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.Credit
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.CreditTransaction
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.ReferenceType
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.TransactionStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.TransactionType
import java.time.OffsetDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreditBalanceDto(
    val userId: Long,
    val balance: Int,
    val updatedAt: String
) {
    companion object {
        fun fromEntity(credit: Credit): CreditBalanceDto {
            return CreditBalanceDto(
                userId = credit.user.id,
                balance = credit.balance,
                updatedAt = credit.updatedAt.toString()
            )
        }
    }
}

@Serializable
data class CreditTransactionDto(
    val id: Long,
    val userId: Long,
    val amount: Int,
    val type: String,
    val referenceId: Long? = null,
    val referenceType: String? = null,
    val description: String? = null,
    val status: String,
    val balanceBefore: Int,
    val balanceAfter: Int? = null,
    val createdAt: String
) {
    companion object {
        fun fromEntity(transaction: CreditTransaction): CreditTransactionDto {
            return CreditTransactionDto(
                id = transaction.id,
                userId = transaction.user.id,
                amount = transaction.amount,
                type = transaction.type.name,
                referenceId = transaction.referenceId,
                referenceType = transaction.referenceType?.name,
                description = transaction.description,
                status = transaction.status.name,
                balanceBefore = transaction.balanceBefore,
                balanceAfter = transaction.balanceAfter,
                createdAt = transaction.createdAt.toString()
            )
        }
    }
}

@Serializable
data class ChargeCreditsRequest(
    val userId: Long,
    val amount: Int,
    val paymentMethod: String? = null,
    val paymentReference: String? = null
)

@Serializable
data class UseCreditsRequest(
    val userId: Long,
    val amount: Int,
    val referenceId: Long? = null,
    val referenceType: ReferenceType? = null,
    val description: String? = null
)

@Serializable
data class ExchangeCreditsRequest(
    val userId: Long,
    val amount: Int,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val accountHolder: String? = null
)

@Serializable
data class TransactionResultDto(
    val transactionId: Long,
    val status: String,
    val newBalance: Int? = null,
    val message: String? = null
)

@Serializable
data class GetTransactionsRequest(
    val userId: Long,
    val type: String? = null,
    val status: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val page: Int = 0,
    val size: Int = 20
) 