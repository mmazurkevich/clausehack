package com.example.myapplication.approval

enum class Status {
    PENDING,
    ACCEPTED,
    REJECTED
}

enum class Scope(val text: String) {
    all("all"),
    document("document"),
    paragraph("paragraph")
}

data class ApprovalPendingDto(
        val documentTitle: String,
        val documentUri: String,
        val approval: ApprovalDto,
        val paragraph: ParagraphDto? = null
)

data class ApprovalDto(
        val scope: Scope,
        val id: String,
        val uri: String,
        val status: Status,
        val paragraphUri: String,
        val documentUri: String,
        val comment: String
)

data class ParagraphDto(
        val content: String
)