package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TritonRepository
import com.example.model.LiveThinkingState
import com.example.model.ThinkingPhase
import com.example.model.TritonModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Triton", appName)
  }

  @Test
  fun `thinking mode defaults and toggle functionality`() {
    val repository = TritonRepository()
    assertTrue(repository.isThinkingEnabled.value)

    repository.toggleThinking()
    assertFalse(repository.isThinkingEnabled.value)

    repository.setThinking(true)
    assertTrue(repository.isThinkingEnabled.value)
  }

  @Test
  fun `thinking phases pipeline definition and state expansion`() {
    val phases = ThinkingPhase.values()
    assertEquals(4, phases.size)
    assertEquals(ThinkingPhase.DECONSTRUCTING, phases[0])
    assertEquals(ThinkingPhase.EXPLORING, phases[1])
    assertEquals(ThinkingPhase.SYNTHESIZING, phases[2])
    assertEquals(ThinkingPhase.VERIFYING, phases[3])

    val state = LiveThinkingState(
      isThinking = true,
      elapsedSeconds = 2.4,
      phase = ThinkingPhase.EXPLORING,
      activeThoughtSummary = "Evaluating architectural tradeoffs...",
      isExpanded = true
    )
    assertTrue(state.isThinking)
    assertTrue(state.isExpanded)
    assertEquals(2.4, state.elapsedSeconds, 0.01)
  }
}
