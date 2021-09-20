package com.lavos.base.presentation


import android.annotation.TargetApi
import android.app.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import androidx.lifecycle.LifecycleRegistry
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.lavos.CustomConstants
import com.lavos.CustomStatic
import com.elvishew.xlog.XLog
import com.lavos.R
import com.lavos.app.*
import com.lavos.app.domain.GpsStatusEntity
import com.lavos.app.utils.AppUtils
import com.lavos.app.utils.FTStorageUtils
import com.lavos.app.utils.FTStorageUtils.isMyServiceRunning
import com.lavos.app.utils.PermissionUtils
import com.lavos.app.utils.Toaster
import com.lavos.base.BaseResponse
import com.lavos.features.alarm.presetation.FloatingWidgetService
import com.lavos.features.commondialogsinglebtn.CommonDialogSingleBtn
import com.lavos.features.commondialogsinglebtn.OnDialogClickListener
import com.lavos.features.dashboard.presentation.ToastBroadcastReceiver
import com.lavos.features.geofence.GeofenceService
import com.lavos.features.login.presentation.LoginActivity
import com.lavos.features.logout.presentation.api.LogoutRepositoryProvider
import com.lavos.features.orderhistory.api.LocationUpdateRepositoryProviders
import com.lavos.features.orderhistory.model.LocationData
import com.lavos.features.orderhistory.model.LocationUpdateRequest
import com.lavos.features.performance.api.UpdateGpsStatusRepoProvider
import com.lavos.features.performance.model.UpdateGpsInputParamsModel
import com.lavos.MonitorService
import com.lavos.app.domain.AddShopDBModelEntity
import com.lavos.app.domain.AssignToDDEntity
import com.lavos.features.DecimalDigitsInputFilter
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.dashboard.presentation.api.dayStartEnd.DayStartEndRepoProvider
import com.lavos.features.dashboard.presentation.model.DaystartDayendRequest
import com.lavos.features.location.*
import com.lavos.widgets.AppCustomTextView
import com.google.android.gms.location.FusedLocationProviderApi
import com.google.android.gms.location.LocationRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import net.alexandroid.gps.GpsStatusDetector
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*


/**
 * Created by Pratishruti on 26-10-2017.
 */

open class BaseActivity : AppCompatActivity(), GpsStatusDetector.GpsStatusDetectorCallBack {

    private val mRegistry = LifecycleRegistry(this)
    private lateinit var geofenceService: Intent
    var progressDialog: CustomProgressDialog? = null
    private var filter: IntentFilter? = null
    private var permissionUtils: PermissionUtils? = null
    private var mGpsStatusDetector: GpsStatusDetector? = null
    private var i = 0
    private var autoLogoutDialog: CommonDialogSingleBtn? = null
    private var autoTimeDialog: CommonDialogSingleBtn? = null


    private fun getProgressInstance(): CustomProgressDialog {
        if (progressDialog == null)
            progressDialog = CustomProgressDialog(this)
        return progressDialog!!
    }


    companion object {
        @JvmStatic
        val compositeDisposable: CompositeDisposable = CompositeDisposable()
        var isApiInitiated = false
        var isShopActivityUpdating = false
        var isMeetingUpdating = false
    }


    /*   override fun getLifecycle(): LifecycleRegistry{
           return mRegistry
       }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isApiInitiated = false

        /*filter = IntentFilter()
        filter?.addAction(AppUtils.gpsDisabledAction)
        filter?.addAction(AppUtils.gpsEnabledAction)*/

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionUtils = PermissionUtils(this, object : PermissionUtils.OnPermissionListener {
                override fun onPermissionGranted() {
                    checkGPSAvailability()
                }

                override fun onPermissionNotGranted() {
                    //Toast.makeText(this@BaseActivity, "Please accept permission from settings", Toast.LENGTH_LONG).show()
                }

            }, arrayOf<String>(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        } else
            checkGPSAvailability()*/
    }


    override fun onResume() {
        super.onResume()

        //registerReceiver(broadcastReceiver, filter)

        if (android.provider.Settings.Global.getInt(contentResolver, android.provider.Settings.Global.AUTO_TIME, 0) == 0) {
            autoTime()
            return
        }

        //checkGPSAvailability()

        if (Pref.user_id.isNullOrEmpty())
            return

        XLog.e("BaseActivity: Login Date====> " + Pref.login_date)
        XLog.e("BaseActivity: Current Date====> " + AppUtils.getCurrentDateChanged())

        if (Pref.user_id!!.isNotEmpty() && AppUtils.getLongTimeStampFromDate2(Pref.login_date!!) != AppUtils.getLongTimeStampFromDate2(AppUtils.getCurrentDateChanged())) {
            Pref.isAutoLogout = true
        } /*else
            Pref.isAutoLogout = false*/

        //Pref.isAutoLogout=true
        if (Pref.isAutoLogout) {
            performLogout()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        } else {

            if (!TextUtils.isEmpty(Pref.approvedOutTime)) {

                val currentTimeInLong = AppUtils.convertTimeWithMeredianToLong(AppUtils.getCurrentTimeWithMeredian())
                val approvedOutTimeInLong = AppUtils.convertTimeWithMeredianToLong(Pref.approvedOutTime)

                if (currentTimeInLong >= approvedOutTimeInLong) {
                    showForceLogoutPopup()
                }
            }
        }
    }

    open fun showForceLogoutPopup() {
    }

    /*val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AppUtils.gpsDisabledAction) {

            }
        }
    }*/

    open fun takeActionOnGeofence() {

        if (Pref.IsLeavePressed == true && Pref.IsLeaveGPSTrack == false) {
            return
        }

        if (Pref.user_id == null)
            return
        if (Pref.user_id!!.isNotEmpty()) {
            Pref.isGeoFenceAdded = true
            geofenceService = Intent(this, GeofenceService::class.java)
            startService(geofenceService)
        } else {
            if (Pref.isGeoFenceAdded) {
                Pref.isGeoFenceAdded = false
                geofenceService = Intent(this, GeofenceService::class.java)
                stopService(geofenceService)
            }
        }
    }


    private fun autoTime() {

        if (autoTimeDialog == null || !autoTimeDialog?.isVisible!!) {
            autoTimeDialog = CommonDialogSingleBtn.getInstance(getString(R.string.date_n_time), getString(R.string.auto_time_zone), getString(R.string.cancel), object : OnDialogClickListener {
                override fun onOkClick() {
                    startActivityForResult(Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 0)
                }
            })//.show(supportFragmentManager, "CommonDialogSingleBtn")
            autoTimeDialog?.show(supportFragmentManager, "CommonDialogSingleBtn")
        }
    }

    private fun performLogout() {

        if (autoLogoutDialog == null) {
            autoLogoutDialog = CommonDialogSingleBtn.getInstance(AppUtils.hiFirstNameText(), "Final logout for the date ${AppUtils.convertLoginTimeToAutoLogoutTimeFormat(Pref.login_date!!)} is pending. Click Ok to complete final logout.", getString(R.string.ok), object : OnDialogClickListener {

                override fun onOkClick() {

                    val list = AppDatabase.getDBInstance()!!.gpsStatusDao().getDataSyncStateWise(false)


                    if (AppUtils.isOnline(this@BaseActivity)) {

                        if (list != null && list.isNotEmpty()) {
                            i = 0
                            callUpdateGpsStatusApi(list)
                        } else {
                            checkToCallLocationSync()
                        }
                    } else {
                        Toaster.msgShort(this@BaseActivity, getString(R.string.no_internet))
                        performLogout()
                    }
                }
            })//.show(supportFragmentManager, "CommonDialogSingleBtn")
            //}

            //if (autoLogoutDialog?.dialog != null && !autoLogoutDialog?.dialog?.isShowing!!)
            autoLogoutDialog?.show(supportFragmentManager, "CommonDialogSingleBtn")
        } else {
            if (autoLogoutDialog?.dialog != null && !autoLogoutDialog?.dialog?.isShowing!!)
                autoLogoutDialog?.show(supportFragmentManager, "CommonDialogSingleBtn")
            else {
                if (autoLogoutDialog?.dialog == null)
                    autoLogoutDialog?.show(supportFragmentManager, "CommonDialogSingleBtn")
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun checkToCallLocationSync() {
        val locationList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationNotUploaded(false)
        if (locationList != null && locationList.isNotEmpty())
            syncLocationActivity(locationList)
        else
            initiateLogoutApi()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun syncLocationActivity(list: List<UserLocationDataEntity>) {

        XLog.d("syncLocationActivity Logout : ENTER")


        if (Pref.user_id.isNullOrEmpty())
            return

        if (AppUtils.isLocationActivityUpdating)
            return

        AppUtils.isLocationActivityUpdating = true

        val locationUpdateReq = LocationUpdateRequest()
        locationUpdateReq.user_id = Pref.user_id
        locationUpdateReq.session_token = Pref.session_token

        val locationList: MutableList<LocationData> = ArrayList()
        val locationListAllId: MutableList<LocationData> = ArrayList()
        var distanceCovered: Double = 0.0
        var timeStamp = 0L

        val allLocationList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADay(AppUtils.convertFromRightToReverseFormat(Pref.login_date!!)).toMutableList()
        val apiLocationList: MutableList<UserLocationDataEntity> = ArrayList()

        val syncList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADayNotSyn(AppUtils.convertFromRightToReverseFormat(Pref.login_date!!), true)

//        for (i in 0 until list.size) {
//            if (list[i].latitude == null || list[i].longitude == null)
//                continue
//            val locationData = LocationData()
//
//
//            /*locationData.locationId = list[i].locationId.toString()
//            locationData.date = list[i].updateDateTime
//            locationData.distance_covered = list[i].distance
//            locationData.latitude = list[i].latitude
//            locationData.longitude = list[i].longitude
//            locationData.location_name = list[i].locationName
//            locationData.shops_covered = list[i].shops
//            locationData.last_update_time = list[i].time + " " + list[i].meridiem*/
//
//            if (syncList == null || syncList.isEmpty()) {
//                if (i == 0) {
//                    locationData.locationId = list[i].locationId.toString()
//                    locationData.date = list[i].updateDateTime
//                    locationData.distance_covered = list[i].distance
//                    locationData.latitude = list[i].latitude
//                    locationData.longitude = list[i].longitude
//                    locationData.location_name = list[i].locationName
//                    locationData.shops_covered = list[i].shops
//                    locationData.last_update_time = list[i].time + " " + list[i].meridiem
//                    locationList.add(locationData)
//                }
//            }
//
//            distanceCovered += list[i].distance.toDouble()
//
//            if (i != 0 && i % 5 == 0) {
//                locationData.locationId = list[i].locationId.toString()
//                locationData.date = list[i].updateDateTime
//
//                locationData.distance_covered = distanceCovered.toString()
//
//                locationData.latitude = list[i].latitude
//                locationData.longitude = list[i].longitude
//                locationData.location_name = list[i].locationName
//                locationData.shops_covered = list[i].shops
//                locationData.last_update_time = list[i].time + " " + list[i].meridiem
//                locationList.add(locationData)
//
//                distanceCovered = 0.0
//            }
//
//            /*if (TextUtils.isEmpty(list[i].unique_id)) {
//                //list[i].unique_id = m.toString()
//                AppDatabase.getDBInstance()!!.userLocationDataDao().updateUniqueId(m.toString(), list[i].locationId)
//            }*/
//
//            val locationDataAll = LocationData()
//            locationDataAll.locationId = list[i].locationId.toString()
//            locationListAllId.add(locationDataAll)
//        }

        var fiveMinsRowGap = 5

        if (Pref.locationTrackInterval == "30")
            fiveMinsRowGap = 10

        for (i in 0 until allLocationList.size) {
            if (allLocationList[i].latitude == null || allLocationList[i].longitude == null)
                continue

            //apiLocationList.add(allLocationList[i])

            if (i == 0) {
                apiLocationList.add(allLocationList[i])
            }

            distanceCovered += allLocationList[i].distance.toDouble()

            if (!TextUtils.isEmpty(allLocationList[i].home_duration)) {
                XLog.e("Home Duration (Location Fuzed Service)=================> ${allLocationList[i].home_duration}")
                XLog.e("Time (Location Fuzed Service)=================> ${allLocationList[i].time}")
                val arr = allLocationList[i].home_duration?.split(":".toRegex())?.toTypedArray()
                timeStamp += arr?.get(2)?.toInt()?.toLong()!!
                timeStamp += 60 * arr[1].toInt().toLong()
                timeStamp += 3600 * arr[0].toInt().toLong()
            }

            if (i != 0) {
                try {

                    val timeStamp_ = allLocationList[i].timestamp.toLong()

                    if (i % fiveMinsRowGap == 0) {
                        allLocationList[i].distance = distanceCovered.toString()

                        if (timeStamp != 0L) {
                            val hh = timeStamp / 3600
                            timeStamp %= 3600
                            val mm = timeStamp / 60
                            timeStamp %= 60
                            val ss = timeStamp
                            allLocationList[i].home_duration = AppUtils.format(hh) + ":" + AppUtils.format(mm) + ":" + AppUtils.format(ss)
                        }

                        apiLocationList.add(allLocationList[i])
                        distanceCovered = 0.0
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                    allLocationList[i].distance = distanceCovered.toString()

                    if (timeStamp != 0L) {
                        val hh = timeStamp / 3600
                        timeStamp %= 3600
                        val mm = timeStamp / 60
                        timeStamp %= 60
                        val ss = timeStamp
                        allLocationList[i].home_duration = AppUtils.format(hh) + ":" + AppUtils.format(mm) + ":" + AppUtils.format(ss)
                    }
                    apiLocationList.add(allLocationList[i])
                    distanceCovered = 0.0
                }
            }
        }

        for (i in apiLocationList.indices) {
            if (!apiLocationList[i].isUploaded) {

                XLog.e("Final Home Duration (Location Fuzed Service)=================> ${apiLocationList[i].home_duration}")
                XLog.e("Time (Location Fuzed Service)=================> ${apiLocationList[i].time} ${apiLocationList[i].meridiem}")


                val locationData = LocationData()

                locationData.locationId = apiLocationList[i].locationId.toString()
                locationData.date = apiLocationList[i].updateDateTime
                locationData.distance_covered = apiLocationList[i].distance
                locationData.latitude = apiLocationList[i].latitude
                locationData.longitude = apiLocationList[i].longitude
                locationData.location_name = apiLocationList[i].locationName
                locationData.shops_covered = apiLocationList[i].shops
                locationData.last_update_time = apiLocationList[i].time + " " + apiLocationList[i].meridiem
                locationData.meeting_attended = apiLocationList[i].meeting
                locationData.network_status = apiLocationList[i].network_status
                locationData.battery_percentage = apiLocationList[i].battery_percentage
                locationData.home_duration = apiLocationList[i].home_duration
                locationList.add(locationData)


                val locationDataAll = LocationData()
                locationDataAll.locationId = apiLocationList[i].locationId.toString()
                locationListAllId.add(locationDataAll)
            }
        }

        if (locationList.size > 0) {

            locationUpdateReq.location_details = locationList
            val repository = LocationUpdateRepositoryProviders.provideLocationUpdareRepository()

            XLog.d("syncLocationActivity Logout : REQUEST")
            getProgressInstance().showDialogForLoading(this)

            BaseActivity.compositeDisposable.add(
                    repository.sendLocationUpdate(locationUpdateReq)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
//                        .timeout(60 * 1, TimeUnit.SECONDS)
                            .subscribe({ result ->
                                val updateShopActivityResponse = result as BaseResponse

                                XLog.d("syncLocationActivity Logout : RESPONSE : " + updateShopActivityResponse.status + ":" + updateShopActivityResponse.message)

                                if (updateShopActivityResponse.status == NetworkConstant.SUCCESS) {

                                    doAsync {

                                        for (i in 0 until locationListAllId/*locationList*/.size) {

                                            //AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploaded(true, locationList[i].locationId.toInt())

                                            if (syncList != null && syncList.isNotEmpty()) {

                                                if (i == 0)
                                                    AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploadedFor5Items(true, syncList[syncList.size - 1].locationId.toInt(), locationListAllId[i].locationId.toInt())
                                                else
                                                    AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploadedFor5Items(true, locationListAllId[i - 1].locationId.toInt(), locationListAllId[i].locationId.toInt())

                                            } else {
                                                if (i == 0)
                                                    AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploaded(true, locationListAllId[i].locationId.toInt())
                                                else
                                                    AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploadedFor5Items(true, locationListAllId[i - 1].locationId.toInt(), locationListAllId[i].locationId.toInt())
                                            }
                                        }

                                        uiThread {
                                            AppUtils.isLocationActivityUpdating = false
                                            getProgressInstance().dismissDialog()
                                            initiateLogoutApi()
                                        }
                                    }
                                } else {
                                    AppUtils.isLocationActivityUpdating = false
                                    getProgressInstance().dismissDialog()
                                    initiateLogoutApi()
                                }

                            }, { error ->
                                AppUtils.isLocationActivityUpdating = false
                                getProgressInstance().dismissDialog()
                                initiateLogoutApi()

                                if (error == null) {
                                    XLog.d("syncLocationActivity Logout : ERROR : " + "UNEXPECTED ERROR IN LOCATION ACTIVITY API")
                                } else {
                                    XLog.d("syncLocationActivity Logout : ERROR : " + error.localizedMessage)
                                    error.printStackTrace()
                                }
                            })
            )
        } else {
            XLog.e("=======locationList is empty (Auto Logout)=========")
            AppUtils.isLocationActivityUpdating = false
            initiateLogoutApi()
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initiateLogoutApi() {
        getProgressInstance().showDialogForLoading(this@BaseActivity)
        Pref.logout_time = "11:59 PM"
        if(Pref.DayStartMarked && Pref.IsShowDayStart){
            singleLocationEnd()
        }else{
            calllogoutApi(Pref.user_id!!, Pref.session_token!!)
        }
    }

    private fun singleLocationEnd() {

        SingleShotLocationProvider.requestSingleUpdate(this,
                object : SingleShotLocationProvider.LocationCallback {
                    override fun onStatusChanged(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderEnabled(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderDisabled(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onNewLocationAvailable(location: Location) {
                        if(location!=null)
                            endDay(location)
                        else{
                           var lloc:Location = Location("")
                            lloc.latitude=Pref.current_latitude.toDouble()
                            lloc.longitude=Pref.current_longitude.toDouble()
                            endDay(lloc)
                        }
                    }

                })

    }

    fun endDay(location: Location) {
        try{
            var saleValue: String = ""
            var dayst: DaystartDayendRequest = DaystartDayendRequest()
            dayst.user_id = Pref.user_id
            dayst.session_token = Pref.session_token
            //dayst.date = AppUtils.getCurrentDateTime()
            dayst.date = AppUtils.getCurrentDateTime12(Pref.login_date!!)
            dayst.location_name = LocationWizard.getNewLocationName(this, location.latitude, location.longitude)
            dayst.latitude = location.latitude.toString()
            dayst.longitude = location.longitude.toString()
            dayst.shop_type = ""
            dayst.shop_id = ""
            dayst.isStart = "0"
            dayst.isEnd = "1"
            dayst.sale_Value = "0.0"
            dayst.remarks = "No Day End Value for auto logout"
            val repository = DayStartEndRepoProvider.dayStartRepositiry()
            BaseActivity.compositeDisposable.add(
                    repository.dayStart(dayst)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                XLog.d("DashboardFragment DayEnd : RESPONSE " + result.status)
                                val response = result as BaseResponse
                                if (response.status == NetworkConstant.SUCCESS) {
                                    calllogoutApi(Pref.user_id!!, Pref.session_token!!)
                                }
                            }, { error ->
                                if (error == null) {
                                    calllogoutApi(Pref.user_id!!, Pref.session_token!!)
                                    XLog.d("DashboardFragment DayEnd : ERROR " + "UNEXPECTED ERROR IN DayStart API")
                                } else {
                                    calllogoutApi(Pref.user_id!!, Pref.session_token!!)
                                    XLog.d("DashboardFragment DayEnd : ERROR " + error.localizedMessage)
                                    error.printStackTrace()
                                }
                            })
            )
        }
        catch (ex:java.lang.Exception){
            ex.printStackTrace()
            calllogoutApi(Pref.user_id!!, Pref.session_token!!)
        }


    }




private fun callUpdateGpsStatusApi(list: List<GpsStatusEntity>) {

    val updateGps = UpdateGpsInputParamsModel()
    updateGps.date = list[i].date
    updateGps.gps_id = list[i].gps_id
    updateGps.gps_off_time = list[i].gps_off_time
    updateGps.gps_on_time = list[i].gps_on_time
    updateGps.user_id = Pref.user_id
    updateGps.session_token = Pref.session_token
    updateGps.duration = AppUtils.getTimeInHourMinuteFormat(list[i].duration?.toLong()!!)

    getProgressInstance().showDialogForLoading(this@BaseActivity)

    val repository = UpdateGpsStatusRepoProvider.updateGpsStatusRepository()
    BaseActivity.compositeDisposable.add(
            repository.updateGpsStatus(updateGps)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val gpsStatusResponse = result as BaseResponse
                        XLog.d("GPS_STATUS : " + "RESPONSE : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name
                                + ",MESSAGE : " + gpsStatusResponse.message)
                        if (gpsStatusResponse.status == NetworkConstant.SUCCESS) {
                            AppDatabase.getDBInstance()!!.gpsStatusDao().updateIsUploadedAccordingToId(true, list[i].id)
                        }

                        i++
                        if (i < list.size) {
                            callUpdateGpsStatusApi(list)
                        } else {
                            i = 0
                            getProgressInstance().dismissDialog()
                            checkToCallLocationSync()
                        }

                    }, { error ->
                        //
                        XLog.d("GPS_STATUS : " + "RESPONSE ERROR: " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                        error.printStackTrace()
                        i++
                        if (i < list.size) {
                            callUpdateGpsStatusApi(list)
                        } else {
                            i = 0
                            getProgressInstance().dismissDialog()
                            checkToCallLocationSync()
                        }
                    })
    )
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun calllogoutApi(user_id: String, session_id: String) {

    if (Pref.current_latitude == null || Pref.current_longitude == null) {
        return
    }

    var intent = Intent(this, MonitorService::class.java)
    intent.action = CustomConstants.STOP_MONITOR_SERVICE
    //mContext.startService(intent)
    stopService(intent)

    var distance = 0.0
    val list = AppDatabase.getDBInstance()!!.userLocationDataDao().all
    if (list != null && list.size > 0) {
        val latestLat = list[list.size - 1].latitude
        val latestLong = list[list.size - 1].longitude

        /*val previousLat = list[list.size - 2].latitude
        val previousLong = list[list.size - 2].longitude*/

//            if (Pref.logout_latitude != "0.0" && Pref.logout_longitude != "0.0") {
//                /*if (latestLat != Pref.latitude && latestLong != Pref.longitude) {
//                    val distance = LocationWizard.getDistance(latestLat.toDouble(), latestLong.toDouble(),
//                            Pref.latitude!!.toDouble(), Pref.longitude!!.toDouble())
//
//                    XLog.d("LOGOUT : DISTANCE=====> $distance")
//                }*/
//
//                /*val distance = LocationWizard.getDistance(previousLat.toDouble(), previousLong.toDouble(),
//                        latestLat.toDouble(), latestLong.toDouble())*/
//
//                distance = LocationWizard.getDistance(latestLat.toDouble(), latestLong.toDouble(),
//                        Pref.logout_latitude.toDouble(), Pref.logout_longitude.toDouble())
//            }
    }

    val unSyncedList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADayNotSyn(AppUtils.convertFromRightToReverseFormat(Pref.login_date!!), false)

    if (unSyncedList != null && unSyncedList.isNotEmpty()) {
        var totalDistance = 0.0
        for (i in unSyncedList.indices) {
            totalDistance += unSyncedList[i].distance.toDouble()
        }

        distance = Pref.tempDistance.toDouble() + totalDistance
    } else
        distance = Pref.tempDistance.toDouble()

    var location = ""

    if (Pref.logout_latitude != "0.0" && Pref.logout_longitude != "0.0") {
        location = LocationWizard.getAdressFromLatlng(this, Pref.logout_latitude.toDouble(), Pref.logout_longitude.toDouble())

        if (location.contains("http"))
            location = "Unknown"
    }

    XLog.d("AUTO_LOGOUT : " + "REQUEST : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name)

    XLog.d("=======AUTO_LOGOUT INPUT PARAMS======")
    XLog.d("AUTO_LOGOUT : USER ID======> $user_id")
    XLog.d("AUTO_LOGOUT : SESSION ID======> $session_id")
    XLog.d("AUTO_LOGOUT : LAT====> " + Pref.logout_latitude)
    XLog.d("AUTO_LOGOUT : LONG=====> " + Pref.logout_longitude)
    XLog.d("AUTO_LOGOUT : DISTANCE=====> $distance")
    XLog.d("AUTO_LOGOUT : LOGOUT TIME========> " + AppUtils.getCurrentDateTime12(Pref.login_date!!))
    XLog.d("AUTO_LOGOUT : IS AUTO LOGOUT=======> 1")
    XLog.d("AUTO_LOGOUT : LOCATION=======> $location")
    XLog.d("=======================================")


    val repository = LogoutRepositoryProvider.provideLogoutRepository()
    BaseActivity.compositeDisposable.add(
            repository.logout(user_id, session_id, Pref.logout_latitude, Pref.logout_longitude, /*"2018-12-21 23:59:00"*/AppUtils.getCurrentDateTime12(Pref.login_date!!),
                    distance.toString(), "1", location)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val logoutResponse = result as BaseResponse
                        XLog.d("AUTO_LOGOUT : " + "RESPONSE : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + logoutResponse.message)
                        if (logoutResponse.status == NetworkConstant.SUCCESS) {

                            Pref.tempDistance = "0.0"
                            //Pref.prevOrderCollectionCheckTimeStamp = 0L

                            if (unSyncedList != null && unSyncedList.isNotEmpty()) {
                                for (i in unSyncedList.indices) {
                                    AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploaded(true, unSyncedList[i].locationId)
                                }
                            }

                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancelAll()

                            Pref.logout_latitude = "0.0"
                            Pref.logout_longitude = "0.0"

                            clearData()
                            Pref.isAutoLogout = false
                            Pref.isAddAttendence = false
                        } else
                            performLogout()

                        BaseActivity.isApiInitiated = false
                        takeActionOnGeofence()
                        getProgressInstance().dismissDialog()
                    }, { error ->
                        //
                        Toaster.msgShort(this@BaseActivity, getString(R.string.something_went_wrong))
                        XLog.d("AUTO_LOGOUT : " + "RESPONSE ERROR: " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                        error.printStackTrace()
                        getProgressInstance().dismissDialog()
                        performLogout()
                    })
    )
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun clearData() {
    doAsync {
        val result = runLongTask()
        uiThread {
            if (result == true) {
                Pref.user_id = ""
                Pref.session_token = ""
                Pref.login_date = ""
                Pref.isLogoutInitiated = false
                Pref.latitude = ""
                Pref.longitude = ""
                Pref.isShopVisited = false
                Pref.isOnLeave = ""
                Pref.willAlarmTrigger = false
                Pref.isHomeLocAvailable = false
                Pref.approvedInTime = ""
                Pref.approvedOutTime = ""
                Pref.home_latitude = ""
                Pref.home_longitude = ""
                Pref.isFieldWorkVisible = ""
                Pref.isOfflineTeam = false
                isMeetingUpdating = false
                Pref.visitDistance = ""
                Pref.distributorName = ""
                Pref.marketWorked = ""
                //AppUtils.timer = null

                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    jobScheduler.cancelAll()
                    XLog.d("============Alert Job scheduler cancel (Base Activity)==============")
                }
                else {
                    val serviceLauncher = Intent(this@BaseActivity, CollectionOrderAlertService::class.java)
                    stopService(serviceLauncher)
                }*/


                try {
                    val intent = Intent(this@BaseActivity, ToastBroadcastReceiver::class.java)
                    //intent.setAction(MyReceiver.ACTION_ALARM_RECEIVER)
                    val pendingIntent = PendingIntent.getBroadcast(this@BaseActivity, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                    val backupAlarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    backupAlarmMgr.cancel(pendingIntent)
                    pendingIntent.cancel()

                    Log.e("BaseActivity", "Stop Job Intent Service")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                AppUtils.clearPreferenceKey(this@BaseActivity, "STATE_LIST")
                AppUtils.clearPreferenceKey(this@BaseActivity, "PRODUCT_RATE_LIST")
                AppUtils.clearPreferenceKey(this@BaseActivity, "TEXT_LIST")
                AppUtils.clearPreferenceKey(this@BaseActivity, "Location")

                serviceStatusActionable()

                try {
                    val shopActivityList = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi())
                    for (i in shopActivityList.indices) {
                        if (!shopActivityList[i].isDurationCalculated && shopActivityList[i].startTimeStamp != "0") {
                            Pref.durationCompletedShopId = shopActivityList[i].shopid!!
                            val endTimeStamp = System.currentTimeMillis().toString()
                            val totalMinute = AppUtils.getMinuteFromTimeStamp(shopActivityList[i].startTimeStamp, endTimeStamp)
                            val duration = AppUtils.getTimeFromTimeSpan(shopActivityList[i].startTimeStamp, endTimeStamp)

                            if (!Pref.isMultipleVisitEnable) {
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopActivityList[i].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopActivityList[i].shopid!!, duration, AppUtils.getCurrentDateForShopActi())
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateIsUploaded(false, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                                //AppUtils.isShopVisited = false
                            } else {
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopActivityList[i].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi(), shopActivityList[i].startTimeStamp)
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopActivityList[i].startTimeStamp)
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopActivityList[i].shopid!!, duration, AppUtils.getCurrentDateForShopActi(), shopActivityList[i].startTimeStamp)
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopActivityList[i].startTimeStamp)
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateIsUploaded(false, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopActivityList[i].startTimeStamp)
                            }
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateOutTime(AppUtils.getCurrentTimeWithMeredian(), shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopActivityList[i].startTimeStamp)
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateOutLocation(LocationWizard.getNewLocationName(this@BaseActivity, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble()), shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopActivityList[i].startTimeStamp)

                            val netStatus = if (AppUtils.isOnline(this@BaseActivity))
                                "Online"
                            else
                                "Offline"

                            val netType = if (AppUtils.getNetworkType(this@BaseActivity).equals("wifi", ignoreCase = true))
                                AppUtils.getNetworkType(this@BaseActivity)
                            else
                                "Mobile ${AppUtils.mobNetType(this@BaseActivity)}"

                            if (!Pref.isMultipleVisitEnable) {
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateDeviceStatusReason(AppUtils.getDeviceName(), AppUtils.getAndroidVersion(),
                                        AppUtils.getBatteryPercentage(this@BaseActivity).toString(), netStatus, netType.toString(), shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                            } else {
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateDeviceStatusReason(AppUtils.getDeviceName(), AppUtils.getAndroidVersion(),
                                        AppUtils.getBatteryPercentage(this@BaseActivity).toString(), netStatus, netType.toString(), shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopActivityList[i].startTimeStamp)
                            }
                            if (Pref.willShowShopVisitReason && totalMinute.toInt() <= Pref.minVisitDurationSpentTime.toInt())
                                Pref.isShowShopVisitReason = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val intent = Intent(this@BaseActivity, LoginActivity::class.java)
                //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(0, 0)
                finishAffinity()
            }

        }
    }
}

private fun runLongTask(): Any {
    try {
        if (AppDatabase.getDBInstance()!!.userAttendanceDataDao().getLoginDate(Pref.user_id!!, AppUtils.getCurrentDateChanged()).isNotEmpty()) {
            val loginTime = AppDatabase.getDBInstance()!!.userAttendanceDataDao().getLoginTime(Pref.user_id!!, AppUtils.getCurrentDateChanged())
            val isOnLeave = AppDatabase.getDBInstance()!!.userAttendanceDataDao().getIsOnLeave(Pref.user_id!!, AppUtils.getCurrentDateChanged())
            val logoutTime = Pref.logout_time!!
            var result = ""
            if (isOnLeave.equals("false", ignoreCase = true))
                result = AppUtils.getTimeDuration(loginTime, logoutTime)
            AppDatabase.getDBInstance()!!.userAttendanceDataDao().updateDuration(result, Pref.user_id!!, AppUtils.getCurrentDateChanged())
            AppDatabase.getDBInstance()!!.userAttendanceDataDao().updateLogoutTimeN(AppUtils.convertTime(FTStorageUtils.getStringToDate(AppUtils.getCurrentISODateTime())), Pref.user_id!!, AppUtils.getCurrentDateChanged())
        } else {
            if (!TextUtils.isEmpty(Pref.add_attendence_time)) {
                val loginTime = Pref.add_attendence_time!!
                val logoutTime = Pref.logout_time!!
                val result = AppUtils.getTimeDuration(loginTime, logoutTime)
                AppDatabase.getDBInstance()!!.userAttendanceDataDao().updateDuration(result, Pref.user_id!!, Pref.login_date!!)
                AppDatabase.getDBInstance()!!.userAttendanceDataDao().updateLogoutTimeN(logoutTime, Pref.user_id!!, Pref.login_date!!)
                Pref.add_attendence_time = ""
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return true
}


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun checkGPSAvailability() {
    var manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        buildAlertMessageNoGps()
    } else {
        //if (PermissionHelper.checkLocationPermission(this, 0)) {
        //Settings.Secure.putInt(contentResolver, Settings.Secure.LOCATION_MODE, 3)
        if (!FTStorageUtils.isMyServiceRunning(LocationFuzedService::class.java, this)) {
            /*Start & Stop Expensive service stuff when logged out*/
            serviceStatusActionable()
            /*val serviceLauncher = Intent(this, LocationFuzedService::class.java)
            startService(serviceLauncher)*/
        }
        //}
    }
}


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun serviceStatusActionable() {
    try {
        if (Pref.IsLeavePressed == true && Pref.IsLeaveGPSTrack == false) {
            return
        }
        val serviceLauncher = Intent(this, LocationFuzedService::class.java)
        if (Pref.user_id != null && Pref.user_id!!.isNotEmpty()) {
            startMonitorService()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val componentName = ComponentName(this, LocationJobService::class.java)
                val jobInfo = JobInfo.Builder(12, componentName)
                        //.setRequiresCharging(true)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        //.setRequiresDeviceIdle(true)
                        .setOverrideDeadline(1000)
                        .build()

                val resultCode = jobScheduler.schedule(jobInfo)

                if (resultCode == JobScheduler.RESULT_SUCCESS) {
                    XLog.d("===============================Job scheduled (Base Activity) " + AppUtils.getCurrentDateTime() + "============================")
                } else {
                    XLog.d("=====================Job not scheduled (Base Activity) " + AppUtils.getCurrentDateTime() + "====================================")
                }
            } else {
                startService(serviceLauncher)
                startMonitorService()
            }
        } else {
            stopService(serviceLauncher)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                jobScheduler.cancelAll()
                XLog.d("===============================Job scheduler cancel (Base Activity)" + AppUtils.getCurrentDateTime() + "============================")

                /*if (AppUtils.mGoogleAPIClient != null) {
                    AppUtils.mGoogleAPIClient?.disconnect()
                    AppUtils.mGoogleAPIClient = null
                }*/
            }

            /*val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()*/

            AlarmReceiver.stopServiceAlarm(this, 123)
            XLog.d("===========Service alarm is stopped (Base Activity)================")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun buildAlertMessageNoGps() {
    /*val builder = AlertDialog.Builder(this)
    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 100)
            })
//                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
    val alert = builder.create()
    alert.show()*/

    mGpsStatusDetector = GpsStatusDetector(this)
    mGpsStatusDetector?.checkGpsStatus()
}

// GpsStatusDetectorCallBack
override fun onGpsSettingStatus(enabled: Boolean) {

    if (enabled)
        Log.e("splash", "GPS enabled")
    else
        Log.e("splash", "GPS disabled")
}

override fun onGpsAlertCanceledByUser() {
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    /*if (requestCode == 100) {
        var manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (PermissionHelper.checkLocationPermission(this, 0)) {
                val serviceLauncher = Intent(this, LocationFuzedService::class.java)
                startService(serviceLauncher)
            }
        } else {
            //buildAlertMessageNoGps()
        }


    }*/

    /*if (resultCode == Activity.RESULT_OK) {
        mGpsStatusDetector?.checkOnActivityResult(requestCode, resultCode)
        //checkGPSProvider()
        val serviceLauncher = Intent(this, LocationFuzedService::class.java)
        startService(serviceLauncher)
    }
    else {
        finish()
        System.exit(0)
    }*/
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun clearDataOnLogoutSync() {
    doAsync {
        var result = runLongTask()
        uiThread {
            if (result == true) {
                Pref.user_id = ""
                Pref.session_token = ""
                Pref.login_date = ""
                Pref.isLogoutInitiated = false
                Pref.latitude = ""
                Pref.longitude = ""

                serviceStatusActionable()

                var shopActivityList = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi())
                for (i in 0 until shopActivityList.size) {
                    if (!shopActivityList[i].isDurationCalculated && shopActivityList[i].startTimeStamp != "0") {
                        val endTimeStamp = System.currentTimeMillis().toString()
                        val totalMinute = AppUtils.getMinuteFromTimeStamp(shopActivityList[i].startTimeStamp, endTimeStamp)
                        val duration = AppUtils.getTimeFromTimeSpan(shopActivityList[i].startTimeStamp, endTimeStamp)
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopActivityList[i].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopActivityList[i].shopid!!, duration, AppUtils.getCurrentDateForShopActi())
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateIsUploaded(false, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi())

//                            AppUtils.isShopVisited = false
                        Pref.isShopVisited = false
                    }
                }

                CommonDialogSingleBtn.getInstance(getString(R.string.data_sync_completed_header), getString(R.string.data_sync_completed_content), getString(R.string.ok), object : OnDialogClickListener {
                    override fun onOkClick() {
                        startActivity(Intent(this@BaseActivity, LoginActivity::class.java))
                        overridePendingTransition(0, 0)
                        finishAffinity()
                    }
                }).show(supportFragmentManager, "CommonDialogSingleBtn")

            }

        }
    }
}

override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
}

override fun onStop() {
    super.onStop()
    if (isMyServiceRunning(FloatingWidgetService::class.java, this)) {
        val i = Intent(applicationContext, FloatingWidgetService::class.java)
        stopService(i)
    }
}

override fun onDestroy() {
    compositeDisposable.clear()
    isApiInitiated = false

    super.onDestroy()
    //unregisterReceiver(broadcastReceiver)
}

///////////////////////////////////////////////////////////////////////////////////////////////
fun startMonitorService() {
    if (!isMonitorServiceRunning()) {
        XLog.d("MonitorService Started : " + " Time :" + AppUtils.getCurrentDateTime())
        val intent = Intent(applicationContext, MonitorService::class.java)
        intent.action = CustomConstants.START_MONITOR_SERVICE
        startService(intent)
        //Toast.makeText(this, "Loc service started", Toast.LENGTH_SHORT).show()
    }

}

fun stopLocationService() {
    if (isMonitorServiceRunning()) {
        //Intent intent=new Intent(getApplicationContext(), LocationService.class);
        val intent = Intent(this, MonitorService::class.java)
        intent.action = CustomConstants.STOP_MONITOR_SERVICE
        startService(intent)
        //Toast.makeText(this, "Loc service stop", Toast.LENGTH_SHORT).show()
    }
}

fun isMonitorServiceRunning(): Boolean {
    val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    if (activityManager != null) {
        val servicesList = activityManager.getRunningServices(Int.MAX_VALUE)
        for (serviceInfo in servicesList) {
            if (MonitorService::class.java.getName() == serviceInfo.service.className) {
                //if (serviceInfo.foreground) {
                return true
                //}
            }
        }
        return false
    }
    return false
}

}
