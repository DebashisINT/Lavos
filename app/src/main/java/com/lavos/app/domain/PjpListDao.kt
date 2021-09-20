package com.lavos.app.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lavos.app.AppConstant

/**
 * Created by Saikat on 02-Jun-20.
 */
@Dao
interface PjpListDao {

    @Insert
    fun insert(vararg pjpList: PjpListEntity)

    @Query("DELETE FROM " + AppConstant.PJP_LIST_TABLE)
    fun deleteAll()

    @Query("SELECT * FROM " + AppConstant.PJP_LIST_TABLE)
    fun getAll(): List<PjpListEntity>
}