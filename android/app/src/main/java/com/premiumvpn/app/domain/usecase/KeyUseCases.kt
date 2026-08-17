package com.premiumvpn.app.domain.usecase

import com.premiumvpn.app.data.local.KeyEntity
import com.premiumvpn.app.data.repository.KeyRepository
import com.premiumvpn.app.domain.model.KeyUsageStats
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllKeysUseCase @Inject constructor(
    private val keyRepository: KeyRepository
) {
    operator fun invoke(): Flow<List<KeyEntity>> = keyRepository.getAllKeys()
}

class AddKeyUseCase @Inject constructor(
    private val keyRepository: KeyRepository
) {
    suspend operator fun invoke(ssUrl: String, name: String? = null): Result<KeyEntity> =
        keyRepository.addKey(ssUrl, name)
}

class DeleteKeyUseCase @Inject constructor(
    private val keyRepository: KeyRepository
) {
    suspend operator fun invoke(id: String) = keyRepository.deleteKey(id)
}

class RefreshKeyStatsUseCase @Inject constructor(
    private val keyRepository: KeyRepository
) {
    suspend operator fun invoke(id: String): Result<KeyUsageStats> =
        keyRepository.refreshKeyStats(id)
}
