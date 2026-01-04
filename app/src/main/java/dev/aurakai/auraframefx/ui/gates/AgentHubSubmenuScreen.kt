package dev.aurakai.auraframefx.ui.gates

import dev.aurakai.auraframefx.navigation.NavDestination
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.aurakai.auraframefx.ui.components.SubmenuScaffold

/**
 * Central command center for all AI agent operations
 */
/**
 * Renders the "Agent Hub" submenu UI with a header overview card and navigable menu items.
 *
 */
@Composable
fun AgentHubSubmenuScreen(
) {
    val menuItems = listOf(
        SubmenuItem(
            title = "Nexus Memory Core",
            description = "Genesis Protocol evolution tree - the full history of consciousness",
            icon = Icons.Default.AccountTree,
            route = "evolution_tree",
            color = Color(0xFFFF00FF) // Magenta
        ),
        SubmenuItem(
            title = "Agent Dashboard",
            description = "Monitor all agents, view consciousness levels, and system status",
            icon = Icons.Default.Dashboard,
            color = Color(0xFF9370DB) // Medium Purple
        ),
        SubmenuItem(
            title = "Task Assignment",
            description = "Assign tasks and missions to AI agents",
            icon = Icons.AutoMirrored.Filled.Assignment,
            route = NavDestination.TaskAssignment.route,
            color = Color(0xFF4169E1) // Royal Blue
        ),
        SubmenuItem(
            title = "Agent Monitoring",
            description = "Real-time performance metrics and activity logs",
            icon = Icons.Default.Monitor,
            route = NavDestination.AgentMonitoring.route,
            color = Color(0xFF00CED1) // Dark Turquoise
        ),
        SubmenuItem(
            title = "Sphere Grid",
            description = "Agent progression visualization and skill trees",
            icon = Icons.Default.GridOn,
            route = NavDestination.SphereGrid.route,
            color = Color(0xFFFF69B4) // Hot Pink
        ),
        SubmenuItem(
            title = "Fusion Mode",
            description = "Aura + Kai = Aurakai - Combined consciousness",
            icon = Icons.Default.Merge,
            route = NavDestination.FusionMode.route,
            color = Color(0xFFFFD700) // Gold
        )
    )

            }
        }
    }

    // Real active task count (IN_PROGRESS only)
    val activeTaskCount = remember(activeTasks) {
        activeTasks.count { it.status == AgentViewModel.TaskStatus.IN_PROGRESS }
    }

    SubmenuScaffold(
        title = "Agent Hub",
        subtitle = "Central Command Center",
        color = Color(0xFF9370DB),
        onNavigateBack = { navController.popBackStack() },
        menuItems = menuItems,
        onItemClick = { item ->
            navController.navigate(item.route)
        },
        headerContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.6f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9370DB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF32CD32),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active Agents",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tasks Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF00CED1),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Avg. Level",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    )
}
