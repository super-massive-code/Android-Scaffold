package com.example.scaffold.ui.feature.postlist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scaffold.R
import com.example.scaffold.model.Post
import com.example.scaffold.ui.components.ErrorState
import com.example.scaffold.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    onPostClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_posts)) }) },
    ) { padding ->
        when (val state = uiState) {
            PostListUiState.Loading -> LoadingIndicator(Modifier.padding(padding))
            is PostListUiState.Error ->
                ErrorState(
                    message = state.message ?: stringResource(R.string.post_list_error_fallback),
                    onRetry = viewModel::refresh,
                    modifier = Modifier.padding(padding),
                )
            is PostListUiState.Content ->
                PostList(
                    posts = state.posts,
                    onPostClick = onPostClick,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun PostList(
    posts: List<Post>,
    onPostClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        items(items = posts, key = { it.id }) { post ->
            PostRow(post = post, onClick = { onPostClick(post.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostRow(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier =
            modifier
                .padding(vertical = 6.dp)
                .fillMaxWidth(),
    ) {
        Text(
            text = post.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(16.dp),
        )
    }
}
