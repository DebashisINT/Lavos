package com.lavos.app.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lavos.app.AppConstant

@Entity(tableName = AppConstant.SCHEDULE_TEMPLATE)
data class ScheduleTemplateEntity (
    @PrimaryKey(autoGenerate = true) var sl_no: Int = 0,
    @ColumnInfo var template_id:Int = 0,
    @ColumnInfo var template_name:String = ""
)