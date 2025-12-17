package com.mafazaa.ainaa.domain.models

enum class DnsProtectionLevel(val primaryDns: String, val secondaryDns: String) {
    HIGH("185.228.168.168", "185.228.169.168"),
    LOW("16.24.111.209", "16.24.202.94"),

    NONE("", "")

}
