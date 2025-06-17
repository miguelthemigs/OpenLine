import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openline.viewmodel.UsersViewModel

@Composable
fun UserName(
    userId: String,
    modifier: Modifier = Modifier
) {
    val viewModel: UsersViewModel = viewModel()
    var name by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        name = viewModel.fetchUserName(userId) ?: "Unknown"
    }

    Text(text = name ?: "Loading…", modifier = modifier)
}
