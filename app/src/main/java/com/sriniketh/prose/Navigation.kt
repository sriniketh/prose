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

    data object CAPTUREANDCROPIMAGE : Screen {
        override val route: String = "capture_and_crop_image"
        const val argBookId: String = "bookId"
    }

    data object SAVEHIGHLIGHT_FROMURI : Screen {
        override val route: String = "save_highlight_from_uri"
        const val argUri: String = "uri"
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
