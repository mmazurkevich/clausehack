package com.example.myapplication.document


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.ColorGenerator
import com.example.myapplication.R
import com.example.myapplication.TextDrawable
import com.example.myapplication.currentUser


class DocumentCommentListItemAdapter(
        private val context: Context,
        var mValues: MutableList<DocumentComment>? = null)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2
    private val profileColors= mutableMapOf<String, Int>()

    fun addWSItem(wsEventDto: WSEventDto) {
        val eventDto = wsEventDto.eventDto!!
        val documentComment = DocumentComment(eventDto.id, eventDto.createdAt, eventDto.createdBy, eventDto.comment)
        mValues?.add(0, documentComment)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mValues?.size ?: 0

    // Determines the appropriate ViewType according to the sender of the message.
    override fun getItemViewType(position: Int): Int {
        val message = mValues!![position]

        return if (message.createdBy?.username == currentUser!!.username) {
            // If the current user is the sender of the message
            VIEW_TYPE_MESSAGE_SENT
        } else {
            // If some other user sent the message
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View

        return if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageHolder(view)
        }else {
            view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
            SentMessageHolder(view)
        }
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = mValues!![position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageHolder).bind(message)
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageHolder).bind(message)
        }
    }

    private inner class SentMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var messageText: TextView
        internal var timeText: TextView

        init {

            messageText = itemView.findViewById<View>(R.id.text_message_body) as TextView
            timeText = itemView.findViewById<View>(R.id.text_message_time) as TextView
        }

        internal fun bind(documentComment: DocumentComment) {
            messageText.text = Html.fromHtml(documentComment.comment?.content).toString()
                    .replace("\n", "").trim()
            // Format the stored timestamp into a readable String using method.
            timeText.text = documentComment.createdAt?.subSequence(11, 16)
        }
    }

    private inner class ReceivedMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var messageText: TextView
        internal var timeText: TextView
        internal var nameText: TextView
        internal var profileImage: ImageView

        init {
            messageText = itemView.findViewById<View>(R.id.text_message_body) as TextView
            timeText = itemView.findViewById<View>(R.id.text_message_time) as TextView
            nameText = itemView.findViewById<View>(R.id.text_message_name) as TextView
            profileImage = itemView.findViewById<View>(R.id.image_message_profile) as ImageView
        }

        internal fun bind(documentComment: DocumentComment) {
            messageText.text= Html.fromHtml(documentComment.comment?.content).toString()
                    .replace("\n", "").trim()

            // Format the stored timestamp into a readable String using method.
            timeText.text = documentComment.createdAt?.subSequence(11, 16)

            nameText.text = documentComment.createdBy?.fullName
            val letter = documentComment.createdBy?.fullName?.first().toString().toUpperCase()
            var profileColor = profileColors[letter]
            if (profileColor == null) {
                profileColor = ColorGenerator.MATERIAL.randomColor
                profileColors[letter] = profileColor
            }
            val drawable = TextDrawable.builder()
                    .buildRound(letter, profileColor)
            profileImage.setImageDrawable(drawable)
        }
    }

}
