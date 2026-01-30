# Frontend Testing Guide

## Overview

Comprehensive React component tests using Vitest and React Testing Library for the Expense Tracker frontend.

## Test Setup

### Testing Stack
- **Vitest** - Fast unit test framework (Vite-native)
- **React Testing Library** - Component testing utilities
- **@testing-library/user-event** - User interaction simulation
- **@testing-library/jest-dom** - Custom matchers for DOM assertions
- **jsdom** - DOM implementation for Node.js
- **MSW (Mock Service Worker)** - API mocking (future use)

### Configuration Files

**vitest.config.ts**
```typescript
- Test environment: jsdom
- Globals enabled
- Setup file: src/test/setup.ts
- Coverage: v8 provider with HTML/JSON reports
```

**src/test/setup.ts**
- Cleanup after each test
- Mock window.matchMedia
- Mock IntersectionObserver
- Mock ResizeObserver
- Import jest-dom matchers

## Test Files

### ExpenseForm.test.tsx
**Location:** `src/test/ExpenseForm.test.tsx`

**Test Suites:** 8 nested suites, **36 test cases**

1. **Component Rendering (5 tests)**
   - ✅ Renders all form fields
   - ✅ Loads and displays categories
   - ✅ Shows loading state for categories
   - ✅ Renders submit button
   - ✅ Renders cancel button when prop provided

2. **Form Validation (5 tests)**
   - ✅ Required amount validation
   - ✅ Amount must be greater than zero
   - ✅ Amount cannot be negative
   - ✅ Description max length (1000 chars)
   - ✅ Required date validation

3. **Form Submission (6 tests)**
   - ✅ Submits with valid data
   - ✅ Parses tags correctly
   - ✅ Handles empty tags
   - ✅ Disables submit during submission
   - ✅ Resets form after success
   - ✅ Calls onSuccess callback

4. **Error Handling (3 tests)**
   - ✅ Displays API error messages
   - ✅ Displays backend validation errors
   - ✅ Handles receipt upload failures

5. **Receipt Upload (2 tests)**
   - ✅ Uploads receipt before expense creation
   - ✅ Shows uploading status

6. **Cancel Button (2 tests)**
   - ✅ Calls onCancel when clicked
   - ✅ Disables during submission

### ExpensesList.test.tsx
**Location:** `src/test/ExpensesList.test.tsx`

**Test Suites:** 7 nested suites, **40 test cases**

1. **Component Rendering (5 tests)**
   - ✅ Renders page header
   - ✅ Renders filters section
   - ✅ Loads and displays expenses
   - ✅ Loads categories in filter
   - ✅ Displays loading state
   - ✅ Displays error messages
   - ✅ Displays empty state

2. **Filtering (8 tests)**
   - ✅ Applies search filter
   - ✅ Applies date range filter
   - ✅ Applies category filter
   - ✅ Applies amount range filter
   - ✅ Applies currency filter
   - ✅ Applies multiple filters together
   - ✅ Resets to first page on filter change
   - ✅ Clears all filters

3. **Pagination (6 tests)**
   - ✅ Displays pagination controls
   - ✅ Navigates to next page
   - ✅ Navigates to previous page
   - ✅ Disables prev button on first page
   - ✅ Disables next button on last page
   - ✅ Changes page size

4. **Expense Display (4 tests)**
   - ✅ Formats amounts correctly
   - ✅ Formats dates correctly
   - ✅ Displays category names
   - ✅ Displays tags

5. **Expense Deletion (3 tests)**
   - ✅ Deletes with confirmation
   - ✅ Cancels on confirmation cancel
   - ✅ Shows error on delete failure

6. **Refetch Behavior (2 tests)**
   - ✅ Refetches on page change
   - ✅ Refetches on filter change

**Total Test Cases: 76 tests**

## Mock Data

**src/test/mockData.ts**
- Mock categories (3 categories)
- Mock expenses (3 expenses)
- Mock paginated response
- Mock expense DTOs
- Reusable across all tests

## Running Tests

### Basic Commands
```bash
# Run all tests
npm test

# Run in watch mode
npm test -- --watch

# Run with UI
npm run test:ui

# Run with coverage
npm run test:coverage

# Run specific test file
npm test ExpenseForm.test

# Run tests matching pattern
npm test -- --grep "validation"
```

### CI/CD Commands
```bash
# Run once (CI mode)
npm test -- --run

# Generate coverage report
npm run test:coverage -- --reporter=json --reporter=html
```

## Test Patterns

### Component Testing
```typescript
// Render with Router wrapper
const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

// Basic render
render(<ExpenseForm />);

// With props
render(<ExpenseForm onSuccess={mockFn} onCancel={mockFn} />);
```

### User Interactions
```typescript
const user = userEvent.setup();

// Type into input
await user.type(screen.getByLabelText(/amount/i), '45.50');

// Select option
await user.selectOptions(screen.getByLabelText(/category/i), '1');

// Click button
await user.click(screen.getByRole('button', { name: /submit/i }));

// Upload file
const file = new File(['content'], 'file.png', { type: 'image/png' });
await user.upload(screen.getByLabelText(/receipt/i), file);
```

### Queries
```typescript
// By role (preferred)
screen.getByRole('button', { name: /submit/i });
screen.getByRole('heading', { name: /expenses/i });

// By label
screen.getByLabelText(/amount/i);

// By text
screen.getByText(/loading/i);

// By test ID
screen.getByTestId('expense-card');

// Query variants
screen.queryByText(/not found/i); // Returns null if not found
screen.findByText(/async/i); // Returns promise
```

### Assertions
```typescript
// Existence
expect(element).toBeInTheDocument();
expect(element).not.toBeInTheDocument();

// Visibility
expect(element).toBeVisible();

// Disabled state
expect(button).toBeDisabled();
expect(button).not.toBeDisabled();

// Values
expect(input).toHaveValue('test');
expect(input).toHaveValue(null);

// Text content
expect(element).toHaveTextContent('Hello');

// Collections
expect(screen.getAllByRole('option')).toHaveLength(4);
```

### Async Operations
```typescript
// Wait for element
await waitFor(() => {
  expect(screen.getByText(/loaded/i)).toBeInTheDocument();
});

// Wait for API call
await waitFor(() => {
  expect(mockApi).toHaveBeenCalled();
});

// Find (built-in waitFor)
const element = await screen.findByText(/async content/i);
```

### Mocking Services
```typescript
// Mock module
vi.mock('../services', () => ({
  listExpenses: vi.fn(),
  createExpense: vi.fn(),
}));

// Setup mock return value
vi.mocked(services.listExpenses).mockResolvedValue(mockData);

// Mock rejection
vi.mocked(services.createExpense).mockRejectedValue(new Error('Failed'));

// Mock with delay
vi.mocked(services.upload).mockImplementation(
  () => new Promise(resolve => setTimeout(() => resolve(data), 100))
);

// Verify calls
expect(services.listExpenses).toHaveBeenCalledWith(
  expect.objectContaining({ page: 0 })
);
expect(services.createExpense).toHaveBeenCalledTimes(1);
```

### Testing Hooks
```typescript
beforeEach(() => {
  vi.clearAllMocks();
  // Reset mock implementations
});

afterEach(() => {
  cleanup(); // Automatic with setup.ts
});

beforeAll(() => {
  // Setup once before all tests
});

afterAll(() => {
  // Cleanup once after all tests
});
```

## Coverage Reports

### Generate Coverage
```bash
npm run test:coverage
```

### Coverage Output
- **Text:** Console summary
- **HTML:** `coverage/index.html` (open in browser)
- **JSON:** `coverage/coverage-final.json`

### Coverage Thresholds
```typescript
// vitest.config.ts
coverage: {
  lines: 80,
  functions: 80,
  branches: 75,
  statements: 80,
}
```

## Best Practices

### ✅ DO
- Test user behavior, not implementation
- Use semantic queries (getByRole, getByLabelText)
- Wait for async operations
- Mock external dependencies
- Test error states
- Test loading states
- Use descriptive test names
- Organize with nested describe blocks

### ❌ DON'T
- Test implementation details
- Use container.querySelector
- Test internal state
- Skip async waits
- Test styling (use visual regression tests)
- Duplicate tests
- Make tests dependent on each other

## Debugging Tests

### Debug Single Test
```typescript
import { debug } from '@testing-library/react';

it('test name', () => {
  render(<Component />);
  debug(); // Prints DOM to console
});
```

### Debug Specific Element
```typescript
const element = screen.getByRole('button');
debug(element);
```

### Run Tests in Debug Mode
```bash
# Node inspect
node --inspect-brk ./node_modules/.bin/vitest --run

# VS Code: Add breakpoint and run debug config
```

### Vitest UI
```bash
npm run test:ui
# Opens browser with interactive test runner
```

## Common Issues

### Issue: Element not found
**Solution:** Use `findBy` or `waitFor` for async rendering
```typescript
const element = await screen.findByText(/async/i);
// or
await waitFor(() => {
  expect(screen.getByText(/async/i)).toBeInTheDocument();
});
```

### Issue: Act warnings
**Solution:** Wrap state updates in `waitFor`
```typescript
await waitFor(() => {
  expect(mockFn).toHaveBeenCalled();
});
```

### Issue: Router required
**Solution:** Wrap component in BrowserRouter
```typescript
render(
  <BrowserRouter>
    <Component />
  </BrowserRouter>
);
```

### Issue: Window.confirm not working
**Solution:** Mock window methods
```typescript
const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
// ... test code
confirmSpy.mockRestore();
```

## CI/CD Integration

### GitHub Actions
```yaml
- name: Install dependencies
  run: cd frontend && npm ci

- name: Run tests
  run: cd frontend && npm test -- --run

- name: Generate coverage
  run: cd frontend && npm run test:coverage

- name: Upload coverage
  uses: codecov/codecov-action@v3
  with:
    files: ./frontend/coverage/coverage-final.json
```

## Future Enhancements

- [ ] Add E2E tests with Playwright
- [ ] Add visual regression tests
- [ ] Add accessibility tests (@testing-library/jest-dom)
- [ ] Add MSW for API mocking
- [ ] Add component snapshot tests
- [ ] Add performance tests
- [ ] Increase coverage to 90%+
- [ ] Add mutation testing

## Resources

- [Vitest Documentation](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/react)
- [Testing Library Queries](https://testing-library.com/docs/queries/about)
- [User Event Documentation](https://testing-library.com/docs/user-event/intro)
- [Common Mistakes](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
