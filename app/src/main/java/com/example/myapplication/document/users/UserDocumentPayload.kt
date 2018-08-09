package com.example.myapplication.document.users

import com.example.myapplication.document.User
import java.util.*

class Permission {
    var id: String? = null
    var primaryRole: String? = null
    var approvalRole: String? = null
    var commentRole: String? = null
    var type: String? = null
    var user: User? = null
    var scope: String? = null
    var documentUri: String? = null
    var userId: String? = null
}