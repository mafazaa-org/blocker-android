# Keyword Blocking Feature

This document describes the newly implemented keyword blocking feature that allows users to block content containing specific keywords they enter.

## Overview

The keyword blocking feature extends the existing app blocking functionality by allowing users to define custom keywords. When the accessibility service detects any of these keywords on the screen, it blocks access to that content, similar to how blocked apps are handled.

## Architecture

### Components

#### 1. Data Storage (`SharedPrefs.kt`)
```kotlin
var blockedKeywords by sharedPreferences.delegates.stringSet()
```
- Stores keywords as a Set<String> in SharedPreferences
- Persists across app restarts
- Leverages existing delegation pattern

#### 2. Domain Models

**BlockReason.kt**
```kotlin
data class UsingBlockedKeyword(val keyword: String) : BlockReason() {
    override fun getName(): String {
        return "كلمة محظورة: $keyword"
    }
}
```
- New sealed class case for keyword blocking
- Includes the detected keyword in the block reason

#### 3. Detection Logic (`ScreenAnalyser.kt`)

**containsBlockedKeyword()**
```kotlin
fun containsBlockedKeyword(screenNode: ScreenNode, blockedKeywords: Set<String>): String?
```
- Recursively searches through the screen node tree
- Checks both `text` and `desc` (contentDescription) fields
- Case-insensitive matching using `contains(keyword, ignoreCase = true)`
- Returns first matched keyword or null if none found
- Early exit when blocked keywords set is empty

#### 4. Service Integration (`MyAccessibilityService.kt`)

The accessibility service checks for blocked keywords after checking for blocked apps:

```kotlin
// Check for blocked keywords
val blockedKeyword = checkBlockedKeywords(analysisResult.root)
if (blockedKeyword != null) {
    MyLog.i(TAG, "Blocked keyword detected: $blockedKeyword")
    block(BlockReason.UsingBlockedKeyword(blockedKeyword))
    return@launch
}
```

Order of checks:
1. Blocked apps
2. Blocked keywords
3. Script-based blocking (existing functionality)

#### 5. UI Components

**ManageKeywordsDialog.kt**
- Material Design 3 dialog for keyword management
- Features:
  - Text field with add button for new keywords
  - Scrollable list of existing keywords
  - Delete button with confirmation dialog
  - Empty state when no keywords exist
  - Proper RTL support for Arabic

**ProtectionActivatedScreen.kt**
- Added "Manage Blocked Keywords" button
- Opens the ManageKeywordsDialog when clicked

#### 6. ViewModel (`AppViewModel.kt`)

```kotlin
private val _blockedKeywords = MutableStateFlow<Set<String>>(emptySet())
val blockedKeywords: StateFlow<Set<String>> = _blockedKeywords.asStateFlow()

fun addBlockedKeyword(keyword: String)
fun removeBlockedKeyword(keyword: String)
```
- Reactive state management using StateFlow
- Trim and validate keywords before adding
- Persist changes immediately to SharedPreferences

## User Flow

1. User opens the app and protection is activated
2. User taps "Manage Blocked Keywords" button
3. Dialog opens showing existing keywords (if any)
4. User can:
   - Add new keyword: Type in text field and tap "Add Keyword"
   - Remove keyword: Tap delete icon, confirm in dialog
5. Keywords are saved immediately
6. When any app displays content with a blocked keyword:
   - Accessibility service detects the keyword
   - Screen is blocked with overlay showing the blocked keyword
   - User is navigated back

## Localization

All strings are available in English and Arabic:

### English
- `blocked_keywords_text`: "Blocked Keywords"
- `add_keyword_text`: "Add Keyword"
- `manage_keywords_text`: "Manage Blocked Keywords"
- `enter_keyword_text`: "Enter keyword to block"
- `keyword_added_text`: "Keyword added successfully"
- `keyword_removed_text`: "Keyword removed"
- `no_keywords_text`: "No blocked keywords yet"
- `blocked_keyword_message`: "This content contains a blocked keyword"

### Arabic
- Similar translations with proper RTL support

## Testing

Unit tests cover all keyword detection scenarios:

1. Empty keyword set returns null
2. Keyword found in text field
3. Keyword found in description field
4. Case-insensitive matching
5. Nested node traversal
6. No match scenarios
7. Null text/description handling

Run tests:
```bash
./gradlew test
```

## Performance Considerations

1. **Early Exit**: Returns immediately if blocked keywords set is empty
2. **First Match**: Stops searching after finding first keyword
3. **Efficient Storage**: Uses Set for O(1) lookup in contains check
4. **Async Processing**: Screen analysis happens on background thread

## Security Considerations

1. **Keyguard Check**: Only blocks if device has secure lock screen enabled
2. **No Bypass**: Keywords cannot be unblocked without app access
3. **Case Insensitive**: Prevents simple case-change bypass attempts
4. **Persistent Storage**: Survives app restarts and updates

## Limitations

1. Keywords are matched as substrings (e.g., "cat" matches "catch")
2. No regex or wildcard support (keeps it simple and fast)
3. No whitelist exceptions
4. Detection limited to visible screen content
5. Requires accessibility service to be active

## Future Enhancements

Possible improvements:
- Whole word matching option
- Regular expression support
- Keyword categories/groups
- Import/export keyword lists
- Keyword blocking statistics
- Temporary keyword disabling

## API Usage

### Adding a Keyword
```kotlin
viewModel.addBlockedKeyword("inappropriate")
```

### Removing a Keyword
```kotlin
viewModel.removeBlockedKeyword("inappropriate")
```

### Checking for Keywords
```kotlin
val keyword = ScreenAnalyser.containsBlockedKeyword(screenNode, blockedKeywords)
if (keyword != null) {
    // Block the content
}
```

## Files Modified/Created

### Created
- `app/src/main/java/com/mafazaa/ainaa/ui/dialog/ManageKeywordsDialog.kt`
- `app/src/test/java/com/mafazaa/ainaa/helpers/ScreenAnalyserTest.kt`

### Modified
- `app/src/main/java/com/mafazaa/ainaa/data/local/SharedPrefs.kt`
- `app/src/main/java/com/mafazaa/ainaa/domain/models/BlockReason.kt`
- `app/src/main/java/com/mafazaa/ainaa/helpers/ScreenAnalyser.kt`
- `app/src/main/java/com/mafazaa/ainaa/service/MyAccessibilityService.kt`
- `app/src/main/java/com/mafazaa/ainaa/viewmodels/AppViewModel.kt`
- `app/src/main/java/com/mafazaa/ainaa/ui/protection/ProtectionActivatedScreen.kt`
- `app/src/main/java/com/mafazaa/ainaa/AppActivity.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-ar/strings.xml`
