package net.tactware.worldweaver.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.World
import net.tactware.worldweaver.ui.components.FeatureEmptyState
import net.tactware.worldweaver.ui.components.FeatureErrorState
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun HomeScreen(
    viewState: HomeViewState,
    onInteraction: (HomeInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(HomeInteraction.ScreenStarted)
    }

    when (viewState) {
        HomeViewState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        is HomeViewState.Error -> {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(HomeInteraction.RetrySelected) },
                )
            }
        }
        is HomeViewState.Empty -> HomeEmpty(
            state = viewState,
            onInteraction = onInteraction,
        )
        is HomeViewState.Content -> HomeContent(
            state = viewState,
            onInteraction = onInteraction,
        )
    }
}

@Composable
private fun HomeGreeting(displayName: String) {
    Column {
        Text(
            text = "Welcome back, $displayName",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Continue weaving your worlds",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun HomeEmpty(
    state: HomeViewState.Empty,
    onInteraction: (HomeInteraction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { HomeGreeting(state.displayName) }
        item {
            FeatureEmptyState(
                icon = Icons.Default.Public,
                title = "No recent worlds",
                message = "Create a world to start building places, people, and stories.",
                actionLabel = "New world",
                onAction = { onInteraction(HomeInteraction.NewWorldSelected) },
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeViewState.Content,
    onInteraction: (HomeInteraction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { HomeGreeting(state.displayName) }

        if (state.continueCampaign != null) {
            item {
                ContinueCard(
                    continueCampaign = state.continueCampaign,
                    onInteraction = onInteraction,
                )
            }
        }

        item {
            Text(
                text = "Recent worlds",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        items(state.recentWorlds, key = { it.id }) { world ->
            RecentWorldCard(
                world = world,
                onSelected = { onInteraction(HomeInteraction.WorldSelected(world.id)) },
            )
        }
    }
}

@Composable
private fun ContinueCard(
    continueCampaign: HomeViewState.ContinueCampaign,
    onInteraction: (HomeInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = continueCampaign.campaignName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = continueCampaign.worldName,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            Button(
                onClick = { onInteraction(HomeInteraction.ContinueCampaignSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text("Open campaign")
            }
        }
    }
}

@Composable
private fun RecentWorldCard(
    world: World,
    onSelected: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelected),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = world.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (world.description.isNotBlank()) {
                Text(
                    text = world.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
