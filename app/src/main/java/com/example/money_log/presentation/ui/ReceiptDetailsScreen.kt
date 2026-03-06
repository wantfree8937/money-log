package com.example.money_log.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.money_log.domain.model.Receipt
import com.example.money_log.ui.theme.*
import android.graphics.BitmapFactory
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailsScreen(
    receipt: Receipt,
    onSave: (Receipt) -> Unit,
    onRetake: () -> Unit,
    onBack: () -> Unit
) {
    var editedMerchant by remember { mutableStateOf(receipt.storeName) }
    var editedDate by remember { mutableStateOf(receipt.date) }
    var editedAmount by remember { mutableStateOf(receipt.amount.toString()) }
    var editedCategory by remember { mutableStateOf(receipt.category) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Receipt Image Preview
            val bitmap = remember(receipt.imagePath) {
                BitmapFactory.decodeFile(receipt.imagePath)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Receipt Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image Not Found")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Fields
            ReceiptInputField(
                label = "Merchant Name",
                value = editedMerchant,
                onValueChange = { editedMerchant = it },
                icon = Icons.Default.Store
            )
            
            ReceiptInputField(
                label = "Date",
                value = editedDate,
                onValueChange = { editedDate = it },
                icon = Icons.Default.CalendarToday
            )
            
            ReceiptInputField(
                label = "Total Amount",
                value = editedAmount,
                onValueChange = { editedAmount = it },
                prefix = "$ "
            )
            
            ReceiptInputField(
                label = "Category",
                value = editedCategory,
                onValueChange = { editedCategory = it },
                icon = Icons.Default.Category,
                isDropdown = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Button(
                onClick = { 
                    onSave(receipt.copy(
                        storeName = editedMerchant,
                        date = editedDate,
                        amount = editedAmount.toIntOrNull() ?: 0,
                        category = editedCategory
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainGreen)
            ) {
                Text("Save Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onRetake,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen, contentColor = PrimaryGreen)
            ) {
                Text("Retake Photo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector? = null,
    prefix: String? = null,
    isDropdown: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            label, 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SurfaceWhite,
                focusedContainerColor = SurfaceWhite,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MainGreen
            ),
            trailingIcon = {
                if (isDropdown) {
                    Icon(Icons.Default.ExpandMore, contentDescription = null, tint = MainGreen)
                } else if (icon != null) {
                    Icon(icon, contentDescription = null, tint = MainGreen)
                }
            },
            prefix = prefix?.let { { Text(it, fontWeight = FontWeight.Bold) } },
            singleLine = true
        )
    }
}
