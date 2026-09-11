package com.vmeduri.fintrack.ui.addedit

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vmeduri.fintrack.R
import com.vmeduri.fintrack.data.Category
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@Composable
fun AddEditTransactionScreen(
    transactionId: Long?,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AddEditViewModel = viewModel(
        factory = AddEditViewModelFactory(application, transactionId)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (transactionId == null) {
                            stringResource(R.string.add_transaction)
                        } else {
                            stringResource(R.string.edit_transaction)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (transactionId != null) {
                        IconButton(onClick = { viewModel.delete(onDone) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = viewModel.amountText,
                        onValueChange = viewModel::onAmountChange,
                        label = { Text("Amount (R)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = viewModel.saveError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val error = viewModel.saveError
                    if (error != null) {
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Column {
                    Text("Category", style = MaterialTheme.typography.titleSmall)
                    Category.entries.forEach { category ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onCategoryChange(category) }
                        ) {
                            RadioButton(
                                selected = viewModel.selectedCategory == category,
                                onClick = { viewModel.onCategoryChange(category) }
                            )
                            Text(category.displayName)
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.note,
                    onValueChange = viewModel::onNoteChange,
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Date: ${viewModel.date.format(dateFormatter)}", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.onDateChange(viewModel.date.minusDays(1)) }) {
                            Text("- 1 day")
                        }
                        OutlinedButton(onClick = { viewModel.onDateChange(LocalDate.now()) }) {
                            Text("Today")
                        }
                        OutlinedButton(onClick = { viewModel.onDateChange(viewModel.date.plusDays(1)) }) {
                            Text("+ 1 day")
                        }
                    }
                }

                Button(onClick = { viewModel.save(onDone) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Save")
                }
            }
        }
    }
}
