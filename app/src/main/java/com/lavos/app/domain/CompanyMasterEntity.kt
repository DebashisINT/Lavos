package com.lavos.app.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lavos.app.AppConstant

@Entity(tableName = AppConstant.COMPANY_MASTER)
data class CompanyMasterEntity (
    @PrimaryKey(autoGenerate = true) var sl_no: Int = 0,
    @ColumnInfo var company_id:Int = 0,
    @ColumnInfo var company_name:String = "",
    @ColumnInfo var isUploaded:Boolean = true

    )