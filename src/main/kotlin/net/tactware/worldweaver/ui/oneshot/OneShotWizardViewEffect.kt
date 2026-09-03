package net.tactware.worldweaver.ui.oneshot

internal sealed interface OneShotWizardViewEffect {
    data object Completed : OneShotWizardViewEffect
    data object Dismissed : OneShotWizardViewEffect
}
