# Prose Developer Documentation

Welcome to the Prose developer documentation index! This folder contains comprehensive design guides, flow descriptions, and architecture documentations intended to help developers and agentic coding assistants understand the codebase quickly and modify it safely.

## Table of Contents

1. [Architecture Guide](architecture.md)
   * Details the modular project layout, dependency graph, Unidirectional Data Flow (UDF) pattern, dependency injection structure (Hilt), and standard testing patterns.
2. [OCR & Capture Workflow](ocr_flow.md)
   * Explains the step-by-step user flow from taking a photo/selecting an image, cropping it, performing on-device OCR using ML Kit, editing, saving, and managing temporary files/permissions.
3. [Local Database Schema](database_schema.md)
   * Describes the local offline-first persistence layer powered by Room, defining database entities (`BookEntity` and `HighlightEntity`), foreign key relationships, indexes, and DAO structures.
4. [Data Export Format](data_export.md)
   * Specifies the JSON serialization format for exporting highlights, domain model wrappers, and file storage strategies.

---
*For quick commands on building, testing, or environment setups, please refer to the root [AGENTS.md](../AGENTS.md).*
