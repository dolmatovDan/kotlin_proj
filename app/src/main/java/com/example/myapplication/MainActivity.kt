package com.example.myapplication

import Rates
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CurrencyConverterScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allCurrencies = listOf(
        "USD - Доллар США", "EUR - Евро", "GBP - Фунт стерлингов",
        "JPY - Японская иена", "RUB - Российский рубль", "CNY - Китайский юань",
        "AUD - Австралийский доллар", "CAD - Канадский доллар", "CHF - Швейцарский франк",
        "HKD - Гонконгский доллар", "SGD - Сингапурский доллар", "KRW - Южнокорейская вона"
    )

    var currency1 by remember { mutableStateOf("") }
    var expanded1 by remember { mutableStateOf(false) }
    var currency2 by remember { mutableStateOf("") }
    var expanded2 by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentRate by remember { mutableStateOf<Double?>(null) }
    var currentSrcCurrency by remember { mutableStateOf("") }
    var currentDstCurrency by remember { mutableStateOf("") }
    var notification by remember { mutableStateOf("") }



    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            FileManager.saveToDownloads(context)
        } else {
            Toast.makeText(context, "Разрешение необходимо для сохранения файлов",
                Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAndSave() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Для Android 10+ разрешение не требуется
                FileManager.saveToDownloads(context)
            }
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                FileManager.saveToDownloads(context)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }


    fun addToHistory() {
        currentRate?.let { rate ->
            FileManager.addRateToHistory(currentSrcCurrency, currentDstCurrency, rate)
            notification = "Курс добавлен в историю"
            scope.launch {
                delay(3000)
                notification = ""
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
        Text(
            text = notification,
            color = if (notification.isNotEmpty()) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            textAlign = TextAlign.Center
        )

        // Первый список с поиском
        ExposedDropdownMenuBox(
            expanded = expanded1,
            onExpandedChange = { expanded1 = it }
        ) {
            TextField(
                value = currency1,
                onValueChange = {
                    currency1 = it
                    expanded1 = it.isNotEmpty()
                },
                label = { Text("Первая валюта") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded1,
                onDismissRequest = { expanded1 = false }
            ) {
                if (currency1.isEmpty()) {
                    allCurrencies.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                currency1 = item
                                expanded1 = false
                            }
                        )
                    }
                } else {
                    allCurrencies.filter { it.contains(currency1, ignoreCase = true) }
                        .forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    currency1 = item
                                    expanded1 = false
                                }
                            )
                        }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Второй список с поиском
        ExposedDropdownMenuBox(
            expanded = expanded2,
            onExpandedChange = { expanded2 = it }
        ) {
            TextField(
                value = currency2,
                onValueChange = {
                    currency2 = it
                    expanded2 = it.isNotEmpty()
                },
                label = { Text("Вторая валюта") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded2,
                onDismissRequest = { expanded2 = false }
            ) {
                if (currency2.isEmpty()) {
                    allCurrencies.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                currency2 = item
                                expanded2 = false
                            }
                        )
                    }
                } else {
                    allCurrencies.filter { it.contains(currency2, ignoreCase = true) }
                        .forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    currency2 = item
                                    expanded2 = false
                                }
                            )
                        }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Поле результата конвертации
        TextField(
            value = when {
                isLoading -> "Загрузка..."
                !error.isNullOrEmpty() -> error!!
                else -> result
            },
            onValueChange = {},
            readOnly = true,
            label = { Text("Результат конвертации") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка конвертации
        Button(
            onClick = {
                if (currency1.isEmpty() || currency2.isEmpty()) {
                    result = "Выберите обе валюты"
                    return@Button
                }

                val srcCurrency = currency1.split(" - ")[0]
                val dstCurrency = currency2.split(" - ")[0]

                if (srcCurrency == dstCurrency) {
                    result = "Валюты одинаковые"
                    return@Button
                }

                scope.launch {
                    try {
                        isLoading = true
                        error = null
                        val rate = getRate(srcCurrency, dstCurrency)
                        if (rate == null) {
                            throw IllegalArgumentException("введена неверная валюта")
                        }
                        currentRate = rate
                        currentSrcCurrency = srcCurrency
                        currentDstCurrency = dstCurrency
                        result = "1 $srcCurrency = %.4f $dstCurrency".format(rate)
                    } catch (e: IllegalArgumentException) {
                        error = "Ошибка: валюта не найдена"
                    } catch (e: Exception) {
                        error = "Ошибка: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Конвертация..." else "Конвертировать")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка добавления в историю
        Button(
            onClick = { addToHistory() },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentRate != null
        ) {
            Text("Добавить в историю")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка сохранения в файл
        Button(
            onClick = { checkAndSave() },
            modifier = Modifier.fillMaxWidth(),
            enabled = FileManager.hasSavedRates()
        ) {
            Text("Сохранить историю в файл")
        }
    }
}

private suspend fun getRate(src: String, dst: String): Double? {
    val rates = Rates()
    rates.loadRates()
    return rates.getExchangeRate(src, dst)
}

@Preview(showBackground = true)
@Composable
fun CurrencyConverterPreview() {
    MyApplicationTheme {
        CurrencyConverterScreen()
    }
}
