package com.sriniketh.prose

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.sriniketh.prose.core_network.BooksRemoteDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var booksRemoteDataSource: BooksRemoteDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Testing
        lifecycleScope.launch {
            booksRemoteDataSource.getVolumes("Harry Potter")
        }
    }
}