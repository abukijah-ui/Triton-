package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TritonRepository
import com.example.model.LiveThinkingState
import com.example.model.ThinkingPhase
import com.example.model.TritonModel
import com.example.ui.components.MarkdownBlock
import com.example.ui.components.parseInlineMarkdown
import com.example.ui.components.parseMarkdownBlocks
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

  @Test
  fun `markdown parser accurately parses tables`() {
    val markdown = """
      | Metric | Triton 3.7 | Claude 3.7 |
      | :--- | :---: | ---: |
      | **Speed** | Instant | Fast |
      | **Reasoning** | Hybrid | Extended |
    """.trimIndent()

    val blocks = parseMarkdownBlocks(markdown)
    val tableBlock = blocks.filterIsInstance<MarkdownBlock.Table>().firstOrNull()
    assertNotNull(tableBlock)
    assertEquals(3, tableBlock!!.headers.size)
    assertEquals("Metric", tableBlock.headers[0])
    assertEquals("Triton 3.7", tableBlock.headers[1])
    assertEquals("Claude 3.7", tableBlock.headers[2])
    assertEquals(2, tableBlock.rows.size)
    assertEquals("**Speed**", tableBlock.rows[0][0])
    assertEquals("Instant", tableBlock.rows[0][1])
  }

  @Test
  fun `markdown parser supports code blocks, lists, quotes, and task items`() {
    val markdown = """
      # Triton Intelligence
      
      > Regal AI system with cognitive depth.
      
      ---
      
      - [ ] Task 1
      - [x] Task 2 Completed
      - Bullet point A
      1. Ordered step 1
      
      ```kotlin
      val x = 42
      ```
    """.trimIndent()

    val blocks = parseMarkdownBlocks(markdown)
    assertTrue(blocks.any { it is MarkdownBlock.Header && it.text == "Triton Intelligence" })
    assertTrue(blocks.any { it is MarkdownBlock.Blockquote })
    assertTrue(blocks.any { it is MarkdownBlock.HorizontalRule })
    assertTrue(blocks.any { it is MarkdownBlock.TaskItem && !it.isChecked })
    assertTrue(blocks.any { it is MarkdownBlock.TaskItem && it.isChecked })
    assertTrue(blocks.any { it is MarkdownBlock.BulletItem })
    assertTrue(blocks.any { it is MarkdownBlock.NumberedItem })
    assertTrue(blocks.any { it is MarkdownBlock.CodeBlock && it.language == "kotlin" })
  }

  @Test
  fun `inline markdown correctly parses bold, code, and links`() {
    val annotated = parseInlineMarkdown("Check **bold** and `code` and [Docs](https://example.com)")
    val text = annotated.text
    assertTrue(text.contains("bold"))
    assertTrue(text.contains("code"))
    assertTrue(text.contains("Docs"))
  }
}
