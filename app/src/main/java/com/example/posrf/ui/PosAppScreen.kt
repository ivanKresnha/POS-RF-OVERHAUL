package com.example.posrf.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posrf.data.Product
import com.example.posrf.data.SalesReport
import com.example.posrf.data.StockTransactionDetail
import com.example.posrf.data.TransactionType
import com.example.posrf.ui.theme.StatusCritical
import com.example.posrf.ui.theme.StatusHealthy
import com.example.posrf.ui.theme.StatusWarning
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosAppScreen(viewModel: PosViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val todaySales by viewModel.todaySales.collectAsState()
    val weeklySales by viewModel.weeklySales.collectAsState()
    val monthlySales by viewModel.monthlySales.collectAsState()
    val history by viewModel.transactionHistory.collectAsState()

    var isSearchActive by remember { mutableStateOf(value = false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp
        val isSplitScreen = maxWidth >= 700.dp

        val tabs = listOf(
            Triple(0, Icons.Rounded.Storefront, "Jual"),
            Triple(1, Icons.Rounded.Inventory2, "Stok"),
            Triple(2, Icons.AutoMirrored.Rounded.ReceiptLong, "Riwayat"),
            Triple(3, Icons.Rounded.BarChart, "Laporan"),
        )

        if (isWideScreen) {
            // Adaptive Side Navigation Rail for Tablet / Foldable / Landscape displays
            Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp),
                        ) {
                            Icon(
                                Icons.Rounded.PointOfSale,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("POS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                ) {
                    tabs.forEach { (index, icon, label) ->
                        NavigationRailItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp)) },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    TopAppBar(
                        title = {
                            if (isSearchActive) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    placeholder = { Text("Cari barang...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { viewModel.setSearchQuery(""); isSearchActive = false }) {
                                            Icon(Icons.Default.Close, contentDescription = null)
                                        }
                                    },
                                )
                            } else {
                                Column {
                                    Text("POS OFFLINE", fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 0.5.sp)
                                    Text("Aman & Mandiri", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                        actions = {
                            if ((!isSearchActive) && ((selectedTab == 0) || (selectedTab == 1))) {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { viewModel.loadData() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.shadow(2.dp),
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (selectedTab) {
                            0 -> SalesScreen(products, cart, viewModel, isSplitScreen, searchQuery)
                            1 -> StockScreen(products, viewModel, searchQuery)
                            2 -> HistoryScreen(history)
                            3 -> ReportsScreen(todaySales, weeklySales, monthlySales)
                        }
                    }
                }
            }
        } else {
            // Mobile Compact Bottom Navigation Bar
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            if (isSearchActive) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    placeholder = { Text("Cari barang...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { viewModel.setSearchQuery(""); isSearchActive = false }) {
                                            Icon(Icons.Default.Close, contentDescription = null)
                                        }
                                    },
                                )
                            } else {
                                Column {
                                    Text("POS OFFLINE", fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 0.5.sp)
                                    Text("Aman & Mandiri", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                        actions = {
                            if ((!isSearchActive) && ((selectedTab == 0) || (selectedTab == 1))) {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { viewModel.loadData() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.shadow(2.dp),
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                    ) {
                        tabs.forEach { (index, icon, label) ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp)) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    when (selectedTab) {
                        0 -> SalesScreen(products, cart, viewModel, isSplitScreen = false, searchQuery = searchQuery)
                        1 -> StockScreen(products, viewModel, searchQuery = searchQuery)
                        2 -> HistoryScreen(history)
                        3 -> ReportsScreen(todaySales, weeklySales, monthlySales)
                    }
                }
            }
        }
    }
}

@Composable
fun SalesScreen(
    products: List<Product>,
    cart: Map<Product, Int>,
    viewModel: PosViewModel,
    isSplitScreen: Boolean,
    searchQuery: String,
) {
    var showCartView by remember { mutableStateOf(value = false) }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    if (isSplitScreen) {
        // Wide / Tablet Dual-Pane View: Products Grid on Left, Cart Panel on Right
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text("Katalog Barang", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))

                if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Barang tidak ditemukan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(filteredProducts) { product ->
                            ProductSalesCard(product = product) { viewModel.addToCart(product) }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.width(340.dp).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                CartContentPanel(cart = cart, viewModel = viewModel) {}
            }
        }
    } else {
        // Compact Mobile View: Toggle Sub-Tab Switcher
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(4.dp),
            ) {
                Button(
                    onClick = { showCartView = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!showCartView) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (!showCartView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = null,
                ) {
                    Icon(Icons.Rounded.GridView, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pilih Barang", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Button(
                    onClick = { showCartView = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showCartView) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (showCartView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = null,
                ) {
                    BadgedBox(
                        badge = {
                            if (cart.isNotEmpty()) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text(cart.values.sum().toString(), color = Color.White)
                                }
                            }
                        },
                    ) {
                        Icon(Icons.Rounded.ShoppingCart, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Keranjang", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            if (!showCartView) {
                if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Barang tidak ditemukan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) {
                        items(filteredProducts) { product ->
                            ProductSalesCard(product = product) { viewModel.addToCart(product) }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    CartContentPanel(cart = cart, viewModel = viewModel) { showCartView = false }
                }
            }
        }
    }
}

@Composable
fun ProductSalesCard(product: Product, onClick: () -> Unit) {
    val stockColor = when {
        product.currentStock <= 0 -> StatusCritical
        product.currentStock < 5 -> StatusWarning
        else -> StatusHealthy
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                )
                Box(
                    modifier = Modifier
                        .background(stockColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        if (product.currentStock <= 0) "Habis" else "Stok: ${product.currentStock}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = stockColor,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Rp ${formatPrice(product.sellingPrice)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun CartContentPanel(cart: Map<Product, Int>, viewModel: PosViewModel, onCheckoutDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text("Keranjang Belanja", fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))

        if (cart.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Keranjang masih kosong", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cart.toList()) { (product, qty) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(product.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Rp ${formatPrice(product.sellingPrice)} x $qty", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(
                            onClick = { viewModel.removeFromCart(product) },
                            modifier = Modifier.size(32.dp).background(Color(0xFF3B2424), CircleShape),
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(18.dp))
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }

            val total = cart.entries.sumOf { it.key.sellingPrice * it.value }
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Total Belanja", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Rp ${formatPrice(total)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }

            Button(
                onClick = {
                    viewModel.checkout()
                    onCheckoutDone()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("KONFIRMASI BAYAR", fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun StockScreen(products: List<Product>, viewModel: PosViewModel, searchQuery: String) {
    var showAddDialog by remember { mutableStateOf(value = false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var selectedProductForStock by remember { mutableStateOf<Product?>(null) }
    var stockAction by remember { mutableStateOf("") }

    val totalUnique by viewModel.totalUniqueProducts.collectAsState()
    val totalQty by viewModel.totalStockQuantity.collectAsState()
    val totalValue by viewModel.totalInventoryValue.collectAsState()

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Barang", modifier = Modifier.size(32.dp), tint = Color.White)
            }
        },
    ) { p ->
        Column(modifier = Modifier.padding(p).fillMaxSize().padding(16.dp)) {
            StockSummaryHeader(totalUnique, totalQty, totalValue)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Daftar Barang & Stok", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("${filteredProducts.size} item", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))

            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Belum ada barang terdaftar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    items(filteredProducts) { product ->
                        StockProductCard(
                            product = product,
                            onEdit = { editingProduct = product },
                            onStockIn = {
                                selectedProductForStock = product
                                stockAction = "IN"
                            },
                            onStockOut = {
                                selectedProductForStock = product
                                stockAction = "OUT"
                            },
                        ) { viewModel.deleteProduct(product) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
        ) { name, costPrice, sellingPrice, stock ->
            viewModel.addProduct(name, costPrice, sellingPrice, stock)
            showAddDialog = false
        }
    }

    if (editingProduct != null) {
        EditProductDialog(
            product = editingProduct!!,
            onDismiss = { editingProduct = null },
        ) { name, costPrice, sellingPrice, stock ->
            viewModel.editProduct(editingProduct!!, name, costPrice, sellingPrice, stock)
            editingProduct = null
        }
    }

    if (selectedProductForStock != null) {
        StockUpdateDialog(
            product = selectedProductForStock!!,
            action = stockAction,
            onDismiss = { selectedProductForStock = null },
        ) { qty ->
            if (stockAction == "IN") viewModel.stockIn(selectedProductForStock!!, qty)
            else viewModel.stockOut(selectedProductForStock!!, qty)
            selectedProductForStock = null
        }
    }
}

@Composable
fun StockProductCard(
    product: Product,
    onEdit: () -> Unit,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onDelete: () -> Unit,
) {
    val stockColor = when {
        product.currentStock <= 0 -> StatusCritical
        product.currentStock < 5 -> StatusWarning
        else -> StatusHealthy
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    product.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )

                Box(
                    modifier = Modifier
                        .background(stockColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        if (product.currentStock <= 0) "Habis" else "Stok: ${product.currentStock}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = stockColor,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Modal: Rp ${formatPrice(product.costPrice)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Jual: Rp ${formatPrice(product.sellingPrice)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("EDIT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = onStockIn,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("TAMBAH", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onStockOut,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("KURANG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("HAPUS BARANG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StockSummaryHeader(totalUnique: Int, totalQty: Int, totalValue: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ringkasan Inventaris Toko", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Jenis Barang", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(totalUnique.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Total Stok", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$totalQty pcs", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            Column {
                Text("Total Nilai Aset Modal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Rp ${formatPrice(totalValue)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, Int) -> Unit,
) {
    var name by remember { mutableStateOf(product.name) }
    var costPrice by remember { mutableStateOf(if (product.costPrice == 0.0) "" else product.costPrice.toString()) }
    var sellingPrice by remember { mutableStateOf(if (product.sellingPrice == 0.0) "" else product.sellingPrice.toString()) }
    var stock by remember { mutableStateOf(product.currentStock.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Barang", fontSize = 20.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Barang") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { costPrice = it },
                    label = { Text("Harga Modal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Harga Jual (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Jumlah Stok") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name,
                            costPrice.toDoubleOrNull() ?: 0.0,
                            sellingPrice.toDoubleOrNull() ?: 0.0,
                            stock.toIntOrNull() ?: 0,
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank(),
            ) {
                Text("SIMPAN PERUBAHAN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("BATAL") }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
fun AddProductDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Double, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Barang Baru", fontSize = 20.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Barang") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { costPrice = it },
                    label = { Text("Harga Modal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Harga Jual (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stok Awal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name,
                            costPrice.toDoubleOrNull() ?: 0.0,
                            sellingPrice.toDoubleOrNull() ?: 0.0,
                            stock.toIntOrNull() ?: 0,
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank(),
            ) {
                Text("SIMPAN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("BATAL") }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
fun StockUpdateDialog(product: Product, action: String, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var qty by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (action == "IN") "Restok: ${product.name}" else "Kurangi: ${product.name}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = qty,
                    onValueChange = {
                        qty = it
                        errorMessage = null
                    },
                    label = { Text("Jumlah (pcs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    isError = errorMessage != null,
                )
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val inputQty = qty.toIntOrNull() ?: 0
                    if ((action == "OUT") && ((product.currentStock - inputQty) < 0)) {
                        errorMessage = "Gagal: Stok tidak boleh kurang dari 0!"
                    } else {
                        onConfirm(inputQty)
                    }
                },
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("BATAL") }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
fun HistoryScreen(history: List<StockTransactionDetail>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Riwayat Transaksi", fontSize = 24.sp, fontWeight = FontWeight.Black)
        }

        if (history.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat aktivitas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(history) { detail ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    val pair = when (detail.transaction.type) {
                        TransactionType.IN -> Icons.Rounded.ArrowCircleUp to Color(0xFF34D399)
                        TransactionType.SELL -> Icons.Rounded.ShoppingBag to Color(0xFFF87171)
                        TransactionType.OUT -> Icons.Rounded.ChangeCircle to Color(0xFFFBBF24)
                    }
                    val icon = pair.first
                    val tint = pair.second

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(tint.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(detail.productName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(formatDateTime(detail.transaction.timestamp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            when (detail.transaction.type) {
                                TransactionType.IN -> "Masuk"
                                TransactionType.SELL -> "Terjual"
                                TransactionType.OUT -> "Keluar"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = tint,
                        )
                        Text(
                            "${if (detail.transaction.type == TransactionType.IN) "+" else "-"}${detail.transaction.quantity}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(today: SalesReport, weekly: SalesReport, monthly: SalesReport) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Laporan & Visualisasi Sales", fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        item {
            SalesPieChart(today = today, weekly = weekly, monthly = monthly)
        }
        item {
            SalesBarChart(today = today, weekly = weekly, monthly = monthly)
        }
        item {
            Text("Ringkasan Pendapatan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        item { ReportCard("HARI INI", today, Icons.Rounded.Today, MaterialTheme.colorScheme.primary) }
        item { ReportCard("MINGGU INI", weekly, Icons.Rounded.DateRange, Color(0xFF818CF8)) }
        item { ReportCard("BULAN INI", monthly, Icons.Rounded.CalendarMonth, Color(0xFFA78BFA)) }
    }
}

@Composable
fun SalesPieChart(
    today: SalesReport,
    weekly: SalesReport,
    monthly: SalesReport,
) {
    val totalRevenue = today.totalRevenue + weekly.totalRevenue + monthly.totalRevenue

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Diagram Distribusi Omset (Pie Chart)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(Icons.Rounded.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(16.dp))

            if (totalRevenue <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Belum ada data omset penjualan.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
            } else {
                val todayAngle = ((today.totalRevenue / totalRevenue) * 360f).toFloat()
                val weeklyAngle = ((weekly.totalRevenue / totalRevenue) * 360f).toFloat()
                val monthlyAngle = ((monthly.totalRevenue / totalRevenue) * 360f).toFloat()

                val colorToday = MaterialTheme.colorScheme.primary
                val colorWeekly = Color(0xFF818CF8)
                val colorMonthly = Color(0xFFA78BFA)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(180.dp),
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 28.dp.toPx()
                        var startAngle = -90f

                        if (todayAngle > 0f) {
                            drawArc(
                                color = colorToday,
                                startAngle = startAngle,
                                sweepAngle = todayAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            )
                            startAngle += todayAngle
                        }

                        if (weeklyAngle > 0f) {
                            drawArc(
                                color = colorWeekly,
                                startAngle = startAngle,
                                sweepAngle = weeklyAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            )
                            startAngle += weeklyAngle
                        }

                        if (monthlyAngle > 0f) {
                            drawArc(
                                color = colorMonthly,
                                startAngle = startAngle,
                                sweepAngle = monthlyAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Omset", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "Rp ${formatPrice(totalRevenue)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    PieChartLegendItem(
                        label = "Hari Ini",
                        percentage = if (totalRevenue > 0) ((today.totalRevenue / totalRevenue) * 100).toInt() else 0,
                        color = colorToday,
                    )
                    PieChartLegendItem(
                        label = "Minggu Ini",
                        percentage = if (totalRevenue > 0) ((weekly.totalRevenue / totalRevenue) * 100).toInt() else 0,
                        color = colorWeekly,
                    )
                    PieChartLegendItem(
                        label = "Bulan Ini",
                        percentage = if (totalRevenue > 0) ((monthly.totalRevenue / totalRevenue) * 100).toInt() else 0,
                        color = colorMonthly,
                    )
                }
            }
        }
    }
}

@Composable
fun PieChartLegendItem(label: String, percentage: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$percentage%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun SalesBarChart(
    today: SalesReport,
    weekly: SalesReport,
    monthly: SalesReport,
) {
    val maxItems = maxOf(today.itemsSold, weekly.itemsSold, monthly.itemsSold, 1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Diagram Perbandingan Item Terjual",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(Icons.Rounded.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(16.dp))

            val totalItems = today.itemsSold + weekly.itemsSold + monthly.itemsSold
            if (totalItems <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Belum ada data item terjual.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
            } else {
                val barData = listOf(
                    Triple("Hari Ini", today.itemsSold, MaterialTheme.colorScheme.primary),
                    Triple("Minggu Ini", weekly.itemsSold, Color(0xFF818CF8)),
                    Triple("Bulan Ini", monthly.itemsSold, Color(0xFFA78BFA)),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    barData.forEach { (label, items, color) ->
                        val barHeightFactor = if (items > 0) (items.toFloat() / maxItems.toFloat()).coerceIn(0.12f, 1.0f) else 0.05f

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                "$items pcs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = color,
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .height(90.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .fillMaxHeight(barHeightFactor)
                                        .background(color, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                label,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportCard(title: String, report: SalesReport, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                Text("Rp ${formatPrice(report.totalRevenue)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Text("${report.itemsSold} item terjual", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

fun formatPrice(amount: Double): String {
    val formatter = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"))
    return formatter.format(amount)
}

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("id-ID"))
    return sdf.format(Date(timestamp))
}
