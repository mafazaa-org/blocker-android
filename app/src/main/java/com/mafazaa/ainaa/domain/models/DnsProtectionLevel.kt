package com.mafazaa.ainaa.domain.models

enum class DnsProtectionLevel(val primaryDns: String, val secondaryDns: String) {
    HIGH("15.184.147.40", "15.184.182.221"),
    LOW("16.24.111.209", "16.24.202.94"),

    NONE("", "")

}