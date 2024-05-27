package com.sriniketh.prose

internal sealed interface Screen {
    val route: String

    data object BOOKSHELF : Screen {
        override val route: String = "bookshelf"
    }

    data object VIEWHIGHLIGHTS : Screen {
        override val route: String = "view_highlights"
        const val argBookId: String = "bookId"
    }

    data object INPUTHIGHLIGHT : Screen {
        override val route: String = "input_highlight"
        const val argBookId: String = "bookId"
    }

    data object SEARCH : Screen {
        override val route: String = "search"
    }

    data object BOOKINFO : Screen {
        override val route: String = "book_info"
        const val argBookId: String = "bookId"
    }
}
