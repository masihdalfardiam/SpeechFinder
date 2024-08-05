package com.example.speechfinder

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.speachfinder.R
import com.example.speechfinder.MainActivity.Companion.dataList
import com.example.speechfinder.MainActivity.Companion.durationInSeconds
import com.example.speechfinder.MainActivity.Companion.fileName
import com.example.speechfinder.MainActivity.Companion.pos
import com.example.speechfinder.login.LoginActivity
import com.example.speechfinder.table.TableActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException


class MainActivity : ComponentActivity() {
    companion object {
        var path: String = ""
        var fileName: String = ""
        val dataList = mutableListOf<Int>()
        var durationInSeconds = 0
        var pos = 0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // A surface container using the 'background' color from the theme
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                TwoFrameApp()
            }
        }
    }
}

@Composable
fun TwoFrameApp() {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var showSecondFrame by remember { mutableStateOf(false) }
    var errorShowing by remember { mutableStateOf(false) }


    val filePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri: Uri? ->
                uri?.let {
                    val fileUtils = FileUtils(context)
                    val path = fileUtils.getPath(it)
                    text = path

                    if (path.substring(path.lastIndexOf('.') + 1) == "mp3") {
                        fileName = path.substring(path.lastIndexOf('/') + 1)
                        durationInSeconds = getAudioFileDuration(context, it)!!

                        sendFileNameToApi(context, fileName)
                        showSecondFrame = true
                    } else {

                        CoroutineScope(Dispatchers.Main).launch {
                            errorShowing = true
                            delay(2000)
                            errorShowing = false
                        }
                    }
                }
            })

    Box(modifier = Modifier.fillMaxSize()) {
        if (showSecondFrame) {
            SecondFrame(path = text, onBackClick = {
                showSecondFrame = false
                text = "" // Clear text
            }, onDashboardClick = {

                val intent = Intent(context, TableActivity::class.java)
                context.startActivity(intent)
                showSecondFrame = false
            })
        } else {
            FirstFrame(text = text, onBrowseClick = {
                filePickerLauncher.launch(arrayOf("audio/*"))
            }, onLogoutClick = {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
                if (context is Activity) {
                    context.finish()
                }
            })
        }

        if (errorShowing) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 250.dp),
                text = "The Entered Path is INVALID!",
                color = Color.Red,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

fun checkValidPath(address: String): Boolean {

    // بررسی وجود فایل
    val file = File(address)
    return file.exists() && file.canRead()
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FirstFrame(text: String, onBrowseClick: () -> Unit, onLogoutClick: () -> Unit) {
    var isTextFieldFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var currentText by remember { mutableStateOf(text) }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        InformationDialog(onDismiss = { showDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(25, 25, 30)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(25, 25, 30))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info, contentDescription = null, tint = Color.Gray
                )
            }
            Button(
                onClick = onLogoutClick,
                shape = RoundedCornerShape(25),
                modifier = Modifier
                    .height(45.dp)
                    .width(130.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(45, 45, 50), contentColor = Color.White
                )
            ) {
                Text("Logout")
            }
        }
        Spacer(modifier = Modifier.height(300.dp))
        val containerColor = Color(35, 35, 40)
        TextField(
            value = currentText, textStyle = TextStyle(
                fontSize = 17.sp
            ), onValueChange = {
                currentText = it
            }, keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ), trailingIcon = {
                Icon(imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        onBrowseClick()
                    })
            }, colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                disabledTextColor = Color.Transparent,
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                disabledContainerColor = containerColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ), placeholder = {
                Text("Please enter your file address:", color = Color.LightGray)
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 0.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                MainActivity.path = currentText
                onBrowseClick()
                keyboardController?.hide()
            },
            shape = RoundedCornerShape(25),
            modifier = Modifier
                .height(45.dp)
                .width(130.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(45, 45, 50), contentColor = Color.White
            )
        ) {
            Text("Browse")
        }
    }
}


@Composable
fun SecondFrame(path: String, onBackClick: () -> Unit, onDashboardClick: () -> Unit) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var seekbarValueState by remember { mutableFloatStateOf(0f) }
    var volumeValueState by remember { mutableFloatStateOf(0.7f) }
    var searchText by remember { mutableStateOf("") }
    val medialPlayer = remember {
        MediaPlayer()
    }
    var currentPosition by remember {
        mutableIntStateOf(medialPlayer.currentPosition)
    }

    val timesList = remember {
        listOf(0, 14000, 18000, 30000, 40000)
    }
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    LaunchedEffect(Unit) {
        medialPlayer.setDataSource(path)
        medialPlayer.prepare()
        medialPlayer.setOnPreparedListener {
            it.start()
        }
        volumeValueState = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / 15

        while (isPlaying) {
            medialPlayer?.let { player ->
                currentPosition = player.currentPosition
                seekbarValueState = player.currentPosition.toFloat() / player.duration
            }
            delay(1000)
        }
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(25, 25, 30)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = {
                    medialPlayer.release()
                    onBackClick()
                }, modifier = Modifier
                    .size(50.dp)
                    .background(Color(25, 25, 30))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Dashboard Icon
            IconButton(
                onClick = {
                    isPlaying = false
                    medialPlayer.release()
                    onDashboardClick()
                }, modifier = Modifier
                    .size(50.dp)
                    .background(Color(25, 25, 30))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Search TextField
            val containerColor = Color(35, 35, 40)
            TextField(shape = RoundedCornerShape(40),
                value = searchText,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    disabledTextColor = Color.Transparent,
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = containerColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),

                onValueChange = { searchText = it },

                placeholder = {
                    Text(
                        text = "Search...",
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .height(50.dp)
                    .width(250.dp),
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // ChevronLeft Icon
                        Icon(imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.clickable {

                                if (dataList.isNotEmpty() && dataList != null && pos >= 0 && pos <= dataList.size - 1) {
                                    val n = dataList[pos]
                                    val seek =
                                        calculatePercentage(durationInSeconds, n).toInt() * 1000
                                    medialPlayer.seekTo(seek)
                                    pos -= 1
                                } else {
                                    pos = 0
                                }

                            })
                        // ChevronRight Icon
                        Icon(imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.clickable {
                                if (dataList.isNotEmpty() && dataList != null && pos >= 0 && pos <= dataList.size - 1) {
                                    val n = dataList[pos]
                                    val seek =
                                        calculatePercentage(durationInSeconds, n).toInt() * 1000
                                    medialPlayer.seekTo(seek)
                                    pos += 1
                                } else {
                                    pos = 0
                                }

                            })

                        // Search Icon
                        Icon(imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier
                                .clickable {
                                    sendFileNameToApi(context, fileName, searchText)
                                }
                                .padding(end = 15.dp))
                    }
                })
        }

        Spacer(modifier = Modifier.height(50.dp))

        // تصویر موزیک
        Image(
            painter = painterResource(id = R.drawable.music_image),
            contentDescription = null,
            modifier = Modifier
                .size(350.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .background(Color(35, 35, 40))
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            path.substring(path.lastIndexOf('/') + 1),
            color = Color.White,
            style = TextStyle(fontSize = 17.sp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        // دکمه پخش/توقف
        IconButton(
            onClick = {
                isPlaying = !isPlaying
                if (medialPlayer.isPlaying) {
                    medialPlayer.pause()
                } else {
                    medialPlayer.start()
                }
                // اجرای عملیات مرتبط با تغییر وضعیت پخش/توقف
            },
            modifier = Modifier
                .size(50.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SeekBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp, 16.dp, 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val reminded = medialPlayer.duration - currentPosition
            // مقادیر سمت چپ
            Text(
                String.format(
                    "%02d:%02d", currentPosition / 1000 / 60, currentPosition / 1000 % 60
                ), color = Color.White
            )
            Spacer(modifier = Modifier.width(5.dp))
            // اسلایدر موزیک
            Slider(
                value = seekbarValueState, onValueChange = {
                    seekbarValueState = it
                    medialPlayer.seekTo((it * medialPlayer.duration).toInt())
                    // اجرای عملیات مرتبط با تغییر مقدار seekbar
                }, modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(5.dp))
            // مقادیر سمت راست
            Text(
                String.format("-%02d:%02d", reminded / 1000 / 60, reminded / 1000 % 60),
                color = Color.White
            )
        }

        // آیکون‌های صدا
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // آیکون کم صدا
            Icon(
                tint = Color.White,
                imageVector = Icons.Default.VolumeDown,
                contentDescription = null,
                modifier = Modifier.size(23.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            // Slider صدا
            Slider(
                value = volumeValueState, onValueChange = {
                    if (it > volumeValueState) {
                        volumeValueState = it
                        audioManager.adjustVolume(
                            AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND
                        )
                    } else {
                        volumeValueState = it
                        audioManager.adjustVolume(
                            AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND
                        )
                    }
                    // اجرای عملیات مرتبط با تغییر مقدار صدا
                }, modifier = Modifier
                    .width(150.dp)  // تغییر عرض به 150 پیکسل
                    .fillMaxWidth()  // پر کردن عرض ممکن
            )
            Spacer(modifier = Modifier.width(5.dp))
            // آیکون زیاد صدا
            Icon(
                tint = Color.White,
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}


@Composable
fun InformationDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { onDismiss() }, title = { Text("Info") }, text = {
        Column {
            Text("Email:  masihdalfardiam@aut.ac.ir")
            Text("Version:  4.1.3")
            Text("Date:  January 09, 2024")
        }
    }, confirmButton = {
        TextButton(onClick = { onDismiss() }) {
            Text("OK")
        }
    })
}


@Preview(showBackground = true)
@Composable
fun TwoFrameAppPreview() {
    TwoFrameApp()
}


fun sendFileNameToApi(context: Context, filePath: String, word: String = "") {
    val token = "Token " + SharedPrefUtil.getToken(context)

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

    // Get the file name from the path
    val fileName = filePath.substring(filePath.lastIndexOf('/') + 1)

    // Build the request body
    val bodyBuilder =
        MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("sound", fileName)

    if (word.isNotEmpty()) {
        bodyBuilder.addFormDataPart("word", word)
    }

    val body = bodyBuilder.build()

    // Create the request
    val request = Request.Builder().url("https://api.speechfinder.ir/audio_sender").post(body)
        .addHeader("Authorization", token).build()

    // Execute the request
    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            // Handle the error
            e.printStackTrace()
            // Show a toast message for failure
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Failed to send file name", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResponse(call: okhttp3.Call, response: Response) {
            if (response.isSuccessful) {
                // Handle the successful response
                val responseBody = response.body?.string()
                Log.d("API Response", responseBody ?: "No response")

                val gson = Gson()
                val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                val ok = jsonObject.get("ok").asBoolean
                val message = jsonObject.get("message").asString

                if (ok) {
                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                        val data = jsonObject.getAsJsonObject("data")
                        if (data.has("numbers") && !data.get("numbers").isJsonNull) {
                            val numbersArray = data.getAsJsonArray("numbers")
                            numbersArray.forEach { element ->
                                dataList.add(element.asInt)
                            }

                        }
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Failed to send file name", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                // Handle the unsuccessful response
                Log.d("API Response", "Request failed with code: ${response.code}")
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Failed to send file name", Toast.LENGTH_SHORT).show()
                }
            }
        }
    })


}

fun getAudioFileDuration(context: Context, uri: Uri): Int? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMillis = durationString?.toInt()
        durationMillis?.div(1000) // Convert to seconds
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        retriever.release()
    }
}

fun calculatePercentage(value: Int, n: Int): Double {
    return (n / 100.0) * value
}


