package com.alex.controle_riscos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TelaPrincipal : AppCompatActivity() {
    // Declaração das variáveis para os elementos da interface
    private lateinit var descricaoEditText: EditText
    private lateinit var nomecompletoEditText: EditText
    private lateinit var setorEditText: EditText
    private lateinit var adclocButton: Button
    private lateinit var buttonvoltar: Button
    private lateinit var adcimgButton: Button
    private lateinit var enviarButton: Button
    private lateinit var imageView: ImageView

    // Variáveis para gerenciamento de localização
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null

    // Códigos de requisição para permissões e seleção de imagem (camelCase)
    private val requestCodePermissions = 101
    private val requestCodeLocationPermission = 102

    // Atividade para seleção de imagem
    private lateinit var pickImage: ActivityResultLauncher<Intent>

    // Referência ao Firebase Database
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_principal)

        // Inicializa o seletor de imagens
        pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    imageView.setImageURI(uri)
                }
            }
        }

        // Inicializa o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializa o Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Vincula os elementos da interface às variáveis
        descricaoEditText = findViewById(R.id.descricao)
        nomecompletoEditText = findViewById(R.id.nome_completo)
        setorEditText = findViewById(R.id.setor)
        enviarButton = findViewById(R.id.button)
        adcimgButton = findViewById(R.id.adcimgButton)
        adclocButton = findViewById(R.id.button2)
        imageView = findViewById(R.id.imageView)
        buttonvoltar = findViewById(R.id.button3)

        buttonvoltar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configura o listener para o botão de adicionar imagem
        adcimgButton.setOnClickListener {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCodePermissions)
            } else {
                openImagePicker()
            }
        }

        // Configura o listener para o botão de adicionar localização
        adclocButton.setOnClickListener {
            checkLocationPermissionAndGetLocation()
            currentLocation?.let { location ->
                Toast.makeText(this, "Localização: ${location.latitude}, ${location.longitude}", Toast.LENGTH_LONG).show()
            } ?: run {
                Toast.makeText(this, "Nenhuma localização foi adicionada", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura o listener para o botão de enviar
        enviarButton.setOnClickListener {
            val dados = mapOf(
                "descricao" to descricaoEditText.text.toString(),
                "nomeCompleto" to nomecompletoEditText.text.toString(),
                "setor" to setorEditText.text.toString(),
                "latitude" to currentLocation?.latitude,
                "longitude" to currentLocation?.longitude
            )

            database.child("dados").push().setValue(dados)
                .addOnSuccessListener {
                    Toast.makeText(this, "Dados enviados com sucesso!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Falha ao enviar dados: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCodeLocationPermission
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.lastLocation
                Toast.makeText(
                    this@TelaPrincipal,
                    "Localização obtida: ${currentLocation?.latitude}, ${currentLocation?.longitude}",
                    Toast.LENGTH_LONG
                ).show()
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            requestCodePermissions -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    Toast.makeText(this, "Permissão negada para acessar a galeria", Toast.LENGTH_SHORT).show()
                }
            }
            requestCodeLocationPermission -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Permissão negada para acessar a localização", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}