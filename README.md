[![Install](https://img.shields.io/badge/Install-JetBrains%20Marketplace-0A7FFF?style=flat-square&logo=intellij-idea&logoColor=white)](https://plugins.jetbrains.com/plugin/29172-pojo-to-json-xml)
---

## 🧭 Overview
POJO to JSON/XML is a lightweight IntelliJ plugin that generates clean, well-formatted JSON and XML examples from your Java classes. It respects common annotations (e.g., @JsonProperty), handles nested types and collections, and produces readable multi-line output—perfect for API samples, tests, and documentation.

---

## 🚀 Installation
- JetBrains Marketplace: install “POJO to JSON/XML”.
- Supported IDEs: IntelliJ IDEA (Community/Ultimate) and IntelliJ Platform IDEs.
- Minimum IDE version: 2023.2+

Manual install:
- Use Settings → Plugins → Install from Disk… and select the ZIP from build/distributions.

---

## 🔍 Usage
1) Open a class in the editor.
2) Right-click → Generate → POJO to JSON or XML.
3) Pick optional settings:
    - Include null values
    - Default string/date values
    - Indent size (2/4 spaces)
4) Copy from the preview or save to file.

Also available from:
- Project View and Editor context menus: “Generate JSON / XML from POJO”.
- Assign a shortcut via Keymap if desired.

---

## ✨ Features
- One-click JSON/XML generation from POJOs
- Annotation awareness: @JsonProperty, @JsonIgnore, etc.
- Collections & Map support: List, Set, Map
- Safe depth control for recursive/nested structures
- Clean formatting with consistent indentation
- Customizable defaults (null policy, string/datetime, indent)
- One-click copy to clipboard

---

## 🧩 Supported Types
- Primitives: boolean, int, long, double, BigDecimal, String, UUID
- Date/Time: LocalDate, LocalDateTime
- Enums: uses the first constant as a sample value
- Collections: List/Set (single-item arrays)
- Map: sample keys/values matching declared types
- Objects: recursive field processing with depth limit

---

## ⚙️ Configuration
Settings → Tools → POJO to JSON/XML
- Null Policy: produce nulls or meaningful defaults
- Default Values: e.g., string “lorem”, date “1970-01-01”
- Indent: 2 or 4 spaces
- Max Depth: prevents infinite recursion in cyclic references

plugin.xml notes:
- depends: com.intellij.modules.java
- Extensions: projectConfigurable and applicationService are registered

---

## 🖼️ Icons
- Default: META-INF/pluginIcon.svg
- Dark theme: META-INF/pluginIcon_dark.svg
- Optional variants:
    - Centered “Java” with XML “<>” and JSON “{}” below
    - XML-focused “<>”
    - JSON-focused “{}”
      All icons are SVG and optimized for small sizes.

---

## 🧪 Development & Packaging
Requirements:
- JDK 17+
- Gradle

Build:
- ./gradlew buildPlugin → outputs ZIP in build/distributions

Run locally:
- ./gradlew runIde

Publish (optional):
- Configure signing/publishing via environment variables:
    - CERTIFICATE_CHAIN, PRIVATE_KEY, PRIVATE_KEY_PASSWORD, PUBLISH_TOKEN
- ./gradlew publishPlugin

---

## 📚 Sample Output
JSON
{
"id": 1,
"name": "lorem",
"createdAt": "1970-01-01T00:00:00"
}

XML
<example>
<id>1</id>
<name>lorem</name>
<createdAt>1970-01-01T00:00:00</createdAt>
</example>

Note: Actual output depends on your class structure and settings.

---

## 📦 Repository Layout (suggested)
- src/main/java — plugin sources
- src/main/resources — META-INF/plugin.xml, icons
- docs/ — screenshots/GIFs
- LICENSE, README.md

---

## 🔧 Troubleshooting
- Action not visible:
    - Ensure Java module support (depends: com.intellij.modules.java).
    - Use runIde with a compatible IDE version.
- Formatting issues:
    - Check indent and null policy settings.
- Publish errors:
    - Verify PUBLISH_TOKEN and vendor permissions on Marketplace.

---

## 📬 Contact
- Vendor: Muhammed Talha Çiğdem
- Email: talhacgdem@gmail.com
- Contributions and issues: please use GitHub Issues/PRs

Apache License. See LICENSE for details.