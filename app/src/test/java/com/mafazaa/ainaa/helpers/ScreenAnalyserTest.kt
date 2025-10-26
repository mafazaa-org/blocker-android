package com.mafazaa.ainaa.helpers

import com.mafazaa.ainaa.domain.models.ScreenNode
import org.junit.Test
import org.junit.Assert.*

class ScreenAnalyserTest {

    @Test
    fun `containsBlockedKeyword should return null when no keywords blocked`() {
        val screenNode = ScreenNode(
            cls = "TextView",
            text = "Hello World",
            id = "text1",
            desc = null,
            children = emptyList()
        )
        val blockedKeywords = emptySet<String>()
        
        val result = ScreenAnalyser.containsBlockedKeyword(screenNode, blockedKeywords)
        
        assertNull(result)
    }

    @Test
    fun `containsBlockedKeyword should find keyword in text field`() {
        val screenNode = ScreenNode(
            cls = "TextView",
            text = "This contains badword in text",
            id = "text1",
            desc = null,
            children = emptyList()
        )
        val blockedKeywords = setOf("badword", "another")
        
        val result = ScreenAnalyser.containsBlockedKeyword(screenNode, blockedKeywords)
        
        assertEquals("badword", result)
    }

    @Test
    fun `containsBlockedKeyword should find keyword in description field`() {
        val screenNode = ScreenNode(
            cls = "TextView",
            text = "Clean text",
            id = "text1",
            desc = "Description with blocked content",
            children = emptyList()
        )
        val blockedKeywords = setOf("blocked")
        
        val result = ScreenAnalyser.containsBlockedKeyword(screenNode, blockedKeywords)
        
        assertEquals("blocked", result)
    }

    @Test
    fun `containsBlockedKeyword should be case insensitive`() {
        val screenNode = ScreenNode(
            cls = "TextView",
            text = "This has BADWORD",
            id = "text1",
            desc = null,
            children = emptyList()
        )
        val blockedKeywords = setOf("badword")
        
        val result = ScreenAnalyser.containsBlockedKeyword(screenNode, blockedKeywords)
        
        assertEquals("badword", result)
    }

    @Test
    fun `containsBlockedKeyword should search in nested children`() {
        val childNode = ScreenNode(
            cls = "TextView",
            text = "Child with badword",
            id = "child1",
            desc = null,
            children = emptyList()
        )
        val parentNode = ScreenNode(
            cls = "LinearLayout",
            text = null,
            id = "parent",
            desc = null,
            children = listOf(childNode)
        )
        val blockedKeywords = setOf("badword")
        
        val result = ScreenAnalyser.containsBlockedKeyword(parentNode, blockedKeywords)
        
        assertEquals("badword", result)
    }

    @Test
    fun `containsBlockedKeyword should return null when keyword not found`() {
        val screenNode = ScreenNode(
            cls = "TextView",
            text = "Clean content",
            id = "text1",
            desc = "Clean description",
            children = emptyList()
        )
        val blockedKeywords = setOf("badword", "inappropriate")
        
        val result = ScreenAnalyser.containsBlockedKeyword(screenNode, blockedKeywords)
        
        assertNull(result)
    }

    @Test
    fun `containsBlockedKeyword should handle null text and description`() {
        val screenNode = ScreenNode(
            cls = "View",
            text = null,
            id = "view1",
            desc = null,
            children = emptyList()
        )
        val blockedKeywords = setOf("badword")
        
        val result = ScreenAnalyser.containsBlockedKeyword(screenNode, blockedKeywords)
        
        assertNull(result)
    }
}
