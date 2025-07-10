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
import java.math.BigDecimal

data class Product(
    val id: Long? = null,
    val qrCode: String,
    val productName: String,
    val description: String? = null,
    val price: BigDecimal,
    val itemsInStock: Int = 0,
    val itemsOrdered: Int = 0
)

interface ApiService {
    @GET("api/products/qr/{qrCode}")
    suspend fun getProductByQrCode(@Path("qrCode") qrCode: String): Product?

    @POST("api/products")
    suspend fun addProduct(@Body product: Product): Product
}

class MainActivity : ComponentActivity() {

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.192:8080/") // <-- IP do serwisu springboot
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
                var isLoading by remember { mutableStateOf(false) }

                LaunchedEffect(scannedData) {
                    scannedData?.let { qrCode ->
                        isLoading = true
                        try {
                            productInfo = api.getProductByQrCode(qrCode)
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Blad sieci: ${e.message}", Toast.LENGTH_SHORT).show()
                            productInfo = null
                        } finally {
                            isLoading = false
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { startScan() },
                        enabled = !isLoading
                    ) {
                        Text("Skanuj kod QR")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isLoading) {
                        CircularProgressIndicator()
                    }

                    scannedData?.let { qrCode ->
                        Text("Zeskanowano produkt: $qrCode")
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        productInfo?.let { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Znaleziono produkt:",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("Nazwa: ${product.productName}")
                                    Text("Cena: $${product.price}")
                                    Text("Posiadane: ${product.itemsInStock}")
                                    Text("Zamowione: ${product.itemsOrdered}")
                                    product.description?.let { desc ->
                                        Text("Opis: $desc")
                                    }
                                }
                            }
                        } ?: run {
                            if (!isLoading) {
                                Text("Nie znaleziono produktu")
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                val newProduct = Product(
                                                    qrCode = qrCode,
                                                    productName = "Nowy produkt - $qrCode",
                                                    description = "Stworzono produkt",
                                                    price = BigDecimal("0.00"),
                                                    itemsInStock = 0,
                                                    itemsOrdered = 0
                                                )
                                                productInfo = api.addProduct(newProduct)
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Pomyslnie stworzono produkt!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Bład przy tworzeniu produktu: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                ) {
                                    Text("Dodaj nowy produkt")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(onClick = { finish() }) {
                        Text("Wyjdz z aplikacji")
                    }
                }
            }
        }
    }

    private fun startScan() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Zeskanuj kod qr")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val scannedQrCode = result.contents
            handleScannedCode(scannedQrCode)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleScannedCode(scannedQrCode: String) {
        setContent {
            SkannerqrTheme {
                var scannedData by remember { mutableStateOf(scannedQrCode) }
                var productInfo by remember { mutableStateOf<Product?>(null) }
                var isLoading by remember { mutableStateOf(false) }

                // Trigger API call when component is created
                LaunchedEffect(Unit) {
                    isLoading = true
                    try {
                        productInfo = api.getProductByQrCode(scannedQrCode)
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Blad sieci: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Zeskanowano kod QR: $scannedQrCode")
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        productInfo?.let { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Informacje o produkcie:",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("Nazwa: ${product.productName}")
                                    Text("cena: $${product.price}")
                                    Text("Posiadane: ${product.itemsInStock}")
                                    Text("Zamowione: ${product.itemsOrdered}")
                                    product.description?.let { desc ->
                                        Text("Opis: $desc")
                                    }
                                }
                            }
                        } ?: run {
                            Text("Nie znaleziono produktu")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(onClick = { 
                        // Reset to main screen
                        onCreate(null)
                    }) {
                        Text("Zeskanuj kolejne")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
