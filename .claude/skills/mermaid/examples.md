# Example Diagrams

Reference examples that demonstrate team conventions. Use these as templates when generating new diagrams.

## Example 1: Context Switch User Flow

This is the canonical example of our user flow style. It covers the profile context-switch flow including profile selection, confirmation, data reload, and error handling.

```mermaid
flowchart LR
    start([User taps<br/>Profile Switch Card]) --> hasMultiple{More than<br/>one profile?}

    hasMultiple -->|No| showToast[/Show toast:<br/>'Only one profile exists'/]
    showToast --> done([Done])

    hasMultiple -->|Yes| showSelector[Show profile<br/>selection screen]

    noteSelector[/"Profiles listed excluding<br/>the currently active one"/]
    noteSelector ~~~ showSelector

    showSelector --> userPicks[User selects<br/>a profile]
    showSelector --> userCancels[User taps Cancel]
    userCancels --> done

    userPicks --> confirmSwitch{{Switch active<br/>profile via API}}

    confirmSwitch -->|Success| reloadData{{Reload data<br/>for new profile}}
    confirmSwitch -->|Error| showError[/Show error dialog:<br/>'Could not switch profile'/]
    showError --> showSelector

    reloadData -->|Success| navigate[Navigate back to<br/>originating screen]
    reloadData -->|Error| showSyncError[/Show error dialog:<br/>'Data sync failed'/]
    showSyncError --> navigate

    navigate --> done

    %% Styling
    classDef startEnd fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px,color:#1b5e20
    classDef action fill:#e3f2fd,stroke:#1565c0,stroke-width:1px,color:#0d47a1
    classDef decision fill:#fff3e0,stroke:#e65100,stroke-width:2px,color:#bf360c
    classDef uiOutput fill:#f3e5f5,stroke:#6a1b9a,stroke-width:1px,color:#4a148c
    classDef apiCall fill:#fce4ec,stroke:#b71c1c,stroke-width:1px,color:#880e4f
    classDef note fill:#f9f9f9,stroke:#999,stroke-dasharray: 5 5,color:#666,font-size:12px

    class start,done startEnd
    class showSelector,userPicks,userCancels,navigate action
    class hasMultiple decision
    class showToast,showError,showSyncError uiOutput
    class confirmSwitch,reloadData apiCall
    class noteSelector note
```

### Notes

- The `~~~` (invisible link) connects annotation nodes near their relevant step without adding an arrow
- Parallelogram nodes (`[/text/]`) represent any UI that appears to the user: dialogs, error messages, toasts
- Hexagon nodes (`{{text}}`) represent API/system operations
- The error recovery loop (`showError` → `showSelector`) shows how to handle cyclical flows

---

## Example 2: API Sync Sequence

Demonstrates sequence diagram conventions for API interactions.

```mermaid
sequenceDiagram
    actor User
    participant App as Mobile App
    participant VM as ViewModel
    participant API as Backend API
    participant DB as Local DB

    User->>App: Taps "Log Food"

    App->>VM: onFoodSubmit(foodItem)
    activate VM

    VM->>API: POST /api/log-food
    activate API

    API->>DB: Save food log entry
    DB-->>API: Entry saved

    alt API error
        API-->>VM: 500 Internal Server Error
        VM-->>App: emit UiState.Error("Could not save entry")
        App-->>User: Show error snackbar
    else Success
        API-->>VM: 200 OK {updatedScore}
        deactivate API

        VM->>VM: Update score StateFlow
        VM-->>App: emit UiState.Success
        deactivate VM

        App-->>User: Show success feedback, update score display
    end
```

---

## Example 3: Food Log Screen State Diagram

Demonstrates state diagram conventions for ViewModel/screen states.

```mermaid
stateDiagram-v2
    [*] --> Loading

    Loading --> Ready: Data loaded
    Loading --> Error: Load failed

    Error --> Loading: Retry tapped

    Ready --> Editing: User taps food entry
    Editing --> Ready: Save or cancel

    state Ready {
        [*] --> Idle
        Idle --> Searching: User types in search field
        Searching --> Idle: Clear search
        Searching --> ItemSelected: User taps result
        ItemSelected --> Idle: Item logged
    }

    Ready --> [*]: User navigates away

    note right of Loading: Initial state on screen entry
    note right of Ready: Primary interactive state
```

---

## Example 4: Compact Flow (Simple Feature)

Not every diagram needs full styling. Small flows should be clean and minimal.

```mermaid
flowchart LR
    start([User opens Settings]) --> tapTheme[Tap 'Appearance']
    tapTheme --> selectTheme[Select theme from list]
    selectTheme --> applyTheme{System theme<br/>selected?}
    applyTheme -->|Yes| followSystem[/Follow device dark/light setting/]
    applyTheme -->|No| setFixed[/Apply selected theme immediately/]
    followSystem --> done([Done])
    setFixed --> done
```

No `classDef` styling needed here — the flow is simple enough to read without colour coding.

---

## Anti-Patterns to Avoid

**Single-letter node IDs:**
```mermaid
%% BAD — unreadable when revisited
flowchart LR
    A --> B --> C{D?}
    C -->|Yes| E
    C -->|No| F
```

**Missing decision labels:**
```mermaid
%% BAD — which path is which?
flowchart LR
    check{Valid?} --> success
    check --> error
```

**Overuse of styling on small diagrams:**
```mermaid
%% BAD — 4 nodes don't need 6 class definitions
```

**Mixing flow directions:**
```mermaid
%% BAD — don't start LR then try to force TD sections
```
