package com.aliucord.themer.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.aliucord.themer.R

@Composable
fun SearchBar(
    navController: NavController,
    title: Int,
    search: MutableState<Boolean>,
    query: MutableState<String>,
) {
    if (search.value) {
        BackHandler { search.value = false }

        BaseThemerAppBar(
            title = {
                TextField(
                    value = query.value,
                    onValueChange = { query.value = it },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { query.value = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear),
                            )
                        }
                    },
                    placeholder = { Text(stringResource(android.R.string.search_go)) },
                    textStyle = MaterialTheme.typography.subtitle1,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            navigationIcon = {
                IconButton(onClick = { search.value = false }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
        )
    } else ThemerAppBar(
        navController = navController,
        title = title,
        back = true,
        actions = {
            IconButton(onClick = { search.value = true }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(android.R.string.search_go),
                    tint = Color.White,
                )
            }
        },
    )
}
