package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseRepository
import com.example.data.Budget
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.data.User
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirebaseRepository()
    private val sharedPrefs = application.getSharedPreferences("pocket_money_prefs", android.content.Context.MODE_PRIVATE)

    // Auth States
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    
    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()

    // Currency Selection
    private val _currency = MutableStateFlow(sharedPrefs.getString("currency", "USD") ?: "USD")
    val currency = _currency.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAuthUserFlow().collect { firebaseUser ->
                if (firebaseUser != null) {
                    _currentUserId.value = firebaseUser.uid
                    val profile = repository.getUserProfile(firebaseUser.uid)
                    
                    val fallbackName = firebaseUser.email?.substringBefore("@")?.split(Regex("[._]"))?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } ?: "User"
                    val nameFromAuth = if (firebaseUser.displayName.isNullOrBlank()) fallbackName else firebaseUser.displayName
                    
                    if (profile != null) {
                        val finalName = if (profile.name == "User" || profile.name.isBlank()) nameFromAuth else profile.name
                        _currentUserProfile.value = profile.copy(name = finalName!!)
                    } else {
                        _currentUserProfile.value = User(
                            userId = firebaseUser.uid,
                            name = nameFromAuth!!,
                            email = firebaseUser.email ?: ""
                        )
                    }
                    _authState.value = AuthState.Authenticated
                } else {
                    _currentUserId.value = null
                    _currentUserProfile.value = null
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun login(email: String, pass: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        _authError.value = null
        val result = repository.login(email, pass)
        if (result.isFailure) {
            _authState.value = AuthState.Unauthenticated
            _authError.value = result.exceptionOrNull()?.message ?: "Login failed"
        } else {
            refreshUserProfile()
        }
    }

    fun signup(name: String, email: String, pass: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        _authError.value = null
        val result = repository.signup(name, email, pass)
        if (result.isFailure) {
            _authState.value = AuthState.Unauthenticated
            _authError.value = result.exceptionOrNull()?.message ?: "Signup failed"
        } else {
            refreshUserProfile()
        }
    }

    fun loginWithGoogle(idToken: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        _authError.value = null
        val result = repository.loginWithGoogle(idToken)
        if (result.isFailure) {
            _authState.value = AuthState.Unauthenticated
            _authError.value = result.exceptionOrNull()?.message ?: "Google Sign-In failed"
        } else {
            refreshUserProfile()
        }
    }

    private fun refreshUserProfile() = viewModelScope.launch {
        val uid = repository.currentUserId
        if (uid != null) {
            _currentUserId.value = uid
            val profile = repository.getUserProfile(uid)
            val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            
            if (profile != null) {
                val nameFromAuth = authUser?.let { 
                    val fallback = it.email?.substringBefore("@")?.split(Regex("[._]"))?.joinToString(" ") { p -> p.replaceFirstChar { c -> c.uppercase() } } ?: "User"
                    if (it.displayName.isNullOrBlank()) fallback else it.displayName
                } ?: "User"
                
                val finalName = if (profile.name == "User" || profile.name.isBlank()) nameFromAuth else profile.name
                _currentUserProfile.value = profile.copy(name = finalName!!)
            } else if (authUser != null) {
                val fallbackName = authUser.email?.substringBefore("@")?.split(Regex("[._]"))?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } ?: "User"
                val name = if (authUser.displayName.isNullOrBlank()) fallbackName else authUser.displayName
                _currentUserProfile.value = User(
                    userId = authUser.uid,
                    name = name!!,
                    email = authUser.email ?: ""
                )
            }
            _authState.value = AuthState.Authenticated
        }
    }

    fun logout() {
        _authState.value = AuthState.Loading
        repository.logout()
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        val result = repository.resetPassword(email)
        if (result.isFailure) {
            _authError.value = result.exceptionOrNull()?.message ?: "Reset failed"
        } else {
            _authError.value = "Password reset email sent."
        }
    }

    fun clearError() {
        _authError.value = null
    }

    fun setError(error: String) {
        _authError.value = error
    }

    fun setCurrency(curr: String) {
        _currency.value = curr
        sharedPrefs.edit().putString("currency", curr).apply()
    }

    fun formatAmount(amount: Double): String {
        val symbol = if (currency.value == "INR") "₹" else "$"
        return "$symbol${String.format("%.2f", amount)}"
    }

    fun formatAmountNoDecimals(amount: Double): String {
        val symbol = if (currency.value == "INR") "₹" else "$"
        return "$symbol${String.format("%.0f", amount)}"
    }

    // Data Flows
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _currentUserId
        .flatMapLatest { uid ->
            if (uid != null) {
                repository.getTransactions(uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val budgets: StateFlow<List<Budget>> = _currentUserId
        .flatMapLatest { uid ->
            if (uid != null) {
                repository.getBudgets(uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recurringTransactions: StateFlow<List<com.example.data.RecurringTransaction>> = _currentUserId
        .flatMapLatest { uid ->
            if (uid != null) {
                repository.getRecurringTransactions(uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            recurringTransactions.collect { list ->
                processRecurringTransactions(list)
            }
        }
    }

    private suspend fun processRecurringTransactions(list: List<com.example.data.RecurringTransaction>) {
        val now = Date()
        for (rtx in list) {
            var nextDate = rtx.nextProcessingDate.toDate()
            var modified = false
            while (nextDate.before(now)) {
                // Create transaction
                repository.addTransaction(Transaction(
                    amount = rtx.amount,
                    type = rtx.type,
                    category = rtx.category,
                    note = rtx.note + " (Auto)",
                    paymentMethod = rtx.paymentMethod,
                    timestamp = Timestamp(nextDate)
                ))
                
                // Calculate next date
                val cal = Calendar.getInstance().apply { time = nextDate }
                when (rtx.interval) {
                    "Daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                    "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                    "Monthly" -> cal.add(Calendar.MONTH, 1)
                    "Yearly" -> cal.add(Calendar.YEAR, 1)
                    else -> cal.add(Calendar.MONTH, 1)
                }
                nextDate = cal.time
                modified = true
            }
            if (modified) {
                repository.updateRecurringTransaction(rtx.copy(nextProcessingDate = Timestamp(nextDate)))
            }
        }
    }

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val notifications = recurringTransactions.map { list ->
        val now = Date()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 3) // 3 days ahead
        val threshold = cal.time

        list.filter { rtx ->
            val nextDate = rtx.nextProcessingDate.toDate()
            nextDate.after(now) && nextDate.before(threshold)
        }.map { rtx ->
            val symbol = if (_currency.value == "INR") "₹" else "$"
            val amountStr = "$symbol${String.format("%.2f", rtx.amount)}"
            val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            "Upcoming payment for ${rtx.category}: $amountStr on ${sdf.format(rtx.nextProcessingDate.toDate())}"
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory = _filterCategory.asStateFlow()
    
    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType = _filterType.asStateFlow()

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setFilterCategory(c: String?) { _filterCategory.value = c }
    fun setFilterType(t: TransactionType?) { _filterType.value = t }

    val filteredTransactions = combine(transactions, _searchQuery, _filterCategory, _filterType) { list, query, category, type ->
        list.filter { tx ->
            val matchesQuery = if (query.isBlank()) true else tx.note.contains(query, ignoreCase = true)
            val matchesCategory = if (category == null || category == "All") true else tx.category == category
            val matchesType = if (type == null) true else tx.type == type
            matchesQuery && matchesCategory && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Actions
    fun addTransaction(amount: Double, type: TransactionType, category: String, note: String, paymentMethod: String, date: Date) = viewModelScope.launch {
        repository.addTransaction(Transaction(amount = amount, type = type, category = category, note = note, paymentMethod = paymentMethod, timestamp = Timestamp(date)))
    }
    
    fun updateTransaction(tx: Transaction) = viewModelScope.launch {
        repository.updateTransaction(tx)
    }
    
    fun deleteTransaction(txId: String) = viewModelScope.launch {
        repository.deleteTransaction(txId)
    }

    fun deleteAllTransactions() = viewModelScope.launch {
        repository.deleteAllTransactions()
    }

    fun addBudget(category: String, amountLimit: Double) = viewModelScope.launch {
        repository.addBudget(Budget(category = category, amountLimit = amountLimit))
    }
    
    fun deleteBudget(budgetId: String) = viewModelScope.launch {
        repository.deleteBudget(budgetId)
    }

    fun addRecurringTransaction(amount: Double, type: TransactionType, category: String, note: String, paymentMethod: String, interval: String, nextProcDate: Date) {
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    amount = amount,
                    type = type,
                    category = category,
                    note = note,
                    paymentMethod = paymentMethod,
                    timestamp = Timestamp(nextProcDate)
                )
            )
        }
        
        viewModelScope.launch {
            val cal = java.util.Calendar.getInstance()
            cal.time = nextProcDate
            when (interval) {
                "Monthly" -> cal.add(java.util.Calendar.MONTH, 1)
                "Yearly" -> cal.add(java.util.Calendar.YEAR, 1)
                else -> cal.add(java.util.Calendar.MONTH, 1)
            }
            
            repository.addRecurringTransaction(
                com.example.data.RecurringTransaction(
                    amount = amount,
                    type = type,
                    category = category,
                    note = note,
                    paymentMethod = paymentMethod,
                    interval = interval,
                    nextProcessingDate = Timestamp(cal.time)
                )
            )
        }
    }

    fun deleteRecurringTransaction(recurringId: String) = viewModelScope.launch {
        repository.deleteRecurringTransaction(recurringId)
    }

    fun updateRecurringTransaction(rtx: com.example.data.RecurringTransaction) = viewModelScope.launch {
        repository.updateRecurringTransaction(rtx)
    }

    fun getExportDataString(txList: List<Transaction>): String {
        val header = "Date,Type,Category,Payment Method,Amount,Note\n"
        val rows = txList.joinToString("\n") { tx ->
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(tx.timestamp.toDate())
            "$dateStr,${tx.type},${tx.category},${tx.paymentMethod},${tx.amount},${tx.note.replace(",", " ")}"
        }
        return header + rows
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}
