---
name: playwright-test
description: >
  Creates Playwright browser-based integration tests for Vaadin views using the
  Drama Finder library for type-safe element wrappers with accessibility-first APIs.
  Use when the user asks to "write Playwright tests", "create e2e tests", "write
  integration tests", "test in the browser", "write IT tests", or mentions
  end-to-end testing, browser tests, UI integration tests, Playwright for Vaadin,
  or Drama Finder. Also trigger when the user references a use case (UC-*) and
  asks for Playwright or E2E tests.
---

# Playwright Tests with Drama Finder

Create Playwright integration tests for the Vaadin view specified in $ARGUMENTS. Tests run in a real browser against a running application. Use the Drama Finder library for type-safe, accessibility-first element lookups — never raw Playwright locators.

## Setup

Tests extend `AbstractBasePlaywrightIT` from Drama Finder, which handles browser lifecycle, page creation, and Vaadin synchronization automatically.

```xml
<dependency>
    <groupId>org.vaadin.addons</groupId>
    <artifactId>dramafinder</artifactId>
    <version>1.1.0</version>
    <scope>test</scope>
</dependency>
```
## Important
- Do Blackbox Tests: Generate the tests against the running application (usually http://localhost:8080) and don't consider the implementation.

## DO NOT

- Use Mockito, access services/repositories/DSLContext directly
- Use raw Playwright locators like `page.locator("vaadin-text-field")` — use Drama Finder element wrappers
- Use `Thread.sleep()` or `page.waitForTimeout()` — Drama Finder assertions auto-retry
- Delete all data in cleanup — only remove data created during the test
- Assume all grid rows are rendered (viewport limits visible rows)
- Use XPath selectors (they don't pierce shadow DOM — CSS does)
- Use `getAttribute()`/`isVisible()` directly in assertions — they don't auto-retry

## Test Data

Use existing test data from Flyway migrations in `src/test/resources/db/migration`. If your test creates data, clean up in `@AfterEach`.

## Template

Use [templates/ExampleViewIT.java](templates/ExampleViewIT.java) as the starting point for new test classes.

## Locating Components

Drama Finder uses ARIA roles and accessible names — not CSS selectors. This makes tests resilient to DOM changes and enforces accessibility. 
API documentation of Drama Finder: [https://parttio-dramafinder-4.mintlify.app/api/vaadin-element](https://parttio-dramafinder-4.mintlify.app/api/vaadin-element)

### By Label (input fields, pickers)

```java
TextFieldElement nameField = TextFieldElement.getByLabel(page, "Full Name");
DatePickerElement birthDate = DatePickerElement.getByLabel(page, "Birth Date");
ComboBoxElement country = ComboBoxElement.getByLabel(page, "Country");
CheckboxElement active = CheckboxElement.getByLabel(page, "Active");
```

### By Text (buttons, tabs)

```java
ButtonElement save = ButtonElement.getByText(page, "Save");
```

### By ID (grids, specific components)

```java
GridElement grid = GridElement.getById(page, "customer-grid");
```

### First on Page

```java
GridElement grid = GridElement.get(page);
DialogElement dialog = new DialogElement(page);
NotificationElement notif = new NotificationElement(page);
```

### By Header Text (dialogs)

```java
DialogElement dialog = DialogElement.getByHeaderText(page, "Confirm Delete");
```

### Scoped Lookups (within containers)

When multiple elements share the same label, scope the lookup to a container:

```java
DialogElement dialog = DialogElement.getByHeaderText(page, "Edit Person");
TextFieldElement name = TextFieldElement.getByLabel(dialog.getLocator(), "Name");
ButtonElement confirm = ButtonElement.getByText(dialog.getLocator(), "Confirm");
```

### ARIA Role Mapping

| Role       | Element Classes                                                                  |
|------------|----------------------------------------------------------------------------------|
| TEXTBOX    | TextFieldElement, EmailFieldElement, PasswordFieldElement, TextAreaElement        |
| SPINBUTTON | IntegerFieldElement, BigDecimalFieldElement, NumberFieldElement                   |
| COMBOBOX   | ComboBoxElement, DatePickerElement, TimePickerElement, DateTimePickerElement      |
| BUTTON     | ButtonElement                                                                    |
| CHECKBOX   | CheckboxElement                                                                  |
| RADIO      | RadioButtonElement                                                               |
| DIALOG     | DialogElement                                                                    |
| GRID       | GridElement                                                                      |

For icon-only buttons, set `setAriaLabel("Close")` on the server side, then find with `ButtonElement.getByText(page, "Close")`.

## Element APIs

### TextFieldElement

```java
TextFieldElement tf = TextFieldElement.getByLabel(page, "Username");
tf.setValue("john.doe");
tf.clear();
tf.getValue();
tf.assertValue("john.doe");
tf.assertVisible();
tf.assertEnabled();
tf.assertValid();
tf.assertInvalid();
tf.assertRequired();
tf.assertErrorMessage("Field is required");
tf.assertPattern("\\d{7}");
tf.assertMinLength(6);
tf.assertMaxLength(7);
tf.assertHelperHasText("Enter 7 digits");
```

Sub-locators: `getInputLocator()`, `getHelperLocator()`, `getErrorMessageLocator()`, `getPrefixLocator()`, `getSuffixLocator()`

### ButtonElement

```java
ButtonElement btn = ButtonElement.getByText(page, "Save");
btn.click();
btn.assertEnabled();
btn.assertDisabled();
btn.assertVisible();
btn.assertTheme("primary");
btn.assertCssClass("custom-btn");
btn.focus();
btn.assertIsFocused();
```

### GridElement

```java
GridElement grid = GridElement.get(page);

// Row counts
int total = grid.getTotalRowCount();        // all rows (including non-rendered)
int visible = grid.getRenderedRowCount();   // only rendered in viewport

// Headers
List<String> headers = grid.getHeaderCellContents();

// Cell access
var cell = grid.findCell(0, 0);             // by row/column index
var cell = grid.findCell(0, "Email");       // by row index + column header

// Row operations
var row = grid.findRow(0);
grid.select(0);
grid.deselect(0);

// Lazy loading — scrolls automatically to the row
var distantRow = grid.findRow(9000);

// Sorting
var header = grid.findHeaderCellByText("Name");
header.get().clickSort();

// Scrolling
grid.scrollToRow(500);
grid.scrollToStart();
grid.scrollToEnd();

// Wait for async data
grid.waitForGridToStopLoading();

// Select all
grid.checkSelectAll();
grid.getSelectedItemCount();
```

### ComboBoxElement

```java
ComboBoxElement cb = ComboBoxElement.getByLabel(page, "Country");
cb.selectItem("Germany");
cb.filterAndSelectItem("Ger", "Germany");  // for lazy-loading
cb.setFilter("search text");
cb.open();
cb.close();
cb.getValue();
cb.assertValue("Germany");
cb.assertOpened();
cb.assertClosed();
cb.assertItemCount(5);
cb.assertReadOnly();
```

### DatePickerElement

```java
DatePickerElement dp = DatePickerElement.getByLabel(page, "Birth Date");
dp.setValue(LocalDate.of(1990, 1, 15));
dp.setValue("15/01/1990");                 // string format
dp.getValueAsLocalDate();
dp.assertValue(LocalDate.of(1990, 1, 15));
```

### CheckboxElement

```java
CheckboxElement cb = CheckboxElement.getByLabel(page, "Accept Terms");
cb.check();
cb.uncheck();
cb.assertChecked();
cb.assertNotChecked();
cb.isIndeterminate();
cb.assertIndeterminate();
```

### DialogElement

```java
DialogElement dialog = DialogElement.getByHeaderText(page, "Confirm Delete");
dialog.assertOpen();
dialog.assertClosed();
dialog.getHeaderLocator();
dialog.getContentLocator();
dialog.getFooterLocator();
```

### NotificationElement

```java
NotificationElement notif = new NotificationElement(page);
notif.assertOpen();
notif.assertClosed();
notif.getContentLocator();
```

### AccordionElement

```java
AccordionElement acc = new AccordionElement(locator);
acc.openPanel("Details");
acc.closePanel("Details");
acc.assertPanelOpened("Details");
acc.assertPanelClosed("Details");
acc.assertPanelCount(3);
```

## Assertions Reference

All `assert*()` methods auto-retry until the condition is met or timeout (default 5 seconds). This eliminates the need for manual waits.

| Category          | Methods                                                          |
|-------------------|------------------------------------------------------------------|
| Visibility        | `assertVisible()`, `assertHidden()`                              |
| State             | `assertEnabled()`, `assertDisabled()`                            |
| Focus             | `assertIsFocused()`, `assertIsNotFocused()`                      |
| Value             | `assertValue("...")` (text fields, combo boxes, date pickers)    |
| Validation        | `assertValid()`, `assertInvalid()`, `assertRequired()`          |
| Error message     | `assertErrorMessage("...")`, `getErrorMessageLocator()`          |
| Checked           | `assertChecked()`, `assertNotChecked()`, `assertIndeterminate()` |
| Dialog            | `assertOpen()`, `assertClosed()`                                 |
| ARIA              | `assertAriaLabel("...")`                                         |
| Styling           | `assertTheme("...")`, `assertCssClass("...")`                    |
| Tooltip           | `assertTooltipHasText("...")`                                    |
| Prefix/Suffix     | `assertPrefixHasText("...")`, `assertSuffixHasText("...")`       |
| Grid row count    | `assertThat(grid.getTotalRowCount()).isGreaterThan(0)`           |
| Grid cell content | `assertThat(cell.get().getCellContentLocator()).hasText("...")`  |

## Locator Types

Each element has two locator levels — use the right one:

- **`getLocator()`** — the component root. Use for: `theme`, `class`, `opened`, `invalid` attributes
- **`getInputLocator()`** — the internal input element. Use for: `value`, `maxlength`, `pattern`, `placeholder`, focus, disabled

CSS selectors pierce shadow DOM automatically. XPath does NOT.

**Part selectors** for internal elements: `input`, `clear-button`, `toggle-button`, `prefix`, `suffix`

## Workflow

1. Read the use case specification
2. Plan test scenarios (group related tests in `@Nested` classes with `@DisplayName`)
3. Create test class extending `AbstractBasePlaywrightIT` with `@SpringBootTest` and `@LocalServerPort`
4. Override `getUrl()` (return `http://localhost:<port>/`) and `getView()` (return the route)
5. For each test:
   - Use Drama Finder element wrappers to locate components by label/text/ID
   - Perform interactions (setValue, click, selectItem, check)
   - Assert outcomes using auto-retry assertions
   - Clean up test-created data in `@AfterEach`
6. Run tests with `./mvnw verify -Pit` to verify
7. On failure: check view loaded, verify test data in Flyway migrations, use `isGreaterThan()` for grid counts, add `waitForGridToStopLoading()` for async grids

## Troubleshooting

- **Element not found**: Check exact label text matches, ensure element is rendered, try scoped lookup
- **Multiple elements matched**: Factory methods use `.first()` automatically; scope to container for precision
- **Wrong locator type**: Use `getInputLocator()` for value/focus, `getLocator()` for component attributes
- **Flaky tests**: Replace any boolean checks with auto-retry assertions
- **Visual debugging**: `./mvnw verify -Pit -Dheadless=false -Dit.test=YourTestIT`
