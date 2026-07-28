package com.aura.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val hazeState = remember { HazeState() } 

            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                // DUMMY PHOTOS GRID
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(state = hazeState),
                    contentPadding = PaddingValues(bottom = 90.dp) 
                ) {
                    items(100) { index ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .background(Color.DarkGray)
                        )
                    }
                }

                // LIQUID GLASS BOTTOM BAR
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth()
                        .height(70.dp)
                        .hazeChild(
                            state = hazeState,
                            shape = RoundedCornerShape(35.dp),
                            style = HazeStyle(
                                tint = Color.White.copy(alpha = 0.15f),
                                blurRadius = 30.dp,
                                noiseFactor = 0.05f 
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Image, contentDescription = "Photos", tint = Color.White)
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Lock, contentDescription = "Vault", tint = Color.White)
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
