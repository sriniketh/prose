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

    data object CAPTUREANDSELECTTEXT : Screen {
        override val route: String = "capture_and_select_text"
        const val argBookId: String = "bookId"
    }

    data object SAVEHIGHLIGHT_FROMTEXT : Screen {
        override val route: String = "save_highlight_from_text"
        const val argText: String = "text"
        const val argBookId: String = "bookId"
    }

    data object SAVEHIGHLIGHT_FROMHIGHLIGHTID : Screen {
        override val route: String = "save_highlight_from_highlight_id"
        const val argHighlightId: String = "highlightId"
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
