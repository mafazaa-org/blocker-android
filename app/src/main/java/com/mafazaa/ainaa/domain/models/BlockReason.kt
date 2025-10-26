package com.mafazaa.ainaa.domain.models

sealed class BlockReason {
    abstract fun getName(): String
    data class UsingBlockedApp(val packageName: String ) : BlockReason() {
        override fun getName(): String {
            return "تطبيق محظور"
        }
    }

    data class UsingBlockedKeyword(val keyword: String) : BlockReason() {
        override fun getName(): String {
            return "كلمة محظورة: $keyword"
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
}