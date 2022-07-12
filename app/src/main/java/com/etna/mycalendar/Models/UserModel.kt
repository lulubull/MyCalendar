package com.etna.mycalendar.Models

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

data class UserModel(val id: String? = "",
                    val nom:String = "",
                    val prenom:String = "",
                    val username:String ="",
                    val email:String ="",
                    val dateDeNaissance:String = "",
                    val numeroVoie: String = "",
                     val typeVoie:String="",
                    val nomVoie:String = "",
                    val codePostal:String = "",
                    val ville:String ="",
                    val telephone:String ="",
                    val availableDates:String = "",
                    val imageURL: String = "",
                     val status:String ="",
                     val search:String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    constructor(): this ("","","","","","","","","","","","","","","")

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nom)
        parcel.writeString(prenom)
        parcel.writeString(username)
        parcel.writeString(email)
        parcel.writeString(dateDeNaissance)
        parcel.writeString(numeroVoie)
        parcel.writeString(typeVoie)
        parcel.writeString(nomVoie)
        parcel.writeString(codePostal)
        parcel.writeString(ville)
        parcel.writeString(telephone)
        parcel.writeString(availableDates)
        parcel.writeString(imageURL)
        parcel.writeString(status)
        parcel.writeString(search)
    }

    companion object CREATOR : Creator<UserModel> {
        override fun createFromParcel(parcel: Parcel): UserModel {
            return UserModel(parcel)
        }

        override fun newArray(size: Int): Array<UserModel?> {
            return arrayOfNulls(size)
        }
    }
}