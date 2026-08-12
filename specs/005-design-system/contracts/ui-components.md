# Phase 1 Contract: Design System Public API

**Feature**: 005-design-system | **Date**: 2026-08-11

The design system's "interface" is its **public composable surface** — what feature modules call.
Signatures are the contract; bodies are implementation (tasks phase). Package:
`com.mirabilis.core.designsystem`. All appearance comes from tokens (FR-012).

---

## Theme

```kotlin
// theme/MirabilisTheme.kt
@Composable
fun MirabilisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
)

// Extension colors not in Material ColorScheme (warning/success), via CompositionLocal.
object MirabilisTheme {
    val extraColors: MirabilisExtraColors   // @Composable get
}
data class MirabilisExtraColors(val warning: Color, val onWarning: Color,
                                val success: Color, val onSuccess: Color)
```

**Contract**: wraps `MaterialTheme(colorScheme, typography, shapes)`; provides brand light/dark
schemes + extra colors. Every role defined in both modes.

---

## Buttons (FR-008)

```kotlin
enum class MirabilisButtonStyle { Primary, Secondary, Text }

@Composable
fun MirabilisButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MirabilisButtonStyle = MirabilisButtonStyle.Primary,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
)
```

**Contract**: Primary = filled `primary`/`onPrimary`; Secondary = outlined/tonal; Text = text-only
`primary`. Disabled uses 38%/12% alpha tokens. Long labels truncate with ellipsis (edge case).

---

## Text (FR-009)

```kotlin
@Composable
fun MirabilisText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,   // defaults to the role for the surface
    maxLines: Int = Int.MAX_VALUE,
)
```

**Contract**: bound to the typography scale + color roles; respects system font scale (FR-016).

---

## Text field (FR-010)

```kotlin
@Composable
fun MirabilisTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
)
```

**Contract**: shows label/placeholder/helper; error state swaps helper→errorText using `error`
role; focused/filled/disabled visuals from tokens.

---

## Floating action button (FR-011)

```kotlin
enum class MirabilisFabSize { Small, Regular }

@Composable
fun MirabilisFab(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    size: MirabilisFabSize = MirabilisFabSize.Regular,
    text: String? = null,   // non-null => extended FAB
)
```

---

## Extended catalog (FR-013)

```kotlin
@Composable fun MirabilisSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier)
@Composable fun MirabilisCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier)
@Composable fun MirabilisRadioButton(selected: Boolean, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier)
@Composable fun MirabilisCard(modifier: Modifier = Modifier, outlined: Boolean = false, content: @Composable ColumnScope.() -> Unit)
@Composable fun MirabilisChip(label: String, selected: Boolean = false, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier)
@Composable fun MirabilisTopAppBar(title: String, modifier: Modifier = Modifier, navigationIcon: (@Composable () -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {})
@Composable fun MirabilisDialog(onDismissRequest: () -> Unit, title: String, text: String, confirmButton: @Composable () -> Unit, dismissButton: (@Composable () -> Unit)? = null)
```

---

## Catalog / gallery (FR-017)

```kotlin
// catalog/ComponentCatalog.kt
@Composable
fun ComponentCatalog(modifier: Modifier = Modifier)   // renders all tokens + components + states
```

**Contract**: single surface listing every token and component with variants/states; itself built
only from design-system APIs (SC-004). Available as `@Preview` (light + dark) and hostable by
`:app` later (not wired here — FR-018).

---

## Preview convention (FR-019 / SC-007)

Every component file provides:

```kotlin
@Preview(name = "Light") @Composable private fun XLightPreview() = MirabilisTheme(darkTheme = false) { /* X in its states */ }
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES) @Composable private fun XDarkPreview() = MirabilisTheme(darkTheme = true) { /* X in its states */ }
```
