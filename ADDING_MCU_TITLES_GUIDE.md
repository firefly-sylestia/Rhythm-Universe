# Adding New MCU Titles to CinemaVerse

This guide explains how to add new MCU titles (movies, series, shorts) to CinemaVerse.

## Quick Summary

1. Add title data to `app/src/main/assets/mcu_data/mcu_titles.json`
2. Add poster image to `app/src/main/assets/mcu_posters/`
3. (Optional) Add viewing list membership in `ViewingLists.kt`
4. Rebuild app: `./gradlew :app:assembleGithubDebug`

## Step-by-Step Workflow

### Step 1: Assign a Title ID

Check existing title IDs to avoid conflicts:

```bash
# View highest ID in use
grep -o '"id": "[0-9]*"' app/src/main/assets/mcu_data/mcu_titles.json | sort -u | tail -5
```

**Example:** If highest is `"id": "48"`, use `"id": "49"` for the next title.

Document new IDs in `TITLE_ID_LIST.md` if maintaining that file.

### Step 2: Add Title to mcu_titles.json

Edit `app/src/main/assets/mcu_data/mcu_titles.json` and add a new entry:

```json
{
  "id": "49",
  "title": "Echo",
  "type": "series",
  "series": "MCU",
  "saga": "Multiverse Saga",
  "phase": "Phase 5",
  "viewingOrder": 49,
  "chronologicalOrder": 49,
  "releaseDate": 1704067200000,
  "year": 2024,
  "runtime": 30,
  "genres": ["Action", "Drama"],
  "plot": "Maya Lopez, aka Echo, gains superhuman powers and must face her past.",
  "director": "Sydney Freeling, Kel Kwan",
  "cast": "Alaqua Cox, Vincent D'Onofrio",
  "posterPath": "049-echo.jpg",
  "backdropPath": "049-echo-backdrop.jpg"
}
```

**Field Guide:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | string | Yes | Unique identifier, should be numeric (e.g., "49") |
| `title` | string | Yes | Display title (e.g., "Echo") |
| `type` | string | Yes | "movie", "series", or "short" |
| `series` | string | Yes | Usually "MCU" or specific storyline (e.g., "Spider-Man") |
| `saga` | string | Yes | "Infinity Saga", "Multiverse Saga", etc. |
| `phase` | string | Yes | "Phase 1" through "Phase 6" |
| `viewingOrder` | number | Yes | Release order (incrementing number) |
| `chronologicalOrder` | number | Yes | In-universe timeline order |
| `releaseDate` | number | Yes | Epoch milliseconds (JavaScript `Date.getTime()`) |
| `year` | number | No | Release year (for quick reference) |
| `runtime` | number | No | Duration in minutes |
| `genres` | array | No | List of genre strings |
| `plot` | string | No | Short plot description (50-100 words) |
| `director` | string | No | Director name(s) |
| `cast` | string | No | Main cast (comma-separated) |
| `posterPath` | string | Yes | Filename in `mcu_posters/` folder |
| `backdropPath` | string | No | Filename for backdrop image |

### Step 3: Prepare Poster Image

**Poster requirements:**
- Format: JPG or PNG (recommend JPG for smaller file size)
- Dimensions: 300×450 pixels (ideal for phone screens)
- File size: <150 KB (optimize with tools like ImageMagick or online optimizers)
- Filename: Match `posterPath` in JSON (e.g., `049-echo.jpg`)

**Example workflow:**

```bash
# Download poster from TMDB or IMDb
# Resize and optimize
convert input.jpg -resize 300x450 -quality 80 049-echo.jpg

# Move to assets folder
mv 049-echo.jpg app/src/main/assets/mcu_posters/
```

**Naming convention:**
- Use format: `XXX-title-slug.jpg` (e.g., `049-echo.jpg`)
- Use lowercase, replace spaces with hyphens
- Include ID prefix for easy identification

### Step 4: (Optional) Add to Viewing Collections

If you want the title to appear in specific viewing collections (e.g., "Phase 5", "Latest Releases"), edit `app/src/main/java/com/cinemaverse/mcu/shared/data/viewing/ViewingLists.kt`:

```kotlin
// In ViewingLists.kt, find the appropriate list

val phase5Items = listOf(
    // ... existing items
    ViewingItem(
        id = "49",
        title = "Echo",
        type = ViewingItemType.SERIES,
        series = "Marvel",
        saga = "Multiverse Saga",
        // ... other fields
        order = 49
    )
)

// Or add to releaseItems if it's a new release
val releaseItems = listOf(
    // ... existing items
    // Plus the new Echo item
)
```

**Note:** The JSON data will be merged with `ViewingLists.kt`. If a title exists in JSON but not in `ViewingLists.kt`, it will still be visible but without curated metadata (plot, director, genres from `ViewingLists`).

### Step 5: Rebuild and Test

```bash
# Clean build (recommended for asset changes)
./gradlew clean :app:assembleGithubDebug

# Install
adb install app/build/outputs/apk/github/debug/app-github-debug.apk

# Or one command:
adb install -r app/build/outputs/apk/github/debug/app-github-debug.apk
```

### Step 6: Verify in App

1. Open CinemaVerse app
2. Go to Library or search by title name
3. Verify:
   - Title appears in correct phase/saga
   - Poster loads from assets
   - Title metadata displays correctly
   - Viewing order is correct

## Release Date Conversion

JavaScript timestamps are milliseconds since epoch (January 1, 1970).

**Convert to timestamp:**

```bash
# Using Node.js REPL
node -e "console.log(new Date('2024-01-01').getTime())"
# Output: 1704067200000

# Using online tool
# https://www.epochconverter.com/
```

**Common MCU releases:**
- Iron Man (2008-05-02): 1209753600000
- Avengers Endgame (2019-04-26): 1556246400000
- Echo (2024-01-09): 1704739200000

## Updating Existing Titles

To update an existing title's metadata (plot, director, rating, etc.):

1. Edit the entry in `mcu_titles.json`
2. If changing poster filename, update both JSON and add new image to assets
3. If updating `ViewingLists.kt`, keep changes in sync with JSON
4. Rebuild: `./gradlew :app:assembleGithubDebug`

## Bulk Operations

If adding many titles at once:

```python
# Python script to generate JSON entries
import json
from datetime import datetime

titles = [
    ("Echo", "2024-01-09", "series"),
    ("Agatha All Along", "2024-09-18", "series"),
    # Add more titles...
]

entries = []
for idx, (title, release_date, title_type) in enumerate(titles, start=49):
    release_ms = int(datetime.fromisoformat(release_date).timestamp() * 1000)
    entry = {
        "id": str(idx),
        "title": title,
        "type": title_type,
        "series": "MCU",
        "saga": "Multiverse Saga",
        "phase": "Phase 5",
        "viewingOrder": idx,
        "chronologicalOrder": idx,
        "releaseDate": release_ms,
        "posterPath": f"{idx:03d}-{title.lower().replace(' ', '-')}.jpg"
    }
    entries.append(entry)

# Save to file for review/insertion
with open("new_titles.json", "w") as f:
    json.dump(entries, f, indent=2)
```

## Validation Checklist

Before rebuilding:

- [ ] JSON is valid (test with `jq app/src/main/assets/mcu_data/mcu_titles.json`)
- [ ] Title ID is unique (not already used)
- [ ] Poster filename matches `posterPath` in JSON
- [ ] Poster file exists in `app/src/main/assets/mcu_posters/`
- [ ] Poster dimensions are ~300×450 pixels
- [ ] Release date is in epoch milliseconds (13-digit number)
- [ ] `viewingOrder` increments sequentially (or custom if needed)
- [ ] `chronologicalOrder` reflects in-universe timeline
- [ ] Phase and saga are accurate for MCU canon
- [ ] Plot summary is 50-100 words (for readability)

## JSON Validation

To validate JSON syntax:

```bash
# Using jq
jq . app/src/main/assets/mcu_data/mcu_titles.json > /dev/null && echo "Valid JSON" || echo "Invalid JSON"

# Using Python
python3 -m json.tool app/src/main/assets/mcu_data/mcu_titles.json > /dev/null && echo "Valid JSON"
```

## Troubleshooting

### "Poster not found" message

- Verify file exists: `ls -la app/src/main/assets/mcu_posters/049-echo.jpg`
- Verify filename matches JSON `posterPath` exactly (case-sensitive)
- Rebuild after adding poster: `./gradlew clean :app:assembleGithubDebug`

### Title doesn't appear in library

- Check JSON is valid: `jq . mcu_titles.json`
- Verify title ID is unique
- Verify JSON has been committed/synced (if using version control)
- Check logcat: `adb logcat | grep -i "mcu\|viewing"`

### Poster loads but wrong image

- Verify `posterPath` in JSON matches actual filename
- File extensions are case-sensitive (jpg ≠ JPG)
- Clear app cache: Settings > Apps > CinemaVerse > Storage > Clear Cache

### Phase not showing new title

- Update `ViewingLists.kt` if you want it in specific collections
- JSON data alone doesn't auto-populate lists
- Sync curated list membership with JSON IDs

## Reference

- **MCU Timeline:** https://www.marvel.com/articles
- **Epoch Converter:** https://www.epochconverter.com/
- **JSON Validator:** https://jsonlint.com/
- **Image Resizer:** https://imageresizer.com/ or ImageMagick CLI

---

**Last Updated:** June 3, 2026
**Next Steps:** After adding titles, test offline functionality and verify APIs can enrich with TMDB/OMDb data.
