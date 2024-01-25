package com.lavos.app.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lavos.app.AppConstant

@Entity(tableName = AppConstant.CRM_STAGE_MASTER)
data class StageMasterEntity (
    @PrimaryKey(autoGenerate = true) var sl_no: Int = 0,
    @ColumnInfo var stage_id:Int = 0,
    @ColumnInfo var stage_name:String = "",

    )