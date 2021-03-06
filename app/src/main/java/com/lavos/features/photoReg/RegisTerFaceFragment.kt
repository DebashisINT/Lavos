package com.lavos.features.photoReg

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.lavos.CustomStatic
import com.lavos.R
import com.lavos.app.NetworkConstant
import com.lavos.app.Pref
import com.lavos.app.types.FragType
import com.lavos.app.uiaction.IntentActionable
import com.lavos.app.utils.AppUtils
import com.lavos.app.utils.PermissionUtils
import com.lavos.app.utils.Toaster
import com.lavos.base.presentation.BaseActivity
import com.lavos.base.presentation.BaseFragment
import com.lavos.faceRec.DetectorActivity
import com.lavos.faceRec.FaceStartActivity
import com.lavos.faceRec.FaceStartActivity.detector
import com.lavos.faceRec.tflite.SimilarityClassifier.Recognition
import com.lavos.faceRec.tflite.TFLiteObjectDetectionAPIModel
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.photoReg.api.GetUserListPhotoRegProvider
import com.lavos.features.photoReg.model.FaceRegResponse
import com.lavos.features.photoReg.model.UserListResponseModel
import com.lavos.features.photoReg.model.UserPhotoRegModel
import com.lavos.widgets.AppCustomTextView
import com.elvishew.xlog.XLog
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.pnikosis.materialishprogress.ProgressWheel
import com.themechangeapp.pickimage.PermissionHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_register_face.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*

class RegisTerFaceFragment: BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context
    private var imagePath: String = ""
    private lateinit var nameTV: AppCustomTextView
    private lateinit var phoneTV: AppCustomTextView
    private lateinit var registerTV: Button
    private lateinit var progress_wheel: ProgressWheel
    private lateinit var ll_phone : LinearLayout

    private lateinit var shopLargeImg:ImageView

    var takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    lateinit var imgUri:Uri
    var facePicTag:Boolean = false

    ////
    protected var previewWidth = 0
    protected var previewHeight = 0
    private var portraitBmp: Bitmap? = null
    private var rgbFrameBitmap: Bitmap? = null
    private var faceBmp: Bitmap? = null
    var faceDetector: FaceDetector? = null
    private val TF_OD_API_MODEL_FILE = "mobile_face_net.tflite"
    val TF_OD_API_IS_QUANTIZED = false
    val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"
    val TF_OD_API_INPUT_SIZE = 112

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object{
        var user_id: String? = null
        var user_name: String? = null
        var user_login_id: String? = null
        var user_contactid: String? = null
        fun getInstance(objects: Any): RegisTerFaceFragment {
            val regisTerFaceFragment = RegisTerFaceFragment()
            if (!TextUtils.isEmpty(objects.toString())) {

                var obj = objects as UserListResponseModel

                user_id=obj!!.user_id.toString()
                user_name=obj!!.user_name
                user_login_id=obj!!.user_login_id
                user_contactid=obj!!.user_contactid
            }
            return regisTerFaceFragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_register_face, container, false)
        initView(view)
        return view
    }

    override fun onResume() {
        super.onResume()
    }

    private fun initView(view:View){
        nameTV = view.findViewById(R.id.tv_frag_reg_face_name)
        phoneTV = view.findViewById(R.id.tv_frag_reg_face_phone)
        registerTV = view.findViewById(R.id.btn_frag_reg_face_register)
        progress_wheel = view.findViewById(R.id.progress_wheel)
        progress_wheel.stopSpinning()
        registerTV.setOnClickListener(this)

        nameTV.text = user_name!!
        phoneTV.text = user_login_id!!

        shopLargeImg = view!!.findViewById(R.id.iv_frag_reg_face)

        ll_phone = view.findViewById(R.id.ll_regis_face_phone);
        ll_phone.setOnClickListener(this)

        faceDetectorSetUp()
        faceDetectorSetUpRandom()

      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            initPermissionCheck()
        else {
            launchCamera()
        }*/
        //showPictureDialog()

        //startActivity(Intent(mContext, CustomCameraActivity::class.java))

        launchCamera()
    }

    fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(mContext)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> selectImageInAlbum()
                1 -> launchCamera()
            }
        }
        pictureDialog.show()
    }

    fun selectImageInAlbum() {
        if (PermissionHelper.checkStoragePermission(mContext as DashboardActivity)) {
            val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            (mContext as DashboardActivity).startActivityForResult(intent, PermissionHelper.REQUEST_CODE_STORAGE)
        }

    }


    private var permissionUtils: PermissionUtils? = null
    private fun initPermissionCheck() {
        permissionUtils = PermissionUtils(mContext as Activity, object : PermissionUtils.OnPermissionListener {
            override fun onPermissionGranted() {
                launchCamera()
            }

            override fun onPermissionNotGranted() {
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.accept_permission))
            }

        }, arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    fun launchCamera() {
        if (PermissionHelper.checkCameraPermission(mContext as DashboardActivity) && PermissionHelper.checkStoragePermission(mContext as DashboardActivity)) {
            /*val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, (mContext as DashboardActivity).getPhotoFileUri(System.currentTimeMillis().toString() + ".png"))
            (mContext as DashboardActivity).startActivityForResult(intent, PermissionHelper.REQUEST_CODE_CAMERA)*/

            (mContext as DashboardActivity).captureImage()
        }
    }

    fun setImage(imgRealPath: Uri, fileSizeInKB: Long) {
        imgUri=imgRealPath
        imagePath = imgRealPath.toString()

        getBitmap(imgRealPath.path)


            /*Picasso.get()
                    .load(imgRealPath)
                    .resize(500, 500)
                    .into(shopLargeImg)*/

    }

    private fun registerFaceApi(){
        progress_wheel.spin()
        var obj= UserPhotoRegModel()
        //obj.user_id= Pref.user_id
        obj.user_id= user_id
        obj.session_token=Pref.session_token

        //obj.registration_date_time=AppUtils.getCurrentDateTimeDDMMYY()
        obj.registration_date_time=AppUtils.getCurrentDateTime()

        val repository = GetUserListPhotoRegProvider.providePhotoReg()
        BaseActivity.compositeDisposable.add(
                repository.addUserFaceRegImg(obj,imagePath,mContext,user_contactid)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as FaceRegResponse
                            if(response.status== NetworkConstant.SUCCESS){
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.face_reg_success))
                                Handler(Looper.getMainLooper()).postDelayed({
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).loadFragment(FragType.ProtoRegistrationFragment, false, "")

                                }, 500)

                                XLog.d(" RegisTerFaceFragment : FaceImageDetection/FaceImage" +response.status.toString() +", : "  + ", Success: "+AppUtils.getCurrentDateTime().toString())
                            }else{
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_reg_face))
                                XLog.d("RegisTerFaceFragment : FaceImageDetection/FaceImage : " + response.status.toString() +", : "  + ", Failed: "+AppUtils.getCurrentDateTime().toString())
                            }
                        },{
                            error ->
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_reg_face))
                            if (error != null) {
                                XLog.d("RegisTerFaceFragment : FaceImageDetection/FaceImage : " + " : "  + ", ERROR: " + error.localizedMessage)
                            }
                        })
        )
    }

    override fun onClick(p0: View?) {
        if(p0!=null){
            when(p0.id){
                R.id.btn_frag_reg_face_register -> {
                    //if(registerTV.isEnabled==false){
                    if(!facePicTag){
                        Toaster.msgShort(mContext,"Please capture valid image")
                        return
                    }

                    if(imagePath.length>0 && imagePath!="") {
                        val simpleDialogg = Dialog(mContext)
                        simpleDialogg.setCancelable(false)
                        simpleDialogg.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        simpleDialogg.setContentView(R.layout.dialog_yes_no)
                        val dialogHeader = simpleDialogg.findViewById(R.id.dialog_cancel_order_header_TV) as AppCustomTextView
                        dialogHeader.text="Do you want to Register ?"
                        val dialogYes = simpleDialogg.findViewById(R.id.tv_dialog_yes_no_yes) as AppCustomTextView
                        val dialogNo = simpleDialogg.findViewById(R.id.tv_dialog_yes_no_no) as AppCustomTextView

                        dialogYes.setOnClickListener( { view ->
                            simpleDialogg.cancel()
                            if (AppUtils.isOnline(mContext)){
                                registerFaceApi()
                            }else{
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
                            }

                        })
                        dialogNo.setOnClickListener( { view ->
                            simpleDialogg.cancel()
                        })
                        simpleDialogg.show()
                    }


                       // registerFaceApi()
                }

                R.id.ll_regis_face_phone ->{
                    IntentActionable.initiatePhoneCall(mContext, phoneTV.text.toString())
                }

            }
        }
    }





    private fun saveImageToGallery() {
        iv_frag_reg_face.setRotation(90f)
        iv_frag_reg_face.setDrawingCacheEnabled(true)
        val b: Bitmap = iv_frag_reg_face.getDrawingCache()
        MediaStore.Images.Media.insertImage(activity!!.contentResolver, b, imgUri.toString(), "")
    }

////////////////////////////////////////////////////////////


    fun faceDetectorSetUp(){
        try {
            FaceStartActivity.detector = TFLiteObjectDetectionAPIModel.create(
                    mContext.getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED)
            //cropSize = TF_OD_API_INPUT_SIZE;
        } catch (e: IOException) {
            e.printStackTrace()
            //LOGGER.e(e, "Exception initializing classifier!");
            val toast = Toast.makeText(mContext, "Classifier could not be initialized", Toast.LENGTH_SHORT)
            toast.show()
            //finish()
        }
        val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

        val detector = FaceDetection.getClient(options)

        faceDetector = detector
    }


    lateinit var face1:Face
    lateinit var face2:Face

    private fun registerFace(mBitmap: Bitmap?) {

/*        val face_dec:com.google.android.gms.vision.face.FaceDetector = com.google.android.gms.vision.face.FaceDetector.Builder(mContext)
                .setTrackingEnabled(false)
                .setLandmarkType(com.google.android.gms.vision.face.FaceDetector.ALL_LANDMARKS)
                .setMode(com.google.android.gms.vision.face.FaceDetector.FAST_MODE)
                .build()

        val frame: Frame = Frame.Builder().setBitmap(mBitmap).build()
        //val faces: SparseArray = face_dec.detect(frame)
        val facesa: SparseArray<com.google.android.gms.vision.face.Face> = face_dec.detect(frame)
        var ss="asf"
        Toaster.msgShort(mContext,"facesa"+facesa.size().toString())*/

        try {
            if (mBitmap == null) {
                //Toast.makeText(this, "No File", Toast.LENGTH_SHORT).show()
                return
            }
            //ivFace.setImageBitmap(mBitmap)
            previewWidth = mBitmap.width
            previewHeight = mBitmap.height
            rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
            portraitBmp = mBitmap
            val image = InputImage.fromBitmap(mBitmap, 0)
            faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888)
            faceDetector?.process(image)?.addOnSuccessListener(OnSuccessListener<List<Face>> { faces ->
                if (faces.size == 0) {
                    Toaster.msgShort(mContext,"Please choose proper face")
                    facePicTag=false
                    return@OnSuccessListener
                }
                Handler().post {
                    object : Thread() {
                        override fun run() {
                            //action
                            //onFacesDetected(1, faces, true) //no need to add currtime
                            face1=faces.get(0)
                            activateRegisterFace()
                        }
                    }.start()
                }
            })


        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun activateRegisterFace(){
        doAsync {
            uiThread {
                //registerTV.isEnabled=true
                //faceDetector?.close()
                facePicTag=true
                //GetImageFromUrl().execute("http://3.7.30.86:82/CommonFolder/FaceImageDetection/EMS0000070.jpg")
                //Toaster.msgShort(mContext,"face present in pic")
            }
        }


    }

    //fun getBitmap(path: String?): Bitmap? {
    fun getBitmap(path: String?) {
        var bitmap: Bitmap? = null
        try {
            val f = File(path)
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
            shopLargeImg.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        registerFace(bitmap)
        //return bitmap
    }


    ///////////////////////////////////////testtttttttttt
    var faceDetectorRandom: FaceDetector? = null

    fun faceDetectorSetUpRandom(){
        try {
            FaceStartActivity.detector = TFLiteObjectDetectionAPIModel.create(
                    mContext.getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED)
            //cropSize = TF_OD_API_INPUT_SIZE;
        } catch (e: IOException) {
            e.printStackTrace()
            //LOGGER.e(e, "Exception initializing classifier!");
            val toast = Toast.makeText(mContext, "Classifier could not be initialized", Toast.LENGTH_SHORT)
            toast.show()
            //finish()
        }
        val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

        val detector = FaceDetection.getClient(options)

        faceDetectorRandom = detector
    }

    private fun registerFaceRandom(mBitmap: Bitmap?) {

/*        val face_dec:com.google.android.gms.vision.face.FaceDetector = com.google.android.gms.vision.face.FaceDetector.Builder(mContext)
                .setTrackingEnabled(false)
                .setLandmarkType(com.google.android.gms.vision.face.FaceDetector.ALL_LANDMARKS)
                .setMode(com.google.android.gms.vision.face.FaceDetector.FAST_MODE)
                .build()

        val frame: Frame = Frame.Builder().setBitmap(mBitmap).build()
        //val faces: SparseArray = face_dec.detect(frame)
        val facesa: SparseArray<com.google.android.gms.vision.face.Face> = face_dec.detect(frame)
        var ss="asf"
        Toaster.msgShort(mContext,"facesa"+facesa.size().toString())*/

        try {
            if (mBitmap == null) {
                //Toast.makeText(this, "No File", Toast.LENGTH_SHORT).show()
                return
            }
            //ivFace.setImageBitmap(mBitmap)
            previewWidth = mBitmap.width
            previewHeight = mBitmap.height
            rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
            portraitBmp = mBitmap
            val image = InputImage.fromBitmap(mBitmap, 0)
            faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888)
            faceDetectorRandom?.process(image)?.addOnSuccessListener(OnSuccessListener<List<Face>> { faces ->
                if (faces.size == 0) {
                    Toaster.msgShort(mContext,"Please choose proper face")
                    facePicTag=false
                    return@OnSuccessListener
                }
                Handler().post {
                    object : Thread() {
                        override fun run() {
                            //action
                            //onFacesDetected(1, faces, true) //no need to add currtime
                            face2=faces.get(0)
                           var ss="asd"
                            if(face1==face2){
                                var a="as"
                            }else{
                                var b="ty"
                            }
                            onFacesDetected(1, faces, true)
                        }
                    }.start()
                }
            })


        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    var cropToFrameTransform: Matrix? = Matrix()

    fun onFacesDetected(currTimestamp: Long, faces: List<Face>, add: Boolean) {
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.0f
        val mappedRecognitions: MutableList<Recognition> = LinkedList()


        //final List<Classifier.Recognition> results = new ArrayList<>();

        // Note this can be done only once
        val sourceW = rgbFrameBitmap!!.width
        val sourceH = rgbFrameBitmap!!.height
        val targetW = portraitBmp!!.width
        val targetH = portraitBmp!!.height
        val transform = createTransform(
                sourceW,
                sourceH,
                targetW,
                targetH,
                90)
        val mutableBitmap = portraitBmp!!.copy(Bitmap.Config.ARGB_8888, true)
        val cv = Canvas(mutableBitmap)

        // draws the original image in portrait mode.
        cv.drawBitmap(rgbFrameBitmap!!, transform!!, null)
        val cvFace = Canvas(faceBmp!!)
        val saved = false
        for (face in faces) {
            //results = detector.recognizeImage(croppedBitmap);
            val boundingBox = RectF(face.boundingBox)

            //final boolean goodConfidence = result.getConfidence() >= minimumConfidence;
            val goodConfidence = true //face.get;
            if (boundingBox != null && goodConfidence) {

                // maps crop coordinates to original
                cropToFrameTransform?.mapRect(boundingBox)

                // maps original coordinates to portrait coordinates
                val faceBB = RectF(boundingBox)
                transform.mapRect(faceBB)

                // translates portrait to origin and scales to fit input inference size
                //cv.drawRect(faceBB, paint);
                val sx = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.width()
                val sy = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.height()
                val matrix = Matrix()
                matrix.postTranslate(-faceBB.left, -faceBB.top)
                matrix.postScale(sx, sy)
                cvFace.drawBitmap(portraitBmp!!, matrix, null)

                //canvas.drawRect(faceBB, paint);
                var label = ""
                var confidence = -1f
                var color = Color.BLUE
                var extra: Any? = null
                var crop: Bitmap? = null
                if (add) {
                    try {
                        crop = Bitmap.createBitmap(portraitBmp!!,
                                faceBB.left.toInt(),
                                faceBB.top.toInt(),
                                faceBB.width().toInt(),
                                faceBB.height().toInt())
                    } catch (eon: java.lang.Exception) {
                        //runOnUiThread(Runnable { Toast.makeText(mContext, "Failed to detect", Toast.LENGTH_LONG) })
                    }
                }
                val startTime = SystemClock.uptimeMillis()
                val resultsAux = FaceStartActivity.detector.recognizeImage(faceBmp, add)
                val lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
                if (resultsAux.size > 0) {
                    val result = resultsAux[0]
                    extra = result.extra
                    //          Object extra = result.getExtra();
//          if (extra != null) {
//            LOGGER.i("embeeding retrieved " + extra.toString());
//          }
                    val conf = result.distance
                    if (conf < 1.0f) {
                        confidence = conf
                        label = result.title
                        color = if (result.id == "0") {
                            Color.GREEN
                        } else {
                            Color.RED
                        }
                    }
                }
                val flip = Matrix()
                flip.postScale(1f, -1f, previewWidth / 2.0f, previewHeight / 2.0f)

                //flip.postScale(1, -1, targetW / 2.0f, targetH / 2.0f);
                flip.mapRect(boundingBox)
                val result = Recognition(
                        "0", label, confidence, boundingBox)
                result.color = color
                result.location = boundingBox
                result.extra = extra
                result.crop = crop
                mappedRecognitions.add(result)
            }
        }

        //    if (saved) {
//      lastSaved = System.currentTimeMillis();
//    }

        Log.e("xc", "startabc" )
        val rec = mappedRecognitions[0]
        FaceStartActivity.detector.register("", rec)
        //val intent = Intent(mContext, DetectorActivity::class.java)
        //startActivityForResult(intent, 171)
//        startActivity(new Intent(this,DetectorActivity.class));
//        finish();

        // detector.register("Sakil", rec);
        /*   runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ivFace.setImageBitmap(rec.getCrop());
                //showAddFaceDialog(rec);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.image_edit_dialog, null);
                ImageView ivFace = dialogLayout.findViewById(R.id.dlg_image);
                TextView tvTitle = dialogLayout.findViewById(R.id.dlg_title);
                EditText etName = dialogLayout.findViewById(R.id.dlg_input);

                tvTitle.setText("Register Your Face");
                ivFace.setImageBitmap(rec.getCrop());
                etName.setHint("Please tell your name");
                detector.register("sam", rec); //for register a face

                //button.setPressed(true);
                //button.performClick();
            }

        });*/

        // updateResults(currTimestamp, mappedRecognitions);
    }

    fun createTransform(srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int, applyRotation: Int): Matrix? {
        val matrix = Matrix()
        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                // LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

            // Rotate around origin.
            matrix.postRotate(applyRotation.toFloat())
        }

//        // Account for the already applied rotation, if any, and then determine how
//        // much scaling is needed for each axis.
//        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
//        final int inWidth = transpose ? srcHeight : srcWidth;
//        final int inHeight = transpose ? srcWidth : srcHeight;
        if (applyRotation != 0) {

            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)
        }
        return matrix
    }


    inner class GetImageFromUrl : AsyncTask<String?, Void?, Bitmap?>() {
        fun GetImageFromUrl() {
            //this.imageView = img;
        }
        override fun doInBackground(vararg url: String?): Bitmap {
            var bitmappppx: Bitmap? = null
            val stringUrl = url[0]
            bitmappppx = null
            val inputStream: InputStream
            try {
                inputStream = URL(stringUrl).openStream()
                bitmappppx = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return bitmappppx!!
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            registerFaceRandom(result)
        }

    }


}