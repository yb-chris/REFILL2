//package com.example.refill
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.refill.ui.theme.ReFillTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            ReFillTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    ReFillTheme {
//        Greeting("Android")
//    }
//}

package com.example.refill

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("orders")

        setContent {
            MaterialTheme {
                MainScreen(
                    onOrderClick = { bottleSize, quantity ->
                        placeOrder(bottleSize, quantity)
                    },
                    onViewOrdersClick = {
                        Toast.makeText(this, "View Orders not implemented yet", Toast.LENGTH_SHORT).show()
                    },
                    onLogoutClick = {
                        auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    private fun placeOrder(bottleSize: String, quantity: Int) {
        if (quantity <= 0) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val pricePerUnit = getPricePerUnit(bottleSize)
        val totalPrice = quantity * pricePerUnit
        val orderId = UUID.randomUUID().toString()
        val userId = auth.currentUser?.uid ?: return
        val order = Order(orderId, userId, bottleSize, quantity, totalPrice, "Pending")

        database.child(orderId).setValue(order)
            .addOnSuccessListener {
                Toast.makeText(this, "Order placed successfully", Toast.LENGTH_SHORT).show()
                // TODO: Proceed to M-Pesa payment
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to place order: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getPricePerUnit(bottleSize: String): Double = when (bottleSize) {
        "500ml" -> 10.0
        "1L" -> 15.0
        "5L" -> 50.0
        else -> 0.0
    }
}

@Composable
fun MainScreen(
    onOrderClick: (String, Int) -> Unit,
    onViewOrdersClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var bottleSize by remember { mutableStateOf("500ml") }
    var quantity by remember { mutableStateOf("") }
    val bottleSizes = listOf("500ml", "1L", "5L")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Welcome to Refill",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { /* Handle dropdown toggle if needed */ }
        ) {
            TextField(
                value = bottleSize,
                onValueChange = { bottleSize = it },
                label = { Text("Bottle Size") },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = false,
                onDismissRequest = { /* Handle dismiss */ }
            ) {
                bottleSizes.forEach { size ->
                    DropdownMenuItem(
                        text = { Text(size) },
                        onClick = { bottleSize = size }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onOrderClick(bottleSize, quantity.toIntOrNull() ?: 0) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Place Order")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onViewOrdersClick() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Orders")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onLogoutClick() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}