package com.example.myapplication.users


import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.TextDrawable
import com.example.myapplication.document.User
import com.example.myapplication.gson
import kotlinx.android.synthetic.main.user_list_item.view.*


class UserListItemAdapter(
        private val context: Context,
        var mValues: MutableList<User>? = null)
    : RecyclerView.Adapter<UserListItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mValues != null) {
            val user = mValues!![position]
            holder.userFullName.text = user.fullName
            holder.username.text = user.username
            if (user.isOnline) {
                holder.userStatus.text = "ONLINE"
                holder.userStatus.visibility = View.VISIBLE
            } else if (!user.accountEnabled) {
                holder.userStatus.text = "DISABLED"
                holder.userStatus.visibility = View.VISIBLE
            } else {
                holder.userStatus.visibility = View.GONE
            }
            val letters = user.firstName?.first().toString() + user.lastName?.first()
            val drawable = TextDrawable.builder()
                    .beginConfig().textColor(ContextCompat.getColor(context, R.color.colorPrimary)).endConfig()
                    .buildRound(letters.toUpperCase(), ContextCompat.getColor(context, R.color.controlUserIconBack))
            holder.userIcon.setImageDrawable(drawable)
            holder.itemView.setOnClickListener {
                val intent = Intent(context, UserEditActivity::class.java)
                intent.putExtra("USER", gson.toJson(user))
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = mValues?.size ?: 0

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val userFullName: TextView = view.user_full_name
        val username: TextView = view.email
        val userStatus: TextView = view.user_status
        val userIcon: ImageView = view.user_icon
        val viewBackground: RelativeLayout = view.findViewById(R.id.view_background)
        val viewForeground: RelativeLayout = view.findViewById(R.id.view_foreground)
    }
}
