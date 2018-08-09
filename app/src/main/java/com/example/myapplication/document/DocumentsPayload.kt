package com.example.myapplication.document

import com.example.myapplication.toISOtFormat
import java.time.LocalDateTime
import java.util.*

class Pageable {
    var content: List<Document>? = null
}

class Document {
    var title: String? = null
    var isPublished: Boolean = false
    var publicationDate: Date? = null
    var uri: String? = null
    var version: String? = null
    var versionOrder: Long? = null
    var template: Boolean = false
    var firstRevisionCreatedDate: Date? = null
    var lastModifiedDate: Date? = null
    var hasPermission = true
    var isArchived: Boolean? = false
}


data class DocumentComment(val id: String?, val createdAt: String?, val createdBy: User?, val comment: Comment?)

data class Comment(val id: String?, val content: String?)

data class WSEventDto(val type: String?, val eventDto: EventDto?, val mutationType: String?,
                      val documentId: String?)

data class EventDto(val type: String?, val id: String?, val action: String?, val scope: String?,
                    val createdAt: String?, val createdBy: User?, val version: Int?, val paragraphId: String?,
                    val comment: Comment?)


data class DocumentCommentDto(val id: String?, val paragraphId: Any? = null, val documentId: String,
                              val content: String, val identities: List<Any>? = null, val type: String = "SIMPLE",
                              val version: Number?, val mentions: Any? = null, val createdDate: String = Date().toISOtFormat(),
                              val createdBy: User, val isRead:Boolean = false, val paragraphComment:Boolean = false,
                              val documentComment: Boolean = true)

data class DocumentAmazonUri(val amazonTemporaryUrl: String)

open class Category {
    lateinit var id: String
    lateinit var type: String
    var subCategories = mutableListOf<Category>()
    var isArchive = false
    var isPublished = true
}

class NamedCategory: Category(){
    lateinit var name: String
}

class DocumentCategory: Category(){
    lateinit var document: DocumentDto
}

class DocumentDto {
    lateinit var documentId: String
    var isPublished: Boolean = false
    lateinit var title: String
    var template: Boolean = false
}

data class User(val id: String?,
                val username: String?,
                val firstName: String?,
                val lastName: String?,
                val fullName: String?,
                val email: String?,
                val authorities: List<Authority>?,
                val isOnline: Boolean = false,
                val accountEnabled: Boolean = false)

data class Authority(val id: String?, val authority: String?, val description: String, val name: String?)
