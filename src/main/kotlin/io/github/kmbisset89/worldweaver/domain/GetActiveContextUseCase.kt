package io.github.kmbisset89.worldweaver.domain

internal class GetActiveContextUseCase(
    private val activeContextRepository: ActiveContextRepository,
) {
    operator fun invoke(): ActiveContext = activeContextRepository.get()
}
