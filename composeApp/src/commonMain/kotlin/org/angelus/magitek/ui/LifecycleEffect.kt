package org.angelus.magitek.ui

import androidx.compose.runtime.Composable

// commonMain
@Composable
expect fun LifecycleEffect(onPause: () -> Unit, onResume: () -> Unit)