package edu.tcu.hienminhdau.paint

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // set up the pallet and width selector
        val drawingView = findViewById<DrawingView>(R.id.drawing_view)
        setUpPallet(drawingView)
        setUpPathWidthSelector(drawingView)

        // set up undo
        findViewById<ImageView>(R.id.undo_iv).setOnClickListener{
            drawingView.undoPath()
        }

        // set up background picker
        val backgroundIv = findViewById<ImageView>(R.id.background_iv)
        setUpBackgroundPicker(backgroundIv)

        // set up save
        setupSave(backgroundIv)
    }

    private fun setUpPallet(drawingView: DrawingView){
        val palletLl = findViewById<LinearLayout>(R.id.pallet_ll)
        palletLl.children.forEach { imageViewVar ->
            imageViewVar.setOnClickListener{
                palletLl.children.forEach { view -> (view as ImageView).setImageResource(R.drawable.path_color_normal) }
                (imageViewVar as ImageView).setImageResource(R.drawable.path_color_selected)
                drawingView.setPathColor((imageViewVar.background as ColorDrawable).color)
            }
        }
    }

    private fun setUpPathWidthSelector(drawingView: DrawingView){
        findViewById<ImageView>(R.id.brush_iv).setOnClickListener{
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.path_width_selector)
            dialog.show()
            dialog.findViewById<ImageView>(R.id.small_width_iv).setOnClickListener {
                drawingView.setPathWidth(5)
                dialog.dismiss()
            }

            dialog.findViewById<ImageView>(R.id.medium_width_iv).setOnClickListener{
                drawingView.setPathWidth(10)
                dialog.dismiss()
            }

            dialog.findViewById<ImageView>(R.id.large_width_iv).setOnClickListener{
                drawingView.setPathWidth(15)
                dialog.dismiss()
            }
        }
    }

    private fun setUpBackgroundPicker(backgroundIv: ImageView){
        // Registers a photo picker activity launcher in single-select mode.
        val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
            uri?.let{Glide.with(this).load(uri).into(backgroundIv)}
        }

        findViewById<ImageView>(R.id.gallery_iv).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    }

    private fun setupSave(backgroundIv: ImageView){
        // if backgroundIv is null, assign a color to the backgroundIv
        if(backgroundIv.drawable == null){
            backgroundIv.setBackgroundColor(getColor(R.color.off_white))
        }


        findViewById<ImageView>(R.id.save_iv).setOnClickListener {
            val dialog = inProgress()
            lifecycleScope.launch(Dispatchers.IO){
                val bitmap = findViewById<FrameLayout>(R.id.drawing_fl).drawToBitmap()

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME,
                        System.currentTimeMillis().toString().substring(2, 11) + ".jpeg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "img/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
                }

                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    contentResolver.openOutputStream(uri).use { image ->
                        image?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, image) }
                    } }

                dialog.dismiss()

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TITLE, "Sharing image")
                    type = "image/jpeg"
                }
                startActivity(Intent.createChooser(shareIntent, null))
            }
        }
    }

    private fun inProgress(): Dialog{
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.progress)
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}