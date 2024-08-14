package eywa.projectcodex.api

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ApiScreen(
        viewModel: ApiViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val text = when (state.response) {
        is DataState.Error -> (state.response as DataState.Error<Exception>).error.message ?: "error"
        DataState.Loading -> "Loading"
        is DataState.Success -> state.response.data()?.joinToString { it.name } ?: "null"
    }

    Text(
            text = text,
    )
}
