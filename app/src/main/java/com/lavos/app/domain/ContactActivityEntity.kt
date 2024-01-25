package com.lavos.app.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lavos.app.AppConstant

@Entity(tableName = AppConstant.CONTACT_ACTIVITY)
data class ContactActivityEntity (
    @PrimaryKey(autoGenerate = true) var sl_no: Int = 0,
    @ColumnInfo var shop_id:String = "",
    @ColumnInfo var activity_date:String = "",
    @ColumnInfo var create_date_time:String = "",
    @ColumnInfo var isActivityDone:Boolean = false
)