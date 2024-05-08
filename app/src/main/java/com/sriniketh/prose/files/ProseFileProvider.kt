package com.sriniketh.prose.files

import androidx.annotation.XmlRes
import androidx.core.content.FileProvider
import com.sriniketh.prose.R

class ProseFileProvider(@XmlRes resourceId: Int = R.xml.file_paths) : FileProvider(resourceId)
