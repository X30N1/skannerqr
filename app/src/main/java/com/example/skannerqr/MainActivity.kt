package com.example.skannerqr

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.zxing.integration.android.IntentIntegrator
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.example.skannerqr.ui.theme.SkannerqrTheme

data class Product(val id: String, val name: String, val status: String)

interface ApiService {
    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: String): Product?

    @POST("api/products")
    suspend fun addProduct(@Body product: Product): Product
}

class MainActivity : ComponentActivity() {

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.192:8080/") // <-- Adjust as needed
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SkannerqrTheme {
                var scannedData by remember { mutableStateOf<String?>(null) }
                var productInfo by remember { mutableStateOf<Product?>(null) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { startScan() }) {
                        Text("Scan QR Code")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    scannedData?.let {
                        Text("Scanned: $it")
                        productInfo?.let { product ->
                            Text("Product Found: ${product.name} - ${product.status}")
                        } ?: run {
                            Button(onClick = {
                                coroutineScope.launch {
                                    val newProduct = Product(it, "New Product", "IN_STOCK")
                                    productInfo = api.addProduct(newProduct)
                                }
                            }) {
                                Text("Add Product")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(onClick = { finish() }) {
                        Text("Exit App")
                    }
                }
            }
        }
    }

    private fun startScan() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR Code")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val scannedId = result.contents
            handleScannedCode(scannedId)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleScannedCode(scannedId: String) {
        setContent {
            var scannedData by remember { mutableStateOf(scannedId) }
            var productInfo by remember { mutableStateOf<Product?>(null) }

            SkannerqrTheme {
                coroutineScope.launch {
                    try {
                        productInfo = api.getProduct(scannedId)
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Network error", Toast.LENGTH_SHORT).show()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Scanned: $scannedId")
                    productInfo?.let {
                        Text("Product: ${it.name}, Status: ${it.status}")
                    } ?: Text("Product not found.")
                }
            }
        }
    }
}
