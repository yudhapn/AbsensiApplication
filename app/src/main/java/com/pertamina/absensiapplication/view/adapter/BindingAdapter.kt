package com.pertamina.absensiapplication.view.adapter

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.model.StatusPermit
import com.pertamina.absensiapplication.model.User

@BindingAdapter("setName")
fun capitalizeWords(view: TextView, name: String) {
    view.text = name.split(" ").joinToString(" ") { it.capitalize() }.trim()
}

@BindingAdapter("statusUser")
fun setStatusUser(view: TextView, isLeave: Boolean) {
    if (isLeave) {
        view.setBackgroundResource(R.drawable.status_outline_color_red)
        view.text = "Sakit / Cuti / Dinas"
    } else {
        view.setBackgroundResource(R.drawable.status_outline_color_green)
        view.text = "Aktif"
    }
}

@BindingAdapter("typeUser")
fun setTypeUser(view: TextView, organic: Boolean) {
    if (organic) {
        view.setBackgroundResource(R.drawable.status_outline_color_blue)
        view.text = "Organik"
    } else {
        view.setBackgroundResource(R.drawable.status_outline_color_orange)
        view.text = "TKJP"
    }
}

@BindingAdapter("isPjs")
fun setPjs(view: TextView, organic: Boolean) {
    if (organic) {
        view.visibility = View.GONE
        view.text = "Organik"
    }
}

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    Glide.with(view.context).load(url).into(view)
}

@BindingAdapter("setVisibility")
fun setVisibility(view: ConstraintLayout, user: User) {
    if (user.senior.isNotEmpty() && user.operationHead.isNotEmpty() && user.standIn.size == 0) {
        view.visibility = View.GONE
    } else {
        view.visibility = View.VISIBLE
    }
}

@BindingAdapter("setCircleIcon")
fun setCircleIcon(view: ImageView, status: StatusPermit) {
    val isRequest = status.request
    val isComplete = status.complete
    val isCancle = status.cancel
    val isNegotiate = status.negotiate
    val confirmBySenior = status.confirmBySenior
    val confirmByOH = status.confirmByOH
    if (isComplete && !isCancle) {
        if (!confirmBySenior) {
            //reject by senior
            view.setBackgroundResource(R.drawable.ic_circle_red)
        } else if (confirmBySenior && !confirmByOH) {
            //reject by OH
            view.setBackgroundResource(R.drawable.ic_circle_red)
        } else if (confirmBySenior && confirmByOH) {
            view.setBackgroundResource(R.drawable.ic_circle_green)
        }
    } else if (isComplete && isCancle) {
        view.setBackgroundResource(R.drawable.ic_circle_red)
    } else if (isRequest && !isNegotiate) {
        view.setBackgroundResource(R.drawable.ic_circle_yellow)
    } else if (isRequest && isNegotiate) {
        view.setBackgroundResource(R.drawable.ic_circle_blue)
    }
}

@BindingAdapter("setStatus")
fun setStatus(view: TextView, status: StatusPermit) {
    val isRequest = status.request
    val isComplete = status.complete
    val isCancle = status.cancel
    val isNegotiate = status.negotiate
    val confirmBySenior = status.confirmBySenior
    val confirmByOH = status.confirmByOH
    if (isComplete && !isCancle) {
        if (!confirmBySenior) {
            //reject by senior
            view.text = "rejected by senior"
        } else if (confirmBySenior && !confirmByOH) {
            //reject by OH
            view.text = "rejected by OH"
        } else if (confirmBySenior && confirmByOH) {
            view.text = "complete"
        }
    } else if (isComplete && isCancle) {
        view.text = "cancel"
    } else if (isRequest && !isNegotiate) {
        if (!confirmBySenior) {
            //reject by senior
            view.text = "request to senior"
        } else if (confirmBySenior && !confirmByOH) {
            //reject by OH
            view.text = "request to OH"
        }
    } else if (isRequest && isNegotiate) {
        view.text = "negotiate"
    }
}

@BindingAdapter("setTitle")
fun setTitle(view: TextView, title: String) {
    view.text = if (title.length > 42) title.substring(0, 25) + "..." else title
}
