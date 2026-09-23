package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.TritonArtifact
import com.example.ui.components.artifact.TritonArtifactPanel

/**
 * Backwards-compatible wrapper delegating to the modular TritonArtifactPanel.
 */
@Composable
fun TritonArtifactViewer(
    artifact: TritonArtifact,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    TritonArtifactPanel(
        artifact = artifact,
        onClose = onClose,
        modifier = modifier
    )
}
