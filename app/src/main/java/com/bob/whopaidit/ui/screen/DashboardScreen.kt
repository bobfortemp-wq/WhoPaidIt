package com.bob.whopaidit.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bob.whopaidit.R
import com.bob.whopaidit.ui.navigation.Routes
import com.bob.whopaidit.ui.theme.WhoPaidItTheme

enum class DashboardTab(val title: String, val route: String, val iconRes: Int) {
    HOME("Home", Routes.DASHBOARD_HOME, R.drawable.ic_home),
    GROUPS("Groups", Routes.DASHBOARD_GROUPS, R.drawable.ic_groups),
    ACTIVITY("Activity", Routes.DASHBOARD_ACTIVITY, R.drawable.ic_activity),
    PROFILE("Profile", Routes.DASHBOARD_PROFILE, R.drawable.ic_person),
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    userName: String = "User",
    userEmail: String = "user@example.com",
    onLogoutClick: () -> Unit = {},
    onNewTripClick: () -> Unit = {},
    onJoinTripClick: () -> Unit = {},
    onTripClick: (TripItem) -> Unit = {},
    dashboardNavController: NavHostController = rememberNavController(),
) {
    var selectedTab by remember { mutableStateOf(DashboardTab.HOME) }
    val isPreview = LocalInspectionMode.current

    val navBackStackEntry by dashboardNavController.currentBackStackEntryAsState()
    val currentRoute = if (isPreview) selectedTab.route else (navBackStackEntry?.destination?.route
        ?: Routes.DASHBOARD_HOME)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp,
            ) {
                DashboardTab.entries.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedTab = tab
                            if (!isPreview && (currentRoute != tab.route)) {
                                dashboardNavController.navigate(tab.route) {
                                    popUpTo(dashboardNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.iconRes),
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (isPreview) {
                when (selectedTab) {
                    DashboardTab.HOME -> HomeScreen(
                        userName = userName,
                        onNewTripClick = onNewTripClick,
                        onJoinTripClick = onJoinTripClick,
                        onTripClick = onTripClick,
                    )

                    DashboardTab.GROUPS -> GroupScreen()
                    DashboardTab.ACTIVITY -> ActivityScreen()
                    DashboardTab.PROFILE -> ProfileScreen(
                        userName = userName,
                        userEmail = userEmail,
                        onLogoutClick = onLogoutClick,
                    )
                }
            } else {
                NavHost(
                    navController = dashboardNavController,
                    startDestination = Routes.DASHBOARD_HOME,
                ) {
                    composable(Routes.DASHBOARD_HOME) {
                        HomeScreen(
                            userName = userName,
                            onNewTripClick = onNewTripClick,
                            onJoinTripClick = onJoinTripClick,
                            onTripClick = onTripClick,
                        )
                    }
                    composable(Routes.DASHBOARD_GROUPS) {
                        GroupScreen()
                    }
                    composable(Routes.DASHBOARD_ACTIVITY) {
                        ActivityScreen()
                    }
                    composable(Routes.DASHBOARD_PROFILE) {
                        ProfileScreen(
                            userName = userName,
                            userEmail = userEmail,
                            onLogoutClick = onLogoutClick,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    WhoPaidItTheme {
        DashboardScreen()
    }
}
