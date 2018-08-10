package com.example.myapplication.document.users


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
import com.example.myapplication.gson
import kotlinx.android.synthetic.main.permission_user_list_item.view.*


class PermissionUserListItemAdapter(
        private val docUserActivity: DocumentUserActivity,
        private val context: Context,
        var mValues: MutableList<Permission>? = null,
        var isAddMode: Boolean = false)
    : RecyclerView.Adapter<PermissionUserListItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.permission_user_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mValues != null) {
            val permission = mValues!![position]
            holder.userFullName.text = permission.user!!.fullName
            holder.username.text = permission.user!!.email
            val letters = permission.user!!.firstName?.first().toString() + permission.user!!.lastName?.first()
            val drawable = TextDrawable.builder()
                    .beginConfig().textColor(ContextCompat.getColor(context, R.color.colorPrimary)).endConfig()
                    .buildRound(letters.toUpperCase(), ContextCompat.getColor(context, R.color.controlUserIconBack))
            holder.userIcon.setImageDrawable(drawable)
            holder.itemView.setOnClickListener {
                if (isAddMode) {
                    docUserActivity.grantUserPermission(permission)
                } else {
                    val intent = Intent(context, DocumentUserEditActivity::class.java)
                    intent.putExtra("PERMISSION", gson.toJson(permission))
                    docUserActivity.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = mValues?.size ?: 0

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val userFullName: TextView = view.user_full_name
        val username: TextView = view.email
        val userIcon: ImageView = view.user_icon
        val viewBackground: RelativeLayout = view.findViewById(R.id.view_permission_background)
        val viewForeground: RelativeLayout = view.findViewById(R.id.view_permission_foreground)
    }
}
