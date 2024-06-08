package com.saimedevs.compose.imagecropper

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.saimedevs.compose.imagecropper.easycrop.CropError
import com.saimedevs.compose.imagecropper.easycrop.CropResult
import com.saimedevs.compose.imagecropper.easycrop.CropperStyle
import com.saimedevs.compose.imagecropper.easycrop.ImageCropper
import com.saimedevs.compose.imagecropper.easycrop.crop
import com.saimedevs.compose.imagecropper.easycrop.ui.ImageCropperDialog
import com.saimedevs.compose.imagecropper.ui.theme.ImageCropperTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    val imageCropper = ImageCropper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val cropState = imageCropper.cropState
            if(cropState != null) ImageCropperDialog(state = cropState,
                style = CropperStyle(
                    overlay = Color.Red.copy(alpha = .5f),
                    autoZoom = false,
                    guidelines = null,
                )
            )
            var imageUri by remember { mutableStateOf<Uri?>(null) }
            var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                imageUri = uri
            }
            ImageCropperTheme {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(top = 50.dp)
                ) {

                    Row( modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround) {
                        Button(onClick = { launcher.launch("image/*") }) {
                            Text("Pick an image")
                        }

                        if(imageUri != null){
                            Button(onClick = {

                                scope.launch {
                                    when(val result =  imageCropper.crop(imageUri!!, context)){
                                        CropResult.Cancelled -> {}
                                        CropError.LoadingError -> {}
                                        CropError.SavingError -> {}
                                        is CropResult.Success -> {
                                            bitmap = result.bitmap
                                        }
                                    }


                                }
                            }) {
                                Text("Crop an image")
                            }
                        }

                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    imageUri?.let {

                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier.size(300.dp),
                            contentScale = ContentScale.Crop
                        )
                    }



                    Spacer(modifier = Modifier.height(16.dp))

                    bitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            modifier = Modifier.size(300.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

