package com.github.ykrank.alipaybillaccessibility.data

import java.util.*

open class BaseAlipayBill {
    var title: String? = null
    var balance: Float? = null
    var kind: String? = null
    var date: Date? = null
    var id:String? = null
    var counterparty:String? = null
}