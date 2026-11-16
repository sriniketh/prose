package com.sriniketh.core_design.ui.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "400x400", widthDp = 400, heightDp = 400, showBackground = true)
@Preview(name = "400x500", widthDp = 400, heightDp = 500, showBackground = true)
@Preview(name = "400x1000", widthDp = 400, heightDp = 1000, showBackground = true)
@Preview(name = "610x400", widthDp = 610, heightDp = 400, showBackground = true)
@Preview(name = "610x500", widthDp = 610, heightDp = 500, showBackground = true)
@Preview(name = "610x1000", widthDp = 610, heightDp = 1000, showBackground = true)
@Preview(name = "900x400", widthDp = 900, heightDp = 400, showBackground = true)
@Preview(name = "900x500", widthDp = 900, heightDp = 500, showBackground = true)
@Preview(name = "900x1000", widthDp = 900, heightDp = 1000, showBackground = true)
annotation class ScreenSizePreviews

@Preview(name = "dark", widthDp = 400, heightDp = 500, showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "large-font", widthDp = 400, heightDp = 500, showBackground = true, fontScale = 1.5f)
annotation class ScreenVariantPreviews

@Preview(name = "light", widthDp = 400, heightDp = 500, showBackground = true)
@Preview(name = "dark", widthDp = 400, heightDp = 500, showBackground = true, uiMode = UI_MODE_NIGHT_YES)
annotation class ScreenStatePreviews
