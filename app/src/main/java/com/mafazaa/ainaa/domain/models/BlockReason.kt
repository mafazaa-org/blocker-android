package com.mafazaa.ainaa.domain.models

sealed class BlockReason {
    abstract fun getName(): String
    data class UsingBlockedApp(val packageName: String ) : BlockReason() {
        override fun getName(): String {
            return "تطبيق محظور"
        }
    }

    data class TryingToDisable(
        val codeName: String,
        val screenAnalysis: ScreenAnalysis
    ) : BlockReason() {
        override fun getName(): String {
            return "محاولة تعطيل التطبيق:  \n$codeName"
        }
    }
    data class BlockedWordDetected(val keyword: String, val sentence: String) : BlockReason() {
        override fun getName(): String {
            return "كلمة محظورة مكتشفة:  \n$keyword"
        }
    }
}