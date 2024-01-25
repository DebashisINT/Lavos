package com.lavos.app.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lavos.features.viewAllOrder.orderOptimized.CommonProductCatagory

@Dao
interface SchedulerMasterDao {
    @Insert
    fun insert(vararg obj: SchedulerMasterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    abstract fun insertAll(kist: List<SchedulerMasterEntity>)

    @Query("select * from crm_scheduler_master")
    fun getAll(): List<SchedulerMasterEntity>

    @Query("delete from crm_scheduler_master")
    fun deleteAll()

    @Query("select * from crm_scheduler_master where select_date=:select_date and isActivityDone=:isActivityDone")
    fun getSchedulerByDate(select_date:String , isActivityDone:Boolean): List<SchedulerMasterEntity>

    @Query("update crm_scheduler_master set isActivityDone =:isActivityDone where select_contact_id=:select_contact_id \n" +
            "and select_timestamp=:select_timestamp \n" +
            "and save_date_time=:save_date_time")
    fun updateSchedulerSucess(select_contact_id:String,select_timestamp:String,save_date_time:String,isActivityDone:Boolean)

}