package com.jack.nars.waver.data.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
abstract class ProfileDao {
    @Transaction
    open suspend fun createOrUpdateProfile(pwi: ProfileWithItems) {
        deleteLoopsInProfileFor(pwi.profile.idProfile)
        insertLoopsInProfile(pwi.loops)
        insertProfile(pwi.profile)
    }

    @Transaction
    open suspend fun deleteProfile(idProfile: Long) {
        deleteLoopsInProfileFor(idProfile)
        deleteProfileWithId(idProfile)
    }

    @Transaction
    @Query("SELECT * from Profile")
    abstract fun getProfilesWithItems(): LiveData<List<ProfileWithItems>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertProfile(profile: Profile)

    @Query("DELETE FROM Profile WHERE :idProfile = idProfile")
    protected abstract suspend fun deleteProfileWithId(idProfile: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertLoopsInProfile(loops: List<LoopInProfile>)

    @Query("DELETE FROM LoopInProfile WHERE :idProfile = idProfile")
    protected abstract suspend fun deleteLoopsInProfileFor(idProfile: Long)
}